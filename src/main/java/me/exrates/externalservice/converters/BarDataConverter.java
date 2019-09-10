package me.exrates.externalservice.converters;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.exrates.externalservice.api.models.Candle;
import me.exrates.externalservice.entities.enums.ResStatus;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@NoArgsConstructor(access = AccessLevel.NONE)
public final class BarDataConverter {

    public static Map<String, Object> convert(List<Candle> data) {
        data.sort(Comparator.comparing(Candle::getTime));

        List<Long> t = new ArrayList<>();
        List<BigDecimal> o = new ArrayList<>();
        List<BigDecimal> c = new ArrayList<>();
        List<BigDecimal> h = new ArrayList<>();
        List<BigDecimal> l = new ArrayList<>();
        List<BigDecimal> v = new ArrayList<>();

        data.forEach(candle -> {
            t.add(candle.getTime().toEpochSecond(ZoneOffset.UTC));
            o.add(candle.getOpen());
            h.add(candle.getHigh());
            l.add(candle.getLow());
            c.add(candle.getClose());
            v.add(candle.getVolume());
        });

        Map<String, Object> response = new HashMap<>();
        response.put("s", ResStatus.OK.getStatus());
        response.put("t", t);
        response.put("o", o);
        response.put("c", c);
        response.put("h", h);
        response.put("l", l);
        response.put("v", v);

        return response;
    }
}