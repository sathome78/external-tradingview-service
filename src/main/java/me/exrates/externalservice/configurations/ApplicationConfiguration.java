package me.exrates.externalservice.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@EnableAsync
@Configuration
public class ApplicationConfiguration {

    public static final String ALLOWED_RESOLUTIONS_LIST = "allowedIntervalsList";

    @Value("${application.allowed-resolutions}")
    private String allowedResolutionsString;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean(ALLOWED_RESOLUTIONS_LIST)
    public List<String> prepareAllowedResolutionsList() {
        return Arrays.stream(allowedResolutionsString.split(";"))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}