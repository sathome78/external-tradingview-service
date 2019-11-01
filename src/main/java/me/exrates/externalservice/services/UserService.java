package me.exrates.externalservice.services;

import me.exrates.externalservice.exceptions.AuthorizationException;
import me.exrates.externalservice.exceptions.VerificationException;
import me.exrates.externalservice.exceptions.conflict.EmailExistException;
import me.exrates.externalservice.exceptions.notfound.UserNotFoundException;
import me.exrates.externalservice.model.JwtTokenDto;
import me.exrates.externalservice.model.UserDto;
import me.exrates.externalservice.model.enums.UserRole;

import java.util.UUID;

public interface UserService {

    UserDto findOne(String login) throws UserNotFoundException, VerificationException;

    void register(String login, String password, String phone, UserRole role) throws EmailExistException;

    void verify(UUID code) throws VerificationException;

    JwtTokenDto authorize(UserDto user, String password) throws AuthorizationException;
}