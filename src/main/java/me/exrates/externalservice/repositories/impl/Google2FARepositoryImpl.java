package me.exrates.externalservice.repositories.impl;

import lombok.extern.log4j.Log4j2;
import me.exrates.externalservice.repositories.Google2FARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Repository
public class Google2FARepositoryImpl implements Google2FARepository {

    private final NamedParameterJdbcOperations masterJdbcTemplate;

    @Autowired
    public Google2FARepositoryImpl(NamedParameterJdbcOperations masterJdbcTemplate) {
        this.masterJdbcTemplate = masterJdbcTemplate;
    }

    @Override
    public boolean isGoogleAuthenticatorEnable(int userId) {
        final String sql = "SELECT ga.enable FROM 2FA_GOOGLE_AUTHENTICATOR ga WHERE ga.user_id = :user_id";

        final Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);

        try {
            return masterJdbcTemplate.queryForObject(sql, params, Boolean.class);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public String getGoogleAuthenticationSecretCode(Integer userId) {
        String sql = "SELECT ga.secret_code FROM 2FA_GOOGLE_AUTHENTICATOR ga WHERE ga.user_id = :user_id";

        final Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);

        try {
            return masterJdbcTemplate.queryForObject(sql, params, String.class);
        }catch (EmptyResultDataAccessException ex){
            return null;
        }
    }
}