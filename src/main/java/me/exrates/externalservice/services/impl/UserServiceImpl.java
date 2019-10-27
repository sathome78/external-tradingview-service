package me.exrates.externalservice.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.model.JwtTokenDto;
import me.exrates.externalservice.model.UserDto;
import me.exrates.externalservice.exceptions.AuthorizationException;
import me.exrates.externalservice.exceptions.InvalidCodeException;
import me.exrates.externalservice.exceptions.VerificationException;
import me.exrates.externalservice.exceptions.conflict.EmailExistException;
import me.exrates.externalservice.exceptions.notfound.UserNotFoundException;
import me.exrates.externalservice.model.enums.EmailType;
import me.exrates.externalservice.model.enums.UserRole;
import me.exrates.externalservice.model.enums.UserStatus;
import me.exrates.externalservice.properties.SecurityProperty;
import me.exrates.externalservice.repositories.UserRepository;
import me.exrates.externalservice.services.Google2FAService;
import me.exrates.externalservice.services.MailSenderService;
import me.exrates.externalservice.services.UserService;
import me.exrates.externalservice.utils.TransactionAfterCommitExecutor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Transactional
@Service
public class UserServiceImpl implements UserService {

    private static final Long JWT_TOKEN_EXPIRE_PERIOD = 7L; //days

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionAfterCommitExecutor afterCommitExecutor;
    private final MailSenderService mailSenderService;
    private final Google2FAService google2FAService;
    private final SecurityProperty securityProperty;

    private Map<UUID, String> loginMap = new ConcurrentHashMap<>();

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           TransactionAfterCommitExecutor afterCommitExecutor,
                           MailSenderService mailSenderService,
                           Google2FAService google2FAService,
                           SecurityProperty securityProperty) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.afterCommitExecutor = afterCommitExecutor;
        this.mailSenderService = mailSenderService;
        this.google2FAService = google2FAService;
        this.securityProperty = securityProperty;
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto findOne(String login) throws UserNotFoundException, VerificationException {
        UserDto user = userRepository.findByLogin(login);
        if (Objects.isNull(user)) {
            throw new UserNotFoundException(String.format("User [%s] not found", login));
        }
        if (user.getStatus() == UserStatus.REGISTERED) {
            sendVerificationMail(login);

            throw new VerificationException(String.format("User email [%s] not verified", login));
        }
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new VerificationException(String.format("User email [%s] blocked", login));
        }
        return user;
    }

    @Override
    public void register(String login, String password, String phone, UserRole role) throws EmailExistException {
        UserDto user = userRepository.findByLogin(login);
        if (Objects.nonNull(user)) {
            throw new EmailExistException(String.format("User with login (email) [%s] already exists", user.getLogin()));
        }
        user = UserDto.builder()
                .login(login)
                .password(passwordEncoder.encode(password))
                .phone(phone)
                .createdAt(LocalDateTime.now())
                .role(role)
                .status(UserStatus.REGISTERED)
                .build();
        userRepository.save(user);

        sendVerificationMail(login);
    }

    @Override
    public void verify(UUID code) throws VerificationException {
        final String login = loginMap.remove(code);
        if (Objects.isNull(login)) {
            throw new VerificationException("User email verification failed");
        }
        userRepository.updateStatus(login, UserStatus.ACTIVATED);
    }

    @Override
    public void authorize(UserDto user) {
        if (!google2FAService.isGoogleAuthenticatorEnable(user.getId())) {
            final String code = RandomStringUtils.randomNumeric(6);

            userRepository.updateCode(user.getId(), passwordEncoder.encode(code));

            afterCommitExecutor.execute(() -> {
                Map<String, Object> properties = new HashMap<>();
                properties.put("code", code);

                mailSenderService.send(EmailType.AUTHORIZATION, user.getLogin(), properties);
            });
        }
    }

    @Override
    public JwtTokenDto authorize(UserDto user, String password, boolean required2FA) throws AuthorizationException {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthorizationException("Invalid user password");
        }

        final LocalDateTime expireTime = LocalDateTime.now()
                .plusDays(JWT_TOKEN_EXPIRE_PERIOD);
        String accessToken = JWT.create()
                .withSubject(user.getLogin())
                .withExpiresAt(Date.from(expireTime.atZone(ZoneOffset.systemDefault()).toInstant()))
                .sign(Algorithm.HMAC256(securityProperty.getAuthorizationSecret()));

        return new JwtTokenDto(accessToken, Timestamp.valueOf(expireTime).getTime(), required2FA);
    }

    @Override
    public void validateCode(UserDto user, String code) throws InvalidCodeException {
        if (google2FAService.isGoogleAuthenticatorEnable(user.getId())) {
            if (!google2FAService.checkGoogle2faVerifyCode(code, user.getId())) {
                throw new InvalidCodeException("User's google2fa authorization code is not valid");
            }
        } else {
            if (!passwordEncoder.matches(code, user.getCode())) {
                throw new InvalidCodeException("User's authorization code is not valid");
            }
            userRepository.updateCode(user.getId(), null);
        }
    }

    private void sendVerificationMail(String login) {
        afterCommitExecutor.execute(() -> {
            final UUID code = UUID.randomUUID();
            loginMap.put(code, login);

            Map<String, Object> properties = new HashMap<>();
            properties.put("code", code);

            mailSenderService.send(EmailType.VERIFICATION, login, properties);
        });
    }
}