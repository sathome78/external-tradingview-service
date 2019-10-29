package me.exrates.externalservice.services;

import me.exrates.externalservice.repositories.Google2FARepository;
import me.exrates.externalservice.services.impl.Google2FAServiceImpl;
import org.jboss.aerogear.security.otp.Totp;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class Google2FAServiceTest extends AbstractTest {

    @Mock
    private Google2FARepository google2FARepository;

    private Google2FAService google2FAService;

    @Before
    public void setUp() throws Exception {
        google2FAService = spy(new Google2FAServiceImpl(google2FARepository));
    }

    @Test
    public void isGoogleAuthenticatorEnable_ok() {
        doReturn(true)
                .when(google2FARepository)
                .isGoogleAuthenticatorEnable(anyInt());

        boolean enabled = google2FAService.isGoogleAuthenticatorEnable(1);

        assertTrue(enabled);

        verify(google2FARepository, atLeastOnce()).isGoogleAuthenticatorEnable(anyInt());
    }

    @Test
    public void checkGoogle2faVerifyCode_ok() {
        final Totp totp = new Totp(GOOGLE_2FA_SECRET);
        String code = totp.now();

        doReturn(GOOGLE_2FA_SECRET)
                .when(google2FARepository)
                .getGoogleAuthenticationSecretCode(anyInt());

        boolean enabled = google2FAService.checkGoogle2faVerifyCode(code, 1);

        assertTrue(enabled);

        verify(google2FARepository, atLeastOnce()).getGoogleAuthenticationSecretCode(anyInt());
    }
}