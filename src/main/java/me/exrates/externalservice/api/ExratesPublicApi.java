package me.exrates.externalservice.api;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.api.models.CandleChartResponse;
import me.exrates.externalservice.api.models.CurrencyPairResponse;
import me.exrates.externalservice.api.models.TickerResponse;
import me.exrates.externalservice.dto.QuotesDto;
import me.exrates.externalservice.dto.ResolutionDto;
import me.exrates.externalservice.entities.enums.ResolutionType;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss");

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
        ResponseEntity<TickerResponse[]> responseEntity = restTemplate.getForEntity(String.format("%s/public/ticker?currency_pair=%s", url, convert(symbol)), TickerResponse[].class);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExratesApiException("Exrates server is not available");
        }

        TickerResponse[] body = responseEntity.getBody();
        if (Objects.isNull(body)) {
            return null;
        }
        return QuotesDto.of(symbol, Arrays.asList(body).get(0));
    }

    public CandleChartResponse getCandleChartDataCached(@NotNull String symbol, @NotNull ResolutionDto resolutionDto,
                                                        @NotNull LocalDateTime fromDate, @NotNull LocalDateTime toDate) {
        final String cacheKey = KeyGeneratorUtil.generate(DELIMITER,
                symbol,
                resolutionDto.toString(),
                fromDate.format(FORMATTER),
                toDate.format(FORMATTER));

        return candleChartDataCache.get(cacheKey, () -> getCandleChartData(symbol, resolutionDto, fromDate, toDate));
    }

    private CandleChartResponse getCandleChartData(@NotNull String symbol, @NotNull ResolutionDto resolutionDto,
                                                   @NotNull LocalDateTime fromDate, @NotNull LocalDateTime toDate) {
        final String queryParams = buildQueryParams(fromDate, toDate, resolutionDto);

        ResponseEntity<CandleChartResponse> responseEntity = restTemplate.getForEntity(String.format("%s/public/%s/candle_chart?%s", url, convert(symbol), queryParams), CandleChartResponse.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExratesApiException("Exrates server is not available");
        }

        CandleChartResponse body = responseEntity.getBody();
        if (Objects.isNull(body)) {
            return null;
        }
        return body;
    }

    private String buildQueryParams(LocalDateTime fromDate, LocalDateTime toDate, ResolutionDto resolutionDto) {
        String fromParam = String.format("from_date=%s", fromDate.format(DateTimeFormatter.ISO_DATE_TIME));
        String toParam = String.format("to_date=%s", toDate.format(DateTimeFormatter.ISO_DATE_TIME));
        String intervalValueParam = String.format("interval_value=%s", String.valueOf(resolutionDto.getValue()));
        String intervalTypeParam = String.format("interval_type=%s", resolutionDto.getType().name());

        return String.join("&", fromParam, toParam, intervalValueParam, intervalTypeParam);
    }

    public Map<String, String> getCurrencyPairsCached() {
        return currencyPairsCache.get(ALL, this::getCurrencyPairs);
    }

    private Map<String, String> getCurrencyPairs() {
        ResponseEntity<CurrencyPairResponse[]> responseEntity = restTemplate.getForEntity(String.format("%s/public/currency_pairs", url), CurrencyPairResponse[].class);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExratesApiException("Exrates server is not available");
        }

        CurrencyPairResponse[] body = responseEntity.getBody();
        if (Objects.isNull(body)) {
            return Collections.emptyMap();
        }
        return Stream.of(body)
                .map(currencyPairDto -> Pair.of(currencyPairDto.getName().replace(DELIMITER, StringUtils.EMPTY), currencyPairDto.getUrlSymbol()))
                .collect(Collectors.toMap(
                        Pair::getKey,
                        Pair::getValue,
                        (v1, v2) -> v1));
    }

    private String convert(String pair) {
        return getCurrencyPairsCached().get(pair);
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
        ExratesPublicApi exratesPublicApi = new ExratesPublicApi("http://localhost:8080/openapi/v1", currencyPairCache, tickerInfoCache, candleChartDataCache);
//        List<String> list = new ArrayList<>();
//        list.add("BTCUSD");
//        list.add("ETHUSD");
//        List<QuotesDto> btcusd = exratesPublicApi.getTickerInfoCached(list);

        ResolutionDto resolutionDto = new ResolutionDto(ResolutionType.HOUR, 1);
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minus(2, ChronoUnit.YEARS);
        CandleChartResponse chartDataCached = exratesPublicApi.getCandleChartDataCached("BTCUSD", resolutionDto, from, to);
    }
}