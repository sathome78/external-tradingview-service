package me.exrates.externalservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.model.enums.ResolutionType;
import org.apache.commons.lang3.StringUtils;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResolutionDto {

    private int value;
    private ResolutionType type;

    @Override
    public String toString() {
        return String.join(" ", String.valueOf(value), type.name());
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
        return new ResolutionDto(resolutionValue, resolutionType);
    }

    private static int getValue(String resolution, String type) {
        String strValue = resolution.replace(type, StringUtils.EMPTY);

        return strValue.equals(StringUtils.EMPTY)
                ? 1
                : Integer.valueOf(strValue);
    }
}