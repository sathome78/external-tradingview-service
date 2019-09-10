package me.exrates.externalservice.services;

import me.exrates.externalservice.api.models.CandleChartResponse;
import me.exrates.externalservice.dto.QuotesDto;
import me.exrates.externalservice.dto.ResolutionDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public interface DataIntegrationService {

    List<QuotesDto> getQuotes(List<String> symbols);

    CandleChartResponse getHistory(String symbol, ResolutionDto resolutionDto, LocalDateTime fromDate, LocalDateTime toDate, Integer countback);

    String getLongPoolingResult();

    BlockingQueue<String> getBufferQueue();

    Map<String, String> getPairs();
}