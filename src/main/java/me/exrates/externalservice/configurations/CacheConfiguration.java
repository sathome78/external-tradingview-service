package me.exrates.externalservice.configurations;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfiguration {

    public final static String CACHE_CURRENCY_PAIRS = "cache.currency-pairs";
    public final static String CACHE_TICKER_INFO = "cache.ticker-info";

    @Bean(CACHE_CURRENCY_PAIRS)
    public Cache cacheCurrencyPairs() {
        return new CaffeineCache(CACHE_CURRENCY_PAIRS, Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build());
    }

    @Bean(CACHE_TICKER_INFO)
    public Cache cacheTickerInfo() {
        return new CaffeineCache(CACHE_TICKER_INFO, Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build());
    }
}