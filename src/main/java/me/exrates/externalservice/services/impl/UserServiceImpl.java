package me.exrates.externalservice.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.dto.JwtTokenDto;
import me.exrates.externalservice.entities.User;
import me.exrates.externalservice.entities.enums.EmailType;
import me.exrates.externalservice.exceptions.AuthorizationException;
import me.exrates.externalservice.exceptions.InvalidCodeException;
import me.exrates.externalservice.exceptions.conflict.EmailExistException;
import me.exrates.externalservice.exceptions.notfound.UserNotFoundException;
import me.exrates.externalservice.properties.SecurityProperty;
import me.exrates.externalservice.repositories.UserRepository;
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

@Slf4j
@Transactional
@Service
public class UserServiceImpl implements UserService {

    private static final Long JWT_TOKEN_EXPIRE_PERIOD = 7L; //days

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionAfterCommitExecutor afterCommitExecutor;
    private final MailSenderService mailSenderService;
    private final SecurityProperty securityProperty;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           TransactionAfterCommitExecutor afterCommitExecutor,
                           MailSenderService mailSenderService,
                           SecurityProperty securityProperty) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.afterCommitExecutor = afterCommitExecutor;
        this.mailSenderService = mailSenderService;
        this.securityProperty = securityProperty;
    }

    @Transactional(readOnly = true)
    @Override
    public User findOne(String login) throws UserNotFoundException {
        User user = userRepository.findByLogin(login);
        if (Objects.isNull(user)) {
            throw new UserNotFoundException(String.format("User [%s] not found", login));
        }
        return user;
    }

    @Override
    public void register(String login, String password, String phone) throws EmailExistException {
        User user = userRepository.findByLogin(login);
        if (Objects.nonNull(user)) {
            throw new EmailExistException(String.format("User with login (email) [%s] already exists", user.getLogin()));
        }
        user = User.builder()
                .login(login)
                .password(passwordEncoder.encode(password))
                .phone(phone)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    @Override
    public void authorize(String login) throws UserNotFoundException {
        User user = findOne(login);
        user.setCode(Integer.valueOf(RandomStringUtils.randomNumeric(6)));
        userRepository.save(user);

        afterCommitExecutor.execute(() -> {
            Map<String, Object> properties = new HashMap<>();
            properties.put("code", user.getCode());

            mailSenderService.send(EmailType.AUTHORIZATION_2FA_CODE, user.getLogin(), properties);
        });
    }

    @Override
    public JwtTokenDto authorize(String login, String password, Integer code)
            throws UserNotFoundException, InvalidCodeException, AuthorizationException {
        User user = findOne(login);
        if (Objects.isNull(user.getCode()) || !user.getCode().equals(code)) {
            throw new InvalidCodeException("Client's authorization code is not valid");
        }
        user.setCode(null);
        userRepository.save(user);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthorizationException("Invalid password");
        }

        final LocalDateTime expireTime = LocalDateTime.now()
                .plusDays(JWT_TOKEN_EXPIRE_PERIOD);
        String accessToken = JWT.create()
                .withSubject(user.getLogin())
                .withExpiresAt(Date.from(expireTime.atZone(ZoneOffset.systemDefault()).toInstant()))
                .sign(Algorithm.HMAC256(securityProperty.getAuthorizationSecret()));

        return new JwtTokenDto(accessToken, Timestamp.valueOf(expireTime).getTime(), true);
    }
}