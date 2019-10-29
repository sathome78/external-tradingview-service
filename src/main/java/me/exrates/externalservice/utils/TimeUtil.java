package me.exrates.externalservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.model.ResolutionDto;
import me.exrates.externalservice.model.enums.ResolutionType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class TimeUtil {

    public static LocalDateTime convert(long time) {
        return LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC);
    }

    public static int convertToMinutes(String resolution) {
        final ResolutionDto resolutionDto = ResolutionDto.getResolution(resolution);
        int value = resolutionDto.getValue();
        ResolutionType type = resolutionDto.getType();

        switch (type) {
            case MINUTE: {
                return value;
            }
            case HOUR: {
                return value * 60;
            }
            case DAY: {
                return value * 24 * 60;
            }
            case WEEK: {
                return value * 7 * 24 * 60;
            }
            case MONTH: {
                return value * 30 * 7 * 24 * 60;
            }
            default: {
                throw new UnsupportedOperationException(String.format("Resolution type - %s not supported", type));
            }
        }
    }
}