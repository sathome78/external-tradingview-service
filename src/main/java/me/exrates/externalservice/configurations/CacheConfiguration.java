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

    public final static String CACHE_ACTIVE_CURRENCY_PAIRS = "cache.active-currency-pairs";
    public static final String TICKER_DATA_CACHE = "cache.ticker-data";

    @Bean(CACHE_ACTIVE_CURRENCY_PAIRS)
    public Cache cacheActiveCurrencyPairs() {
        return new CaffeineCache(CACHE_ACTIVE_CURRENCY_PAIRS, Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build());
    }

    @Bean(TICKER_DATA_CACHE)
    public Cache cacheTickerData() {
        return new CaffeineCache(TICKER_DATA_CACHE, Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build());
    }
}