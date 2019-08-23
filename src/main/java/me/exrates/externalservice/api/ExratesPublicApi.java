package me.exrates.externalservice.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.dto.QuotesDto;
import me.exrates.externalservice.exceptions.api.ExratesApiException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.exrates.externalservice.configurations.CacheConfiguration.CACHE_CURRENCY_PAIRS;
import static me.exrates.externalservice.configurations.CacheConfiguration.CACHE_TICKER_INFO;

@Slf4j
@Component
public class ExratesPublicApi {

    private static final String ALL = "All";
    private static final String DELIMITER = "/";

    private final String url;

    private final Cache currencyPairsCache;
    private final Cache tickerInfoCache;
    private final RestTemplate restTemplate;

    @Autowired
    public ExratesPublicApi(@Value("${api.exrates.url}") String url,
                            @Qualifier(CACHE_CURRENCY_PAIRS) Cache currencyPairsCache,
                            @Qualifier(CACHE_TICKER_INFO) Cache tickerInfoCache) {
        this.url = url;
        this.currencyPairsCache = currencyPairsCache;
        this.tickerInfoCache = tickerInfoCache;
        this.restTemplate = new RestTemplate();
    }

    public List<QuotesDto> getTickerInfoCached(@NotNull List<String> pairs) {
        return pairs.stream()
                .map(pair -> Pair.of(pair, tickerInfoCache.get(pair, () -> getTickerInfo(pair))))
                .filter(pair -> Objects.nonNull(pair.getValue()))
                .map(pair -> QuotesDto.of(pair.getKey(), pair.getValue()))
                .collect(Collectors.toList());
    }

    private TickerResponse getTickerInfo(@NotNull String pair) {
        ResponseEntity<TickerResponse[]> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(String.format("%s/public/ticker?currency_pair=%s", url, convert(pair)), TickerResponse[].class);
            if (responseEntity.getStatusCodeValue() != 200) {
                throw new ExratesApiException("Exrates server is not available");
            }
        } catch (Exception ex) {
            log.warn("Exrates service did not return valid data: server not available", ex);
            return null;
        }
        TickerResponse[] body = responseEntity.getBody();

        if (Objects.isNull(body)) {
            return null;
        }
        return Arrays.asList(body).get(0);
    }

    public Map<String, String> getCurrencyPairsCached() {
        return currencyPairsCache.get(ALL, this::getCurrencyPairs);
    }

    private Map<String, String> getCurrencyPairs() {
        ResponseEntity<CurrencyPairResponse[]> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(String.format("%s/public/currency_pairs", url), CurrencyPairResponse[].class);
            if (responseEntity.getStatusCodeValue() != 200) {
                throw new ExratesApiException("Exrates server is not available");
            }
        } catch (Exception ex) {
            log.warn("Exrates service did not return valid data: server not available", ex);
            return Collections.emptyMap();
        }
        CurrencyPairResponse[] body = responseEntity.getBody();

        if (Objects.isNull(body)) {
            return Collections.emptyMap();
        }
        return Stream.of(body)
                .map(currencyPairDto -> Pair.of(currencyPairDto.name.replace(DELIMITER, StringUtils.EMPTY), currencyPairDto.urlSymbol))
                .collect(Collectors.toMap(
                        Pair::getKey,
                        Pair::getValue));
    }

    private String convert(String pair) {
        return getCurrencyPairsCached().get(pair);
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class TickerResponse {

        Integer id;
        String name;
        BigDecimal last;
        BigDecimal lowestAsk;
        BigDecimal highestBid;
        BigDecimal percentChange;
        BigDecimal baseVolume;
        BigDecimal quoteVolume;
        BigDecimal high;
        BigDecimal low;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class CurrencyPairResponse {

        String name;
        @JsonProperty("url_symbol")
        String urlSymbol;
    }

    public static void main(String[] args) {
        CaffeineCache currencyPairCache = new CaffeineCache(CACHE_CURRENCY_PAIRS, Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build());
        CaffeineCache tickerInfoCache = new CaffeineCache(CACHE_TICKER_INFO, Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build());
        ExratesPublicApi exratesPublicApi = new ExratesPublicApi("https://api.exrates.me/openapi/v1", currencyPairCache, tickerInfoCache);
        List<String> list = new ArrayList<>();
        list.add("BTCUSD");
        list.add("ETHUSD");
        List<QuotesDto> btcusd = exratesPublicApi.getTickerInfoCached(list);
    }
}