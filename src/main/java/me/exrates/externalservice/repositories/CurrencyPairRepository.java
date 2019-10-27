package me.exrates.externalservice.repositories;

import me.exrates.externalservice.model.CurrencyPairDto;

import java.util.List;

public interface CurrencyPairRepository {

    List<CurrencyPairDto> getAllCurrencyPairs();
}