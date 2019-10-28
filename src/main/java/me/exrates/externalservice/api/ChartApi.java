package me.exrates.externalservice.api;

import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.api.models.CandleResponse;
import me.exrates.externalservice.api.models.TickerResponse;
import me.exrates.externalservice.exceptions.api.ChartApiException;
import me.exrates.externalservice.utils.QueryBuilderUtil;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static me.exrates.externalservice.configurations.CacheConfiguration.TICKER_DATA_CACHE;

@Slf4j
@Component
public class ChartApi {

    private final String url;
    private final String coinmarketcapUrl;

    private final RestTemplate restTemplate;
    private final Cache tickerDataCache;

    @Autowired
    public ChartApi(@Value("${api.chart.url}") String url,
                    @Value("${api.chart.coinmarketcap-url}") String coinmarketcapUrl,
                    @Qualifier(TICKER_DATA_CACHE) Cache tickerDataCache) {
        this.url = url;
        this.coinmarketcapUrl = coinmarketcapUrl;
        this.tickerDataCache = tickerDataCache;
        this.restTemplate = new RestTemplate(getClientHttpRequestFactory());
    }

    public TickerResponse getCachedTickerInfo(@NotNull String symbol) {
        return tickerDataCache.get(symbol, () -> this.getTickerInfo(symbol));
    }

    public TickerResponse getTickerInfo(@NotNull String symbol) {
        final String resolution = "D"; //1 day

        final String queryParams = QueryBuilderUtil.build(symbol, null, null, resolution);

        ResponseEntity<TickerResponse[]> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(String.format("%s?%s", coinmarketcapUrl, queryParams), TickerResponse[].class);
            if (responseEntity.getStatusCodeValue() != 200) {
                throw new ChartApiException("Chart server is not available");
            }
        } catch (Exception ex) {
            log.warn("Chart service did not return valid data: server not available");
            return null;
        }
        TickerResponse[] body = responseEntity.getBody();
        if (Objects.isNull(body) || body.length == 0) {
            return null;
        }
        return body[0];
    }

    public List<CandleResponse> getCandleChartData(@NotNull String symbol, @NotNull LocalDateTime fromDate,
                                                   @NotNull LocalDateTime toDate, @NotNull String resolution) {
        final String queryParams = QueryBuilderUtil.build(symbol, fromDate, toDate, resolution);

        ResponseEntity<CandleResponse[]> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(String.format("%s/range?%s", url, queryParams), CandleResponse[].class);
            if (responseEntity.getStatusCodeValue() != 200) {
                throw new ChartApiException("Chart server is not available");
            }
        } catch (Exception ex) {
            log.warn("Chart service did not return valid data: server not available");
            return Collections.emptyList();
        }
        CandleResponse[] body = responseEntity.getBody();
        if (Objects.isNull(body) || body.length == 0) {
            return null;
        }
        return Arrays.asList(body);
    }

    public LocalDateTime getLastCandleTimeBeforeDate(@NotNull String symbol, @NotNull LocalDateTime toDate,
                                                     @NotNull String resolution) {
        final String queryParams = QueryBuilderUtil.build(symbol, null, toDate, resolution);

        ResponseEntity<LocalDateTime> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(String.format("%s/last-date?%s", url, queryParams), LocalDateTime.class);
            if (responseEntity.getStatusCodeValue() != 200) {
                throw new ChartApiException("Chart server is not available");
            }
        } catch (Exception ex) {
            log.warn("Chart service did not return valid data: server not available");
            return null;
        }
        return responseEntity.getBody();
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setHttpClient(httpClient);
        httpRequestFactory.setConnectTimeout(10000);
        httpRequestFactory.setConnectionRequestTimeout(10000);
        httpRequestFactory.setReadTimeout(10000);

        return httpRequestFactory;
    }
}