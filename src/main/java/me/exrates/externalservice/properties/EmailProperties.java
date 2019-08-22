package me.exrates.externalservice.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "application.email")
public class EmailProperties {

    private String address;
    private String name;
    private String titlePrefix;
}