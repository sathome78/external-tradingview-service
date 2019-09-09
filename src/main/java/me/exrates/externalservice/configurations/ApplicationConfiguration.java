package me.exrates.externalservice.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    public static final String JSON_MAPPER = "jsonMapper";

    public static final String ALLOWED_RESOLUTIONS_LIST = "allowedIntervalsList";

    @Value("${application.allowed-resolutions}")
    private String allowedResolutionsString;

    @Bean(JSON_MAPPER)
    public ObjectMapper mapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .registerModule(new JavaTimeModule());
    }

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