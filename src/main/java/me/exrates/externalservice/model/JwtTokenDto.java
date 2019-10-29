package me.exrates.externalservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenDto {

    @JsonProperty("access_token")
    private String accessToken;
    private Long expiration;
    @JsonProperty("2fa_required")
    private boolean required2FA;

    public JwtTokenDto(boolean required2FA) {
        this.required2FA = required2FA;
    }
}