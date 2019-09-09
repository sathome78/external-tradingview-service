package me.exrates.externalservice.services.impl;

import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.api.ExratesPublicApi;
import me.exrates.externalservice.api.models.CandleChartResponse;
import me.exrates.externalservice.dto.QuotesDto;
import me.exrates.externalservice.dto.ResolutionDto;
import me.exrates.externalservice.exceptions.UnsupportedResolutionException;
import me.exrates.externalservice.services.DataIntegrationService;
import me.exrates.externalservice.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static me.exrates.externalservice.configurations.ApplicationConfiguration.ALLOWED_RESOLUTIONS_LIST;

@Slf4j
@Service
public class DataIntegrationServiceImpl implements DataIntegrationService {

    private final List<String> allowedResolutions;

    private final ExratesPublicApi publicApi;

    @Autowired
    public DataIntegrationServiceImpl(@Qualifier(ALLOWED_RESOLUTIONS_LIST) List<String> allowedResolutions,
                                      ExratesPublicApi publicApi) {
        this.allowedResolutions = allowedResolutions;
        this.publicApi = publicApi;
    }

    @Override
    public List<QuotesDto> getQuotes(List<String> symbols) {
        return symbols.stream()
                .map(symbol -> QuotesDto.of(symbol, publicApi.getTickerInfoCached(symbol)))
                .filter(quotesDto -> Objects.nonNull(quotesDto.getPrice()))
                .collect(Collectors.toList());
    }

    @Override
    public CandleChartResponse getHistory(String symbol, String resolution, long from, long to, int countback) {
        ResolutionDto resolutionDto = TimeUtil.getResolution(resolution);

        checkResolution(resolutionDto);

        LocalDateTime fromDate = TimeUtil.convert(from);
        LocalDateTime toDate = TimeUtil.convert(to);

        if (Objects.nonNull(countback)) {
            fromDate = toDate.minusMinutes(countback * TimeUtil.convertToMinutes(resolutionDto));
        }

        return publicApi.getCandleChartDataCached(symbol, resolutionDto, fromDate, toDate);
    }

    @Override
    public Map<String, String> getCurrencyPairs() {
        return publicApi.getCurrencyPairsCached();
    }

    private void checkResolution(ResolutionDto resolutionDto) {
        allowedResolutions.stream()
                .filter(resolution -> resolution.equals(resolutionDto.toString()))
                .findFirst()
                .orElseThrow(() -> new UnsupportedResolutionException(resolutionDto.toString()));
    }
}