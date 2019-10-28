package me.exrates.externalservice.utils;

import me.exrates.externalservice.exceptions.UnsupportedResolutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResolutionUtil {

    private final List<String> allowedResolutions;

    @Autowired
    public ResolutionUtil(@Value("#{'${application.allowed-resolutions}'.replaceAll(' ', '').split(',')}") List<String> allowedResolutions) {
        this.allowedResolutions = allowedResolutions;
    }

    public void check(String resolution) {
        boolean contains = allowedResolutions.contains(resolution);

        if (!contains) {
            throw new UnsupportedResolutionException(resolution);
        }
    }
}