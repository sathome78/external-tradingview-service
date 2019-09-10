package me.exrates.externalservice.converters;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.exrates.externalservice.entities.enums.ResStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@NoArgsConstructor(access = AccessLevel.NONE)
public final class SymbolInfoConverter {

    private static final String EXRATES = "EXRATES";

    public static Map<String, Object> convert(Map<String, String> pairs) {
        List<String> symbol = new ArrayList<>();
        List<String> description = new ArrayList<>();
        List<String> currency = new ArrayList<>();
        List<String> baseCurrency = new ArrayList<>();
        List<String> exchangeListed = new ArrayList<>();
        List<String> exchangeTraded = new ArrayList<>();
        List<Long> minmovement = new ArrayList<>();
        List<Long> pricescale = new ArrayList<>();
        List<String> type = new ArrayList<>();
        List<String> ticker = new ArrayList<>();
        List<String> timezone = new ArrayList<>();
        List<String> sessionRegular = new ArrayList<>();
        List<List<String>> supportedResolutions = new ArrayList<>();
        List<Boolean> hasDaily = new ArrayList<>();
        List<Boolean> barFillgaps = new ArrayList<>();

        List<String> resolutions = new ArrayList<>();
        resolutions.add("5");
        resolutions.add("15");
        resolutions.add("30");
        resolutions.add("60");
        resolutions.add("360");
        resolutions.add("D");

        pairs.forEach((key, value) -> {
            symbol.add(key);
            description.add(value);
            currency.add(value.split("/")[1]);
            baseCurrency.add(value.split("/")[0]);
            exchangeListed.add(EXRATES);
            exchangeTraded.add(EXRATES);
            minmovement.add(1L);
            pricescale.add(100_000_000L);
            type.add("crypto");
            ticker.add(key);
            timezone.add("Etc/UTC");
            sessionRegular.add("24x7");
            supportedResolutions.add(resolutions);
            hasDaily.add(true);
            barFillgaps.add(true);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("s", ResStatus.OK.getStatus());
        response.put("symbol", symbol);
        response.put("description", description);
        response.put("currency", currency);
        response.put("base-currency", baseCurrency);
        response.put("exchange-listed", exchangeListed);
        response.put("exchange-traded", exchangeTraded);
        response.put("minmovement", minmovement);
        response.put("pricescale", pricescale);
        response.put("type", type);
        response.put("ticker", ticker);
        response.put("timezone", timezone);
        response.put("session-regular", sessionRegular);
        response.put("supported-resolutions", supportedResolutions);
        response.put("has-daily", hasDaily);
        response.put("bar-fillgaps", barFillgaps);

        return response;
    }
}