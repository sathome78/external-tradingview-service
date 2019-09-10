package me.exrates.externalservice.api;

import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.api.models.CandleChartResponse;
import me.exrates.externalservice.api.models.CurrencyPairResponse;
import me.exrates.externalservice.api.models.OrderBookResponse;
import me.exrates.externalservice.api.models.TickerResponse;
import me.exrates.externalservice.api.models.TradeHistoryResponse;
import me.exrates.externalservice.dto.ResolutionDto;
import me.exrates.externalservice.exceptions.api.ExratesApiException;
import me.exrates.externalservice.utils.QueryBuilderUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.exrates.externalservice.configurations.CacheConfiguration.CACHE_CURRENCY_PAIRS;

@Slf4j
@Component
public class ExratesPublicApi {

    private static final String ALL = "All";

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss");

    private final String url;

    private final Cache currencyPairsCache;
    private final RestTemplate restTemplate;

    @Autowired
    public ExratesPublicApi(@Value("${api.exrates.url}") String url,
                            @Qualifier(CACHE_CURRENCY_PAIRS) Cache currencyPairsCache) {
        this.url = url;
        this.currencyPairsCache = currencyPairsCache;
        this.restTemplate = new RestTemplate();
    }

    public TickerResponse getTickerInfo(@NotNull String symbol) {
        ResponseEntity<TickerResponse[]> responseEntity = restTemplate.getForEntity(String.format("%s/public/ticker?currency_pair=%s", url, convert(symbol)), TickerResponse[].class);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExratesApiException("Exrates server is not available");
        }

        TickerResponse[] body = responseEntity.getBody();
        if (Objects.isNull(body)) {
            return null;
        }
        return Arrays.asList(body).get(0);
    }

    public CandleChartResponse getCandleChartData(@NotNull String symbol, @NotNull ResolutionDto resolutionDto,
                                                  @NotNull LocalDateTime fromDate, @NotNull LocalDateTime toDate) {
        final String queryParams = QueryBuilderUtil.build(fromDate, toDate, resolutionDto);

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

    public OrderBookResponse getOrderBook(@NotNull String symbol) {
        ResponseEntity<OrderBookResponse> responseEntity = restTemplate.getForEntity(String.format("%s/public/orderbook/%s", url, convert(symbol)), OrderBookResponse.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExratesApiException("Exrates server is not available");
        }

        OrderBookResponse body = responseEntity.getBody();
        if (Objects.isNull(body)) {
            return null;
        }
        return body;
    }

    public TradeHistoryResponse getTradeHistory(@NotNull String symbol, @NotNull LocalDate fromDate, @NotNull LocalDate toDate) {
        final String queryParams = QueryBuilderUtil.build(fromDate, toDate);

        ResponseEntity<TradeHistoryResponse> responseEntity = restTemplate.getForEntity(String.format("%s/public/history/%s?%s", url, convert(symbol), queryParams), TradeHistoryResponse.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExratesApiException("Exrates server is not available");
        }

        TradeHistoryResponse body = responseEntity.getBody();
        if (Objects.isNull(body)) {
            return null;
        }
        return body;
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
                .map(currencyPairDto -> Pair.of(currencyPairDto.getName().replace("/", StringUtils.EMPTY), currencyPairDto.getUrlSymbol()))
                .collect(Collectors.toMap(
                        Pair::getKey,
                        Pair::getValue,
                        (v1, v2) -> v1));
    }

    private String convert(String pair) {
        return getCurrencyPairsCached().get(pair);
    }
}