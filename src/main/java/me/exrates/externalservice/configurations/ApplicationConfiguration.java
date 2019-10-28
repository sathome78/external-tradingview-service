package me.exrates.externalservice.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableAsync
@Configuration
public class ApplicationConfiguration {

    public static final String JSON_MAPPER = "jsonMapper";

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
}