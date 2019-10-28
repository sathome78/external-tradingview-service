package me.exrates.externalservice.services;

import me.exrates.externalservice.api.models.CandleResponse;
import me.exrates.externalservice.model.QuotesDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public interface DataIntegrationService {

    List<QuotesDto> getQuotes(List<String> symbols);

    Map<String, Object> getHistory(String symbol, LocalDateTime fromDate, LocalDateTime toDate, Integer countback, String resolution);

    LocalDateTime getLastCandleTimeBeforeDate(String symbol, LocalDateTime date, String resolution);

    String getLongPoolingResult();

    BlockingQueue<String> getBufferQueue();
}