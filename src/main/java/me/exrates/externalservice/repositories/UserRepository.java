package me.exrates.externalservice.repositories;

import me.exrates.externalservice.model.UserDto;
import me.exrates.externalservice.model.enums.UserStatus;

public interface UserRepository {

    UserDto findByLogin(String login);

    void save(UserDto user);

    void updateCode(int userId, String code);

    void updateStatus(String login, UserStatus status);
}