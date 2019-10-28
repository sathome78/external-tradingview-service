package me.exrates.externalservice.services.impl;

import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.api.ChartApi;
import me.exrates.externalservice.api.models.CandleResponse;
import me.exrates.externalservice.converters.BarDataConverter;
import me.exrates.externalservice.model.QuotesDto;
import me.exrates.externalservice.services.CurrencyPairService;
import me.exrates.externalservice.services.DataIntegrationService;
import me.exrates.externalservice.utils.ResolutionUtil;
import me.exrates.externalservice.utils.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
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

    private final int resultSize;

    private final CurrencyPairService currencyPairService;
    private final ChartApi chartApi;
    private final ResolutionUtil resolutionUtil;

    @Autowired
    public DataIntegrationServiceImpl(@Value("${stream.result.size:100}") int resultSize,
                                      CurrencyPairService currencyPairService,
                                      ChartApi chartApi,
                                      ResolutionUtil resolutionUtil) {
        this.resultSize = resultSize;
        this.currencyPairService = currencyPairService;
        this.chartApi = chartApi;
        this.resolutionUtil = resolutionUtil;
    }

    @Override
    public List<QuotesDto> getQuotes(List<String> symbols) {
        return symbols.stream()
                .map(symbol -> Pair.of(symbol, convert(symbol)))
                .filter(pair -> Objects.nonNull(pair.getValue()))
                .map(pair -> QuotesDto.of(pair.getKey(), chartApi.getCachedTickerInfo(pair.getValue())))
                .filter(quotesDto -> Objects.nonNull(quotesDto.getPrice()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getHistory(String symbol, LocalDateTime fromDate, LocalDateTime toDate, Integer countback, String resolution) {
        final String convertedSymbol = convert(symbol);
        if (Objects.isNull(convertedSymbol)) {
            return Collections.emptyMap();
        }
        resolutionUtil.check(resolution);

        if (Objects.nonNull(countback)) {
            fromDate = toDate.minusMinutes(countback * TimeUtil.convertToMinutes(resolution));
        }
        List<CandleResponse> data = chartApi.getCandleChartData(convertedSymbol, fromDate, toDate, resolution);

        return BarDataConverter.convert(data);
    }

    @Override
    public LocalDateTime getLastCandleTimeBeforeDate(String symbol, LocalDateTime date, String resolution) {
        final String convertedSymbol = convert(symbol);
        if (Objects.isNull(convertedSymbol)) {
            return null;
        }
        return chartApi.getLastCandleTimeBeforeDate(convertedSymbol, date, resolution);
    }

    @Override
    public String getLongPoolingResult() {
        StringBuilder result = new StringBuilder(StringUtils.EMPTY);

        int size = 0;
        while (!bufferQueue.isEmpty() && size <= resultSize) {
            try {
                result.append(bufferQueue.take());
                result.append("\n");

                size++;
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

    private String convert(String symbol) {
        return currencyPairService.getCachedActiveCurrencyPairs().get(symbol);
    }
}