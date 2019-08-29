package me.exrates.externalservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.dto.ResolutionDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class QueryBuilderUtil {

    public static String build(LocalDateTime fromDate, LocalDateTime toDate, ResolutionDto resolutionDto) {
        String fromParam = String.format("from_date=%s", fromDate.format(DateTimeFormatter.ISO_DATE_TIME));
        String toParam = String.format("to_date=%s", toDate.format(DateTimeFormatter.ISO_DATE_TIME));
        String intervalValueParam = String.format("interval_value=%s", String.valueOf(resolutionDto.getValue()));
        String intervalTypeParam = String.format("interval_type=%s", resolutionDto.getType().name());

        return String.join("&", fromParam, toParam, intervalValueParam, intervalTypeParam);
    }

    public static String build(LocalDate fromDate, LocalDate toDate) {
        String fromParam = String.format("from_date=%s", fromDate.format(DateTimeFormatter.ISO_DATE));
        String toParam = String.format("to_date=%s", toDate.format(DateTimeFormatter.ISO_DATE));
        String directionParam = "direction=DESC";

        return String.join("&", fromParam, toParam, directionParam);
    }
}