package me.exrates.externalservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.model.ResolutionDto;
import me.exrates.externalservice.model.enums.ResolutionType;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class TimeUtil {

    public static int convertToSeconds(String resolution) {
        final ResolutionDto resolutionDto = ResolutionDto.getResolution(resolution);
        int value = resolutionDto.getValue();
        ResolutionType type = resolutionDto.getType();

        switch (type) {
            case MINUTE: {
                return value * 60;
            }
            case HOUR: {
                return value * 60 * 60;
            }
            case DAY: {
                return value * 24 * 60 * 60;
            }
            case WEEK: {
                return value * 7 * 24 * 60 * 60;
            }
            case MONTH: {
                return value * 30 * 7 * 24 * 60 * 60;
            }
            default: {
                throw new UnsupportedOperationException(String.format("Resolution type - %s not supported", type));
            }
        }
    }
}