package me.exrates.externalservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.model.ResolutionDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class QueryBuilderUtil {

    public static String build(String pairName, LocalDateTime fromDate, LocalDateTime toDate, String resolution) {
        List<String> params = new ArrayList<>();

        if (nonNull(pairName)) {
            params.add(String.format("currencyPair=%s", pairName));
        }
        if (nonNull(fromDate)) {
            params.add(String.format("from=%s", fromDate.format(DateTimeFormatter.ISO_DATE_TIME)));
        }
        if (nonNull(toDate)) {
            params.add(String.format("to=%s", toDate.format(DateTimeFormatter.ISO_DATE_TIME)));
        }
        if (nonNull(resolution)) {
            params.add(String.format("resolution=%s", resolution));
        }
        return String.join("&", params);
    }
}