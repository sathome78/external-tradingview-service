package me.exrates.externalservice.properties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.json.Json;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class SymbolInfoProperty {

    private static final String EXRATES = "EXRATES";

    public static String get(String symbol) {
        return Json.createObjectBuilder()
                .add("name", symbol)
                .add("base_name", Json.createArrayBuilder()
                        .add(symbol)
                        .build())
                .add("description", symbol)
                .add("full_name", symbol)
                .add("has_seconds", false)
                .add("has_intraday", true)
                .add("has_no_volume", false)
                .add("listed_exchange", EXRATES)
                .add("exchange", EXRATES)
                .add("minmov", 1)
                .add("fractional", false)
                .add("pricescale", 1_000_000_000)
                .add("type", "bitcoin")
                .add("session", "24x7")
                .add("ticker", symbol)
                .add("timezone", "Etc/UTC")
                .add("supported_resolutions", Json.createArrayBuilder()
                        .add("5")
                        .add("15")
                        .add("30")
                        .add("60")
                        .add("360")
                        .add("D")
                        .build())
                .add("force_session_rebuild", false)
                .add("has_daily", true)
                .add("has_weekly_and_monthly", false)
                .add("has_empty_bars", true)
                .add("volume_precision", 2)
                .build()
                .toString();
    }
}