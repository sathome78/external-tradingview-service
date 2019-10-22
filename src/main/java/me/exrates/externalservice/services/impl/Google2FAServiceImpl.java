package me.exrates.externalservice.services.impl;

import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.repositories.Google2FARepository;
import me.exrates.externalservice.services.Google2FAService;
import me.exrates.externalservice.utils.NumberUtil;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Service
public class Google2FAServiceImpl implements Google2FAService {

    private final Google2FARepository google2FARepository;

    @Autowired
    public Google2FAServiceImpl(Google2FARepository google2FARepository) {
        this.google2FARepository = google2FARepository;
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isGoogleAuthenticatorEnable(int userId) {
        return google2FARepository.isGoogleAuthenticatorEnable(userId);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean checkGoogle2faVerifyCode(String code, Integer userId) {
        String google2faSecret = google2FARepository.getGoogleAuthenticationSecretCode(userId);

        final Totp totp = new Totp(google2faSecret);
        return NumberUtil.isValidLong(code) && totp.verify(code);
    }
}