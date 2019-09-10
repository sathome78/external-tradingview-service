package me.exrates.externalservice.services.impl;

import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.api.ExratesPublicApi;
import me.exrates.externalservice.api.models.CandleChartResponse;
import me.exrates.externalservice.dto.QuotesDto;
import me.exrates.externalservice.dto.ResolutionDto;
import me.exrates.externalservice.services.DataIntegrationService;
import me.exrates.externalservice.utils.ResolutionUtil;
import me.exrates.externalservice.utils.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataIntegrationServiceImpl implements DataIntegrationService {

    private final BlockingQueue<String> bufferQueue = new LinkedBlockingDeque<>();

    private final ExratesPublicApi publicApi;
    private final ResolutionUtil resolutionUtil;

    @Autowired
    public DataIntegrationServiceImpl(ExratesPublicApi publicApi,
                                      ResolutionUtil resolutionUtil) {
        this.publicApi = publicApi;
        this.resolutionUtil = resolutionUtil;
    }

    @Override
    public List<QuotesDto> getQuotes(List<String> symbols) {
        return symbols.stream()
                .map(symbol -> QuotesDto.of(symbol, publicApi.getTickerInfoCached(symbol)))
                .filter(quotesDto -> Objects.nonNull(quotesDto.getPrice()))
                .collect(Collectors.toList());
    }

    @Override
    public CandleChartResponse getHistory(String symbol, ResolutionDto resolutionDto, LocalDateTime fromDate, LocalDateTime toDate, Integer countback) {
        resolutionUtil.check(resolutionDto);

        if (Objects.nonNull(countback)) {
            fromDate = toDate.minusMinutes(countback * TimeUtil.convertToMinutes(resolutionDto));
        }
        return publicApi.getCandleChartDataCached(symbol, resolutionDto, fromDate, toDate);
    }

    @Override
    public String getLongPoolingResult() {
        StringBuilder result = new StringBuilder(StringUtils.EMPTY);

        while (!bufferQueue.isEmpty()) {
            try {
                result.append(bufferQueue.take());
                result.append("\n");
            } catch (InterruptedException ex) {
                log.error("Interrupted exception occurred");
            }
        }
        return result.toString();
    }

    @Override
    public BlockingQueue<String> getBufferQueue() {
        return bufferQueue;
    }

    @Override
    public Map<String, String> getPairs() {
        return publicApi.getCurrencyPairsCached();
    }
}