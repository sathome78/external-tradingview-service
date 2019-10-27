package me.exrates.externalservice.services;

import java.util.Map;

public interface CurrencyPairService {

    Map<String, String> getCachedActiveCurrencyPairs();

    Map<String, String> getActiveCurrencyPairs();
}