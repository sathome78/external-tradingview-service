package me.exrates.externalservice.repositories;

public interface Google2FARepository {

    boolean isGoogleAuthenticatorEnable(int userId);

    String getGoogleAuthenticationSecretCode(Integer userId);
}