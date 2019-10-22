package me.exrates.externalservice.repositories.impl;

import lombok.extern.log4j.Log4j2;
import me.exrates.externalservice.entities.UserDto;
import me.exrates.externalservice.entities.enums.UserRole;
import me.exrates.externalservice.entities.enums.UserStatus;
import me.exrates.externalservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log4j2
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final NamedParameterJdbcOperations masterJdbcTemplate;

    @Autowired
    public UserRepositoryImpl(@Qualifier("masterTemplate") NamedParameterJdbcOperations masterJdbcTemplate) {
        this.masterJdbcTemplate = masterJdbcTemplate;
    }

    @Override
    public UserDto findByLogin(String login) {
        final String sql = "SELECT " +
                "u.id, " +
                "u.email, " +
                "u.password, " +
                "u.phone, " +
                "u.regdate, " +
                "u.status, " +
                "u.roleid, " +
                "u.login_pin " +
                "FROM USER u " +
                "WHERE u.email = :email";

        final Map<String, Object> params = new HashMap<>();
        params.put("email", login);

        try {
            return masterJdbcTemplate.queryForObject(sql, params, (rs, row) -> UserDto.builder()
                    .id(rs.getInt("id"))
                    .login(rs.getString("email"))
                    .password(rs.getString("password"))
                    .phone(rs.getString("phone"))
                    .createdAt(Objects.nonNull(rs.getTimestamp("regdate"))
                            ? rs.getTimestamp("regdate").toLocalDateTime()
                            : null)
                    .status(UserStatus.of(rs.getInt("status")))
                    .role(UserRole.of(rs.getInt("roleid")))
                    .code(rs.getString("login_pin"))
                    .build());
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void save(UserDto user) {
        final String sql = "INSERT INTO USER (email, password, phone, regdate, status, roleid) " +
                "VALUES (:email, :password, :phone, :regdate, :status, :role)";

        final Map<String, Object> params = new HashMap<>();
        params.put("email", user.getLogin());
        params.put("password", user.getPassword());
        params.put("phone", user.getPhone());
        params.put("regdate", user.getCreatedAt());
        params.put("status", user.getStatus().getStatus());
        params.put("role", user.getRole().getRole());

        masterJdbcTemplate.update(sql, params);
    }

    @Override
    public void updateCode(int userId, String code) {
        final String sql = "UPDATE USER u " +
                "SET u.login_pin = :login_pin " +
                "WHERE u.id = :user_id";

        final Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("login_pin", code);

        masterJdbcTemplate.update(sql, params);
    }

    @Override
    public void updateStatus(String login, UserStatus status) {
        final String sql = "UPDATE USER u " +
                "SET u.status = :status " +
                "WHERE u.email = :email";

        final Map<String, Object> params = new HashMap<>();
        params.put("email", login);
        params.put("status", status.getStatus());

        masterJdbcTemplate.update(sql, params);
    }
}