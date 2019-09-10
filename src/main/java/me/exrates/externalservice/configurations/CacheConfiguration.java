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

    @Bean(CACHE_CURRENCY_PAIRS)
    public Cache cacheCurrencyPairs() {
        return new CaffeineCache(CACHE_CURRENCY_PAIRS, Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build());
    }
}