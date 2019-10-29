package me.exrates.externalservice.services;

import me.exrates.externalservice.model.QuotesDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public interface DataIntegrationService {

    Map<String, Object> getSymbolInfo();

    List<QuotesDto> getQuotes(List<String> symbols);

    Map<String, Object> getHistory(String symbol, Long from, Long to, Integer countback, String resolution);

    LocalDateTime getLastCandleTimeBeforeDate(String symbol, Long date, String resolution);

    String getLongPoolingResult();

    BlockingQueue<String> getBufferQueue();
}