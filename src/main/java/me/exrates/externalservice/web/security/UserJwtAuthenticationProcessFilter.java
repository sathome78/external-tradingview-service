package me.exrates.externalservice.web.security;

import me.exrates.externalservice.exceptions.VerificationException;
import me.exrates.externalservice.exceptions.notfound.UserNotFoundException;
import me.exrates.externalservice.properties.SecurityProperty;
import me.exrates.externalservice.services.UserService;
import org.springframework.security.core.userdetails.UserDetails;

public class UserJwtAuthenticationProcessFilter extends AbstractJwtAuthenticationProcessFilter {

    private final UserService userService;

    public UserJwtAuthenticationProcessFilter(SecurityProperty securityProperty,
                                              UserService userService) {
        super(securityProperty);
        this.userService = userService;
    }

    @Override
    UserDetails getById(String principalId) throws UserDetailsNotFoundException {
        try {
            return userService.findOne(principalId);
        } catch (UserNotFoundException | VerificationException ex) {
            throw new UserDetailsNotFoundException(ex.getMessage(), ex);
        }
    }
}