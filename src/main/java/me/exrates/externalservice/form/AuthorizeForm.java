package me.exrates.externalservice.form;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizeForm {

    @NotEmpty
    private String login;
    @NotEmpty
    private String password;
    @JsonProperty("2faCode")
    private Integer code;
}