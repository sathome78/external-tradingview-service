package me.exrates.externalservice.controllers;

import me.exrates.externalservice.exceptions.ServiceException;
import me.exrates.externalservice.exceptions.ValidationException;
import me.exrates.externalservice.exceptions.conflict.EmailExistException;
import me.exrates.externalservice.form.AuthorizeForm;
import me.exrates.externalservice.form.RegisterForm;
import me.exrates.externalservice.model.JwtTokenDto;
import me.exrates.externalservice.model.UserDto;
import me.exrates.externalservice.model.enums.ResStatus;
import me.exrates.externalservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api")
@CrossOrigin
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    //for test
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity register(@Validated RegisterForm form,
                                   Errors result) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (result.hasErrors()) {
                throw new ValidationException(result.getAllErrors());
            }
            userService.register(form.getLogin(), form.getPassword(), form.getPhone(), form.getRole());

            response.put("s", ResStatus.OK.getStatus());

            return ResponseEntity.ok(response);
        } catch (ValidationException | EmailExistException ex) {
            response.put("s", ResStatus.ERROR.getStatus());
            response.put("errmsg", ex.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping(value = "/register/verify/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity verifyEmail(@PathVariable("code") UUID code) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.verify(code);

            response.put("s", ResStatus.OK.getStatus());

            return ResponseEntity.ok(response);
        } catch (ServiceException ex) {
            response.put("s", ResStatus.ERROR.getStatus());
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
            UserDto user = userService.findOne(form.getLogin());

            JwtTokenDto tokenDto = userService.authorize(user, form.getPassword());

            response.put("s", ResStatus.OK.getStatus());
            response.put("d", tokenDto);
        } catch (ValidationException | ServiceException ex) {
            response.put("s", ResStatus.ERROR.getStatus());
            response.put("errmsg", ex.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}