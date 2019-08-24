package me.exrates.externalservice.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.dto.CandleDto;
import me.exrates.externalservice.dto.QuotesDto;
import me.exrates.externalservice.dto.ResolutionDto;
import me.exrates.externalservice.exceptions.api.ExratesApiException;
import me.exrates.externalservice.utils.KeyGeneratorUtil;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.exrates.externalservice.configurations.CacheConfiguration.CACHE_CANDLE_CHART_DATA;
import static me.exrates.externalservice.configurations.CacheConfiguration.CACHE_CURRENCY_PAIRS;
import static me.exrates.externalservice.configurations.CacheConfiguration.CACHE_TICKER_INFO;

@Slf4j
@Component
public class ExratesPublicApi {

    private static final String ALL = "All";
    private static final String DELIMITER = "/";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss");

    private final String url;

    private final Cache currencyPairsCache;
    private final Cache tickerInfoCache;
    private final Cache candleChartDataCache;
    private final RestTemplate restTemplate;

    @Autowired
    public ExratesPublicApi(@Value("${api.exrates.url}") String url,
                            @Qualifier(CACHE_CURRENCY_PAIRS) Cache currencyPairsCache,
                            @Qualifier(CACHE_TICKER_INFO) Cache tickerInfoCache,
                            @Qualifier(CACHE_CANDLE_CHART_DATA) Cache candleChartDataCache) {
        this.url = url;
        this.currencyPairsCache = currencyPairsCache;
        this.tickerInfoCache = tickerInfoCache;
        this.candleChartDataCache = candleChartDataCache;
        this.restTemplate = new RestTemplate();
    }

    public List<QuotesDto> getTickerInfoCached(@NotNull List<String> symbols) {
        return symbols.stream()
                .map(symbol -> tickerInfoCache.get(symbol, () -> getTickerInfo(symbol)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private QuotesDto getTickerInfo(@NotNull String symbol) {
        ResponseEntity<TickerResponse[]> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(String.format("%s/public/ticker?currency_pair=%s", url, convert(symbol)), TickerResponse[].class);
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
        return QuotesDto.of(symbol, Arrays.asList(body).get(0));
    }

    public List<CandleDto> getCandleChartDataCached(@NotNull String symbol, @NotNull ResolutionDto resolutionDto,
                                                    @NotNull LocalDateTime fromDate, @NotNull LocalDateTime toDate) {
        final String cacheKey = KeyGeneratorUtil.generate(DELIMITER,
                symbol,
                resolutionDto.toString(),
                fromDate.format(FORMATTER),
                toDate.format(FORMATTER));

        return candleChartDataCache.get(cacheKey, () -> getCandleChartData(symbol, resolutionDto, fromDate, toDate));
    }

    private List<CandleDto> getCandleChartData(@NotNull String symbol, @NotNull ResolutionDto resolutionDto,
                                               @NotNull LocalDateTime fromDate, @NotNull LocalDateTime toDate) {
        final String queryParams = buildQueryParams(fromDate, toDate, resolutionDto);

        ResponseEntity<CandleChartResponse[]> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(String.format("%s/public/%s/candle_chart?%s", url, convert(symbol), queryParams), CandleChartResponse[].class);
            if (responseEntity.getStatusCodeValue() != 200) {
                throw new ExratesApiException("Exrates server is not available");
            }
        } catch (Exception ex) {
            log.warn("Exrates service did not return valid data: server not available", ex);
            return null;
        }
        CandleChartResponse[] body = responseEntity.getBody();

        if (Objects.isNull(body)) {
            return null;
        }
        return Arrays.stream(body)
                .map(CandleDto::of)
                .collect(Collectors.toList());
    }

    private String buildQueryParams(LocalDateTime fromDate, LocalDateTime toDate, ResolutionDto resolutionDto) {
        String fromParam = String.format("from_date=%s", fromDate.format(DateTimeFormatter.ISO_DATE_TIME));
        String toParam = String.format("to_date=%s", toDate.format(DateTimeFormatter.ISO_DATE_TIME));
        String intervalValueParam = String.format("intervalValue=%s", String.valueOf(resolutionDto.getValue()));
        String intervalTypeParam = String.format("intervalType=%s", resolutionDto.getType().name());

        return String.join("&", fromParam, toParam, intervalValueParam, intervalTypeParam);
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

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class CandleChartResponse {

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime time;
        BigDecimal close;
        BigDecimal open;
        BigDecimal high;
        BigDecimal low;
        BigDecimal volume;
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
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build());
        CaffeineCache candleChartDataCache = new CaffeineCache(CACHE_CANDLE_CHART_DATA, Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build());
        ExratesPublicApi exratesPublicApi = new ExratesPublicApi("https://api.exrates.me/openapi/v1", currencyPairCache, tickerInfoCache, candleChartDataCache);
        List<String> list = new ArrayList<>();
        list.add("BTCUSD");
        list.add("ETHUSD");
        List<QuotesDto> btcusd = exratesPublicApi.getTickerInfoCached(list);
    }
}