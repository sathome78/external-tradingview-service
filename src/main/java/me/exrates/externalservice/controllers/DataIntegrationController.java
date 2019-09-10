package me.exrates.externalservice.controllers;

import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.api.models.Candle;
import me.exrates.externalservice.api.models.CandleChartResponse;
import me.exrates.externalservice.converters.BarDataConverter;
import me.exrates.externalservice.converters.SymbolInfoConverter;
import me.exrates.externalservice.dto.QuotesDto;
import me.exrates.externalservice.dto.ResolutionDto;
import me.exrates.externalservice.entities.enums.ResStatus;
import me.exrates.externalservice.services.DataIntegrationService;
import me.exrates.externalservice.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static me.exrates.externalservice.api.ExratesPublicApi.FORMATTER;

@Slf4j
@RestController
@RequestMapping("/api")
public class DataIntegrationController {

    private final DataIntegrationService integrationService;

    @Autowired
    public DataIntegrationController(DataIntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping(value = "/quotes", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getQuotes(@RequestParam List<String> symbols) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<QuotesDto> data = integrationService.getQuotes(symbols);

            response.put("s", ResStatus.OK.getStatus());
            response.put("d", data);
        } catch (Exception ex) {
            response.put("s", ResStatus.ERROR.getStatus());
            response.put("errmsg", ex.getMessage());
        }
        return response;
    }

    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getHistory(@RequestParam String symbol,
                                          @RequestParam String resolution,
                                          @RequestParam Long from,
                                          @RequestParam Long to,
                                          @RequestParam(required = false) Integer countback) {
        final ResolutionDto resolutionDto = TimeUtil.getResolution(resolution);
        final LocalDateTime fromDate = TimeUtil.convert(from);
        final LocalDateTime toDate = TimeUtil.convert(to);

        Map<String, Object> response = new HashMap<>();

        try {
            CandleChartResponse data = integrationService.getHistory(symbol, resolutionDto, fromDate, toDate, countback);

            List<Candle> candlesList = data.getBody();
            if (CollectionUtils.isEmpty(candlesList)) {
                response.put("s", ResStatus.NO_DATA.getStatus());

                List<String> errors = data.getErrors();
                if (Objects.nonNull(errors.get(0))) {
                    response.put("nb", FORMATTER.parse(errors.get(0)));
                }
            } else {
                response.putAll(BarDataConverter.convert(candlesList));
            }
        } catch (Exception ex) {
            response.put("s", ResStatus.ERROR.getStatus());
            response.put("errmsg", ex.getMessage());
        }
        return response;
    }

    @GetMapping("/streaming")
    public DeferredResult<String> getStreamOfPrices() {
        DeferredResult<String> deferredResult = new DeferredResult<>();

        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);

                deferredResult.setResult(integrationService.getLongPoolingResult());
            } catch (Exception ex) {
                log.error("Interrupted exception occurred");
            }
        });
        return deferredResult;
    }

    @GetMapping(value = "/symbol_info", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getSymbolInfo() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, String> pairs = integrationService.getPairs()
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().replace("_", "/").toUpperCase()));

            response.putAll(SymbolInfoConverter.convert(pairs));
        } catch (Exception ex) {
            response.put("s", ResStatus.ERROR.getStatus());
            response.put("errmsg", ex.getMessage());
        }
        return response;
    }
}