package me.exrates.externalservice.services;

import me.exrates.externalservice.api.models.CandleChartResponse;
import me.exrates.externalservice.dto.QuotesDto;

import java.util.List;

public interface DataIntegrationService {

    List<QuotesDto> getQuotes(List<String> symbols);

    CandleChartResponse getHistory(String symbol, String resolution, long from, long to, int countback);
}