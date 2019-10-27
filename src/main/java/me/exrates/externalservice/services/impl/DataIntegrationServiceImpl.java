package me.exrates.externalservice.services.impl;

import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.api.ChartApi;
import me.exrates.externalservice.api.models.CandleResponse;
import me.exrates.externalservice.converters.BarDataConverter;
import me.exrates.externalservice.model.QuotesDto;
import me.exrates.externalservice.model.ResolutionDto;
import me.exrates.externalservice.services.DataIntegrationService;
import me.exrates.externalservice.utils.ResolutionUtil;
import me.exrates.externalservice.utils.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private final int resultSize;

    private final ChartApi chartApi;
    private final ResolutionUtil resolutionUtil;

    @Autowired
    public DataIntegrationServiceImpl(@Value("${stream.result.size:100}") int resultSize,
                                      ChartApi chartApi,
                                      ResolutionUtil resolutionUtil) {
        this.resultSize = resultSize;
        this.chartApi = chartApi;
        this.resolutionUtil = resolutionUtil;
    }

    @Override
    public List<QuotesDto> getQuotes(Map<String, String> pairs) {
        return pairs.entrySet().stream()
                .map(entry -> QuotesDto.of(entry.getKey(), chartApi.getCachedTickerInfo(entry.getValue())))
                .filter(quotesDto -> Objects.nonNull(quotesDto.getPrice()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getHistory(String symbol, ResolutionDto resolutionDto, LocalDateTime fromDate, LocalDateTime toDate, Integer countback) {
        resolutionUtil.check(resolutionDto);

        if (Objects.nonNull(countback)) {
            fromDate = toDate.minusMinutes(countback * TimeUtil.convertToMinutes(resolutionDto));
        }
        List<CandleResponse> data = chartApi.getCandleChartData(symbol, resolutionDto, fromDate, toDate);

        return BarDataConverter.convert(data);
    }

    @Override
    public LocalDateTime getLastCandleTimeBeforeDate(String symbol, LocalDateTime date, ResolutionDto resolutionDto) {
        return chartApi.getLastCandleTimeBeforeDate(symbol, date, resolutionDto);
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
}