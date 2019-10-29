package me.exrates.externalservice.form;

import lombok.Data;
import me.exrates.externalservice.model.enums.UserRole;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class RegisterForm {

    @NotEmpty
    @Email
    private String login;
    @NotEmpty
    private String password;
    @Pattern(regexp = "\\+\\d{12}")
    private String phone;
    @NotNull
    private UserRole role;
}