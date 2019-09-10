package me.exrates.externalservice.utils;

import me.exrates.externalservice.dto.ResolutionDto;
import me.exrates.externalservice.exceptions.UnsupportedResolutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

import static me.exrates.externalservice.configurations.ApplicationConfiguration.ALLOWED_RESOLUTIONS_LIST;

@Component
public class ResolutionUtil {

    private final List<String> allowedResolutions;

    @Autowired
    public ResolutionUtil(@Qualifier(ALLOWED_RESOLUTIONS_LIST) List<String> allowedResolutions) {
        this.allowedResolutions = allowedResolutions;
    }

    public void check(ResolutionDto resolutionDto) {
        allowedResolutions.stream()
                .filter(resolution -> resolution.equals(resolutionDto.toString()))
                .findFirst()
                .orElseThrow(() -> new UnsupportedResolutionException(resolutionDto.toString()));
    }
}