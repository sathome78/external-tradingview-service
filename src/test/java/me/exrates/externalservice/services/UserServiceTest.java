package me.exrates.externalservice.services;

import me.exrates.externalservice.exceptions.AuthorizationException;
import me.exrates.externalservice.exceptions.InvalidCodeException;
import me.exrates.externalservice.exceptions.VerificationException;
import me.exrates.externalservice.exceptions.conflict.EmailExistException;
import me.exrates.externalservice.exceptions.notfound.UserNotFoundException;
import me.exrates.externalservice.model.JwtTokenDto;
import me.exrates.externalservice.model.UserDto;
import me.exrates.externalservice.model.enums.UserRole;
import me.exrates.externalservice.model.enums.UserStatus;
import me.exrates.externalservice.properties.SecurityProperty;
import me.exrates.externalservice.repositories.UserRepository;
import me.exrates.externalservice.services.impl.UserServiceImpl;
import me.exrates.externalservice.utils.TransactionAfterCommitExecutor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class UserServiceTest extends AbstractTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TransactionAfterCommitExecutor afterCommitExecutor;
    @Mock
    private MailSenderService mailSenderService;
    @Mock
    private Google2FAService google2FAService;
    @Mock
    private SecurityProperty securityProperty;

    private UserService userService;

    @Before
    public void setUp() throws Exception {
        userService = spy(new UserServiceImpl(
                userRepository,
                passwordEncoder,
                afterCommitExecutor,
                mailSenderService,
                google2FAService,
                securityProperty));
    }

    @Test
    public void findOne_ok() throws Exception {
        doReturn(UserDto.builder()
                .status(UserStatus.ACTIVATED)
                .build())
                .when(userRepository)
                .findByLogin(anyString());

        UserDto user = userService.findOne("login");

        assertNotNull(user);

        verify(userRepository, atLeastOnce()).findByLogin(anyString());
    }

    @Test(expected = UserNotFoundException.class)
    public void findOne_not_found() throws Exception {
        doReturn(null)
                .when(userRepository)
                .findByLogin(anyString());

        userService.findOne("login");
    }

    @Test(expected = VerificationException.class)
    public void findOne_user_registered() throws Exception {
        doReturn(UserDto.builder()
                .status(UserStatus.REGISTERED)
                .build())
                .when(userRepository)
                .findByLogin(anyString());

        userService.findOne("login");
    }

    @Test(expected = VerificationException.class)
    public void findOne_user_blocked() throws Exception {
        doReturn(UserDto.builder()
                .status(UserStatus.BLOCKED)
                .build())
                .when(userRepository)
                .findByLogin(anyString());

        userService.findOne("login");
    }

    @Test
    public void register_ok() throws Exception {
        doReturn(null)
                .when(userRepository)
                .findByLogin(anyString());
        doNothing()
                .when(userRepository)
                .save(any(UserDto.class));

        userService.register("login", "password", "phone", UserRole.USER);

        verify(userRepository, atLeastOnce()).findByLogin(anyString());
        verify(userRepository, atLeastOnce()).save(any(UserDto.class));
    }

    @Test(expected = EmailExistException.class)
    public void register_user_found() throws Exception {
        doReturn(new UserDto())
                .when(userRepository)
                .findByLogin(anyString());
        doNothing()
                .when(userRepository)
                .save(any(UserDto.class));

        userService.register("login", "password", "phone", UserRole.USER);
    }

    @Test
    public void verify_ok() throws Exception {
        UUID code = UUID.randomUUID();

        Map<UUID, String> loginMap = new ConcurrentHashMap<>();
        loginMap.put(code, "login");

        ReflectionTestUtils.setField(userService, "loginMap", loginMap);

        doNothing()
                .when(userRepository)
                .updateStatus(anyString(), any(UserStatus.class));

        userService.verify(code);

        verify(userRepository, atLeastOnce()).updateStatus(anyString(), any(UserStatus.class));
    }

    @Test(expected = VerificationException.class)
    public void verify_user_not_found() throws Exception {
        UUID code = UUID.randomUUID();

        doNothing()
                .when(userRepository)
                .updateStatus(anyString(), any(UserStatus.class));

        userService.verify(code);
    }

    @Test
    public void authorize1_ok() {
        UserDto user = UserDto.builder()
                .id(1)
                .login("login")
                .build();

        doReturn(false)
                .when(google2FAService)
                .isGoogleAuthenticatorEnable(anyInt());
        doNothing()
                .when(userRepository)
                .updateCode(anyInt(), anyString());
        doReturn("password_encoded")
                .when(passwordEncoder)
                .encode(anyString());

        userService.authorize(user);

        verify(google2FAService, atLeastOnce()).isGoogleAuthenticatorEnable(anyInt());
        verify(userRepository, atLeastOnce()).updateCode(anyInt(), anyString());
    }

    @Test
    public void authorize1_google() {
        UserDto user = UserDto.builder()
                .id(1)
                .login("login")
                .build();

        doReturn(true)
                .when(google2FAService)
                .isGoogleAuthenticatorEnable(anyInt());

        userService.authorize(user);

        verify(google2FAService, atLeastOnce()).isGoogleAuthenticatorEnable(anyInt());
        verify(userRepository, never()).updateCode(anyInt(), anyString());
    }

    @Test
    public void authorize2_ok() throws Exception {
        UserDto user = UserDto.builder()
                .id(1)
                .login("login")
                .password("password")
                .build();

        doReturn(true)
                .when(passwordEncoder)
                .matches(anyString(), anyString());
        doReturn("secret")
                .when(securityProperty)
                .getAuthorizationSecret();
        doNothing()
                .when(userRepository)
                .updateCode(anyInt(), anyString());

        JwtTokenDto token = userService.authorize(user, "password", true);

        assertNotNull(token);

        verify(passwordEncoder, atLeastOnce()).matches(anyString(), anyString());
        verify(securityProperty, atLeastOnce()).getAuthorizationSecret();
    }

    @Test(expected = AuthorizationException.class)
    public void authorize2_password_not_matches() throws Exception {
        UserDto user = UserDto.builder()
                .id(1)
                .login("login")
                .password("password")
                .build();

        doReturn(false)
                .when(passwordEncoder)
                .matches(anyString(), anyString());

        userService.authorize(user, "password", true);
    }

    @Test
    public void validateCode_google_true_ok() throws Exception {
        UserDto user = UserDto.builder()
                .id(1)
                .login("login")
                .password("password")
                .code("code")
                .build();

        doReturn(true)
                .when(google2FAService)
                .isGoogleAuthenticatorEnable(anyInt());
        doReturn(true)
                .when(google2FAService)
                .checkGoogle2faVerifyCode(anyString(), anyInt());

        userService.validateCode(user, "code");

        verify(google2FAService, atLeastOnce()).isGoogleAuthenticatorEnable(anyInt());
        verify(google2FAService, atLeastOnce()).checkGoogle2faVerifyCode(anyString(), anyInt());
    }

    @Test(expected = InvalidCodeException.class)
    public void validateCode_google_true_verified_false() throws Exception {
        UserDto user = UserDto.builder()
                .id(1)
                .login("login")
                .password("password")
                .code("code")
                .build();

        doReturn(true)
                .when(google2FAService)
                .isGoogleAuthenticatorEnable(anyInt());
        doReturn(false)
                .when(google2FAService)
                .checkGoogle2faVerifyCode(anyString(), anyInt());

        userService.validateCode(user, "code");
    }

    @Test
    public void validateCode_google_false_ok() throws Exception {
        UserDto user = UserDto.builder()
                .id(1)
                .login("login")
                .password("password")
                .code("code")
                .build();

        doReturn(false)
                .when(google2FAService)
                .isGoogleAuthenticatorEnable(anyInt());
        doReturn(true)
                .when(passwordEncoder)
                .matches(anyString(), anyString());
        doNothing()
                .when(userRepository)
                .updateCode(anyInt(), anyString());

        userService.validateCode(user, "code");

        verify(google2FAService, atLeastOnce()).isGoogleAuthenticatorEnable(anyInt());
        verify(passwordEncoder, atLeastOnce()).matches(anyString(), anyString());
    }

    @Test(expected = InvalidCodeException.class)
    public void validateCode_google_password_not_matches() throws Exception {
        UserDto user = UserDto.builder()
                .id(1)
                .login("login")
                .password("password")
                .code("code")
                .build();

        doReturn(false)
                .when(google2FAService)
                .isGoogleAuthenticatorEnable(anyInt());
        doReturn(false)
                .when(passwordEncoder)
                .matches(anyString(), anyString());

        userService.validateCode(user, "code");
    }
}