package me.exrates.externalservice.converters;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.exrates.externalservice.api.models.CandleResponse;
import me.exrates.externalservice.model.enums.ResStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@NoArgsConstructor(access = AccessLevel.NONE)
public final class BarDataConverter {

    public static Map<String, Object> convert(List<CandleResponse> data) {
        data.sort(Comparator.comparing(CandleResponse::getTime));

        List<Long> t = new ArrayList<>();
        List<BigDecimal> o = new ArrayList<>();
        List<BigDecimal> c = new ArrayList<>();
        List<BigDecimal> h = new ArrayList<>();
        List<BigDecimal> l = new ArrayList<>();
        List<BigDecimal> v = new ArrayList<>();

        data.forEach(candleResponse -> {
            t.add(candleResponse.getTime());
            o.add(candleResponse.getOpen());
            h.add(candleResponse.getHigh());
            l.add(candleResponse.getLow());
            c.add(candleResponse.getClose());
            v.add(candleResponse.getVolume());
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