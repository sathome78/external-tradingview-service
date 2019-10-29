package me.exrates.externalservice.services.impl;

import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.repositories.CurrencyPairRepository;
import me.exrates.externalservice.services.CurrencyPairService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

import static me.exrates.externalservice.configurations.CacheConfiguration.CACHE_ACTIVE_CURRENCY_PAIRS;

@Slf4j
@Transactional
@Service
public class CurrencyPairServiceImpl implements CurrencyPairService {

    private static final String ALL = "All";

    private static final String DELIMITER_1 = "/";

    private final CurrencyPairRepository currencyPairRepository;
    private final Cache currencyPairsCache;

    @Autowired
    public CurrencyPairServiceImpl(CurrencyPairRepository currencyPairRepository,
                                   @Qualifier(CACHE_ACTIVE_CURRENCY_PAIRS) Cache currencyPairsCache) {
        this.currencyPairRepository = currencyPairRepository;
        this.currencyPairsCache = currencyPairsCache;
    }

    @Override
    public Map<String, String> getCachedActiveCurrencyPairs() {
        return currencyPairsCache.get(ALL, this::getActiveCurrencyPairs);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<String, String> getActiveCurrencyPairs() {
        return currencyPairRepository.getAllCurrencyPairs().stream()
                .filter(dto -> !dto.isHidden())
                .map(dto -> Pair.of(dto.getName().replace(DELIMITER_1, StringUtils.EMPTY), dto.getName()))
                .collect(Collectors.toMap(
                        Pair::getKey,
                        Pair::getValue,
                        (v1, v2) -> v1));
    }
}