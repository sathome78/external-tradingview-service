package me.exrates.externalservice.services;

import me.exrates.externalservice.dto.CandleDto;
import me.exrates.externalservice.dto.QuotesDto;

import java.util.List;

public interface DataIntegrationService {

    List<QuotesDto> getQuotes(List<String> symbols);

    List<CandleDto> getHistory(String symbol, String resolution, long from, long to, int countback);
}