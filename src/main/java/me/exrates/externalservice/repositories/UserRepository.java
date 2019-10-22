package me.exrates.externalservice.repositories;

import me.exrates.externalservice.entities.UserDto;
import me.exrates.externalservice.entities.enums.UserStatus;

public interface UserRepository {

    UserDto findByLogin(String login);

    void save(UserDto user);

    void updateCode(int userId, String code);

    void updateStatus(String login, UserStatus status);
}