package me.exrates.externalservice.controllers;

import me.exrates.externalservice.dto.JwtTokenDto;
import me.exrates.externalservice.exceptions.ServiceException;
import me.exrates.externalservice.exceptions.ValidationException;
import me.exrates.externalservice.exceptions.conflict.EmailExistException;
import me.exrates.externalservice.form.AuthorizeForm;
import me.exrates.externalservice.form.RegisterForm;
import me.exrates.externalservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api")
@CrossOrigin
public class UserController {

    private final UserService userService;

    private final boolean required2FA;

    @Autowired
    public UserController(UserService userService,
                          @Value("${authorize.2fa-required}") boolean required2FA) {
        this.userService = userService;
        this.required2FA = required2FA;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity register(@Validated RegisterForm form,
                                   Errors result) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (result.hasErrors()) {
                throw new ValidationException(result.getAllErrors());
            }
            userService.register(form.getLogin(), form.getPassword(), form.getPhone());

            response.put("s", "ok");

            return ResponseEntity.ok(response);
        } catch (ValidationException | EmailExistException ex) {
            response.put("s", "error");
            response.put("errmsg", ex.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping(value = "/authorize", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity authorize(@Validated AuthorizeForm form,
                                    Errors result) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (result.hasErrors()) {
                throw new ValidationException(result.getAllErrors());
            }
            JwtTokenDto tokenDto;
            if (Objects.isNull(form.getCode()) && required2FA) {
                userService.authorize(form.getLogin());

                tokenDto = new JwtTokenDto(true);
            } else {
                tokenDto = userService.authorize(form.getLogin(), form.getPassword(), form.getCode());
            }
            response.put("s", "ok");
            response.put("d", tokenDto);
        } catch (ValidationException | ServiceException ex) {
            response.put("s", "error");
            response.put("errmsg", ex.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}