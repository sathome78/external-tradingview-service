package me.exrates.externalservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.dto.ResolutionDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class QueryBuilderUtil {

    public static String build(LocalDateTime fromDate, LocalDateTime toDate, ResolutionDto resolutionDto) {
        List<String> params = new ArrayList<>();

        if (nonNull(fromDate)) {
            params.add(String.format("from_date=%s", fromDate.format(DateTimeFormatter.ISO_DATE_TIME)));
        }
        if (nonNull(toDate)) {
            params.add(String.format("to_date=%s", toDate.format(DateTimeFormatter.ISO_DATE_TIME)));
        }
        if (nonNull(resolutionDto)) {
            params.add(String.format("interval_value=%s", String.valueOf(resolutionDto.getValue())));
            params.add(String.format("interval_type=%s", resolutionDto.getType().name()));
        }
        return String.join("&", params);
    }
}