package me.exrates.externalservice.web.security;

import me.exrates.externalservice.exceptions.notfound.UserNotFoundException;
import me.exrates.externalservice.properties.SecurityProperties;
import me.exrates.externalservice.services.UserService;
import org.springframework.security.core.userdetails.UserDetails;

public class UserJwtAuthenticationProcessFilter extends AbstractJwtAuthenticationProcessFilter {

    private final UserService userService;

    public UserJwtAuthenticationProcessFilter(SecurityProperties securityProperties,
                                              UserService userService) {
        super(securityProperties);
        this.userService = userService;
    }

    @Override
    UserDetails getById(String principalId) throws UserDetailsNotFoundException {
        try {
            return userService.findOne(principalId);
        } catch (UserNotFoundException ex) {
            throw new UserDetailsNotFoundException(ex.getMessage(), ex);
        }
    }
}