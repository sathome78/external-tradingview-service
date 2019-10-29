package me.exrates.externalservice.controllers;

import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.model.QuotesDto;
import me.exrates.externalservice.model.enums.ResStatus;
import me.exrates.externalservice.services.CurrencyPairService;
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

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api")
public class DataIntegrationController {

    private final DataIntegrationService integrationService;
    private final CurrencyPairService currencyPairService;

    @Autowired
    public DataIntegrationController(DataIntegrationService integrationService,
                                     CurrencyPairService currencyPairService) {
        this.integrationService = integrationService;
        this.currencyPairService = currencyPairService;
    }

    @GetMapping(value = "/symbol_info", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getSymbolInfo() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> data = integrationService.getSymbolInfo();

            response.putAll(data);
        } catch (Exception ex) {
            response.put("s", ResStatus.ERROR.getStatus());
            response.put("errmsg", ex.getMessage());
        }
        return response;
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
                                          @RequestParam Long from,
                                          @RequestParam Long to,
                                          @RequestParam String resolution,
                                          @RequestParam(required = false) Integer countback) {
        final LocalDateTime fromDate = TimeUtil.convert(from);
        final LocalDateTime toDate = TimeUtil.convert(to);

        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> data = integrationService.getHistory(symbol, fromDate, toDate, countback, resolution);
            if (CollectionUtils.isEmpty(data)) {
                response.put("s", ResStatus.NO_DATA.getStatus());

                LocalDateTime lastCandleTimeBeforeDate = integrationService.getLastCandleTimeBeforeDate(symbol, fromDate, resolution);
                if (Objects.nonNull(lastCandleTimeBeforeDate)) {
                    response.put("nb", Timestamp.valueOf(lastCandleTimeBeforeDate).getTime());
                }
            } else {
                response.putAll(data);
            }
        } catch (Exception ex) {
            response.put("s", ResStatus.ERROR.getStatus());
            response.put("errmsg", ex.getMessage());
        }
        return response;
    }

    @GetMapping("/streaming")
    public DeferredResult<String> getStreamOfPrices(HttpServletResponse response) {
        response.addHeader("Transfer-Encoding", "chunked");

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
}