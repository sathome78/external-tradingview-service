package me.exrates.externalservice.services;

public interface Google2FAService {

    boolean isGoogleAuthenticatorEnable(int userId);

    boolean checkGoogle2faVerifyCode(String code, Integer userId);
}