package me.exrates.externalservice.services;

import me.exrates.externalservice.model.QuotesDto;
import me.exrates.externalservice.model.ResolutionDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public interface DataIntegrationService {

    List<QuotesDto> getQuotes(Map<String, String> pairs);

    Map<String, Object> getHistory(String symbol, ResolutionDto resolutionDto, LocalDateTime fromDate, LocalDateTime toDate, Integer countback);

    LocalDateTime getLastCandleTimeBeforeDate(String symbol, LocalDateTime date, ResolutionDto resolutionDto);

    String getLongPoolingResult();

    BlockingQueue<String> getBufferQueue();
}