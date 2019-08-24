package me.exrates.externalservice.services;

import me.exrates.externalservice.dto.JwtTokenDto;
import me.exrates.externalservice.entities.User;
import me.exrates.externalservice.exceptions.AuthorizationException;
import me.exrates.externalservice.exceptions.InvalidCodeException;
import me.exrates.externalservice.exceptions.conflict.EmailExistException;
import me.exrates.externalservice.exceptions.notfound.UserNotFoundException;

public interface UserService {

    User findOne(String login) throws UserNotFoundException;

    void register(String login, String password, String phone) throws EmailExistException;

    void authorize(String login) throws UserNotFoundException;

    JwtTokenDto authorize(String login, String password, Integer code) throws UserNotFoundException, InvalidCodeException, AuthorizationException;
}