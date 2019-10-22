package me.exrates.externalservice.services;

import me.exrates.externalservice.dto.JwtTokenDto;
import me.exrates.externalservice.entities.UserDto;
import me.exrates.externalservice.entities.enums.UserRole;
import me.exrates.externalservice.exceptions.AuthorizationException;
import me.exrates.externalservice.exceptions.InvalidCodeException;
import me.exrates.externalservice.exceptions.VerificationException;
import me.exrates.externalservice.exceptions.conflict.EmailExistException;
import me.exrates.externalservice.exceptions.notfound.UserNotFoundException;

import java.util.UUID;

public interface UserService {

    UserDto findOne(String login) throws UserNotFoundException, VerificationException;

    void register(String login, String password, String phone, UserRole role) throws EmailExistException;

    void verify(UUID code) throws VerificationException;

    void authorize(UserDto user);

    JwtTokenDto authorize(UserDto user, String password, boolean required2FA) throws AuthorizationException;

    void validateCode(UserDto user, String code) throws InvalidCodeException;
}