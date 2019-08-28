package me.exrates.externalservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.dto.ResolutionDto;
import me.exrates.externalservice.entities.enums.ResolutionType;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class TimeUtil {

    public static LocalDateTime convert(long time) {
        return LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC);
    }

    public static int convertToMinutes(ResolutionDto resolutionDto) {
        final ResolutionType resolutionType = resolutionDto.getType();
        final int resolutionValue = resolutionDto.getValue();

        switch (resolutionType) {
            case MINUTE: {
                return resolutionValue;
            }
            case HOUR: {
                return resolutionValue * 60;
            }
            case DAY: {
                return resolutionValue * 24 * 60;
            }
            case WEEK: {
                return resolutionValue * 7 * 24 * 60;
            }
            case MONTH: {
                return resolutionValue * 30 * 7 * 24 * 60;
            }
            default: {
                throw new UnsupportedOperationException(String.format("Resolution type - %s not supported", resolutionType));
            }
        }
    }

    public static ResolutionDto getResolution(String resolution) {
        ResolutionType resolutionType;
        int resolutionValue;

        if (resolution.contains("H")) {
            resolutionType = ResolutionType.HOUR;
            resolutionValue = getValue(resolution, "H");
        } else if (resolution.contains("D")) {
            resolutionType = ResolutionType.DAY;
            resolutionValue = getValue(resolution, "D");
        } else if (resolution.contains("W")) {
            resolutionType = ResolutionType.WEEK;
            resolutionValue = getValue(resolution, "W");
        } else if (resolution.contains("M")) {
            resolutionType = ResolutionType.MONTH;
            resolutionValue = getValue(resolution, "M");
        } else {
            resolutionType = ResolutionType.MINUTE;
            resolutionValue = Integer.valueOf(resolution);
        }
        return new ResolutionDto(resolutionType, resolutionValue);
    }

    private static int getValue(String resolution, String type) {
        String strValue = resolution.replace(type, StringUtils.EMPTY);

        return strValue.equals(StringUtils.EMPTY)
                ? 1
                : Integer.valueOf(strValue);
    }
}