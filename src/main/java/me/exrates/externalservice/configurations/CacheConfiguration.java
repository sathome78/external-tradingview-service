package me.exrates.externalservice.configurations;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfiguration {

    public final static String CACHE_CURRENCY_PAIRS = "cache.currency-pairs";
    public final static String CACHE_TICKER_INFO = "cache.ticker-info";
    public final static String CACHE_CANDLE_CHART_DATA = "cache.candle-chart-data";

    @Bean(CACHE_CURRENCY_PAIRS)
    public Cache cacheCurrencyPairs() {
        return new CaffeineCache(CACHE_CURRENCY_PAIRS, Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build());
    }

    @Bean(CACHE_TICKER_INFO)
    public Cache cacheTickerInfo() {
        return new CaffeineCache(CACHE_TICKER_INFO, Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build());
    }

    @Bean(CACHE_CANDLE_CHART_DATA)
    public Cache cacheCandleChartData() {
        return new CaffeineCache(CACHE_CANDLE_CHART_DATA, Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build());
    }
}