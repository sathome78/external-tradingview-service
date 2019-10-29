package me.exrates.externalservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class QueryBuilderUtil {

    public static String build(String pairName, Long from, Long to, String resolution) {
        List<String> params = new ArrayList<>();

        if (nonNull(pairName)) {
            params.add(String.format("currencyPair=%s", pairName));
        }
        if (nonNull(from)) {
            params.add(String.format("from=%s", String.valueOf(from)));
        }
        if (nonNull(to)) {
            params.add(String.format("to=%s", String.valueOf(to)));
        }
        if (nonNull(resolution)) {
            params.add(String.format("resolution=%s", resolution));
        }
        return String.join("&", params);
    }
}