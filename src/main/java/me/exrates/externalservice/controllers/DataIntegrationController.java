package me.exrates.externalservice.controllers;

import me.exrates.externalservice.converters.BarDataConverter;
import me.exrates.externalservice.dto.CandleDto;
import me.exrates.externalservice.dto.QuotesDto;
import me.exrates.externalservice.entities.enums.ResStatus;
import me.exrates.externalservice.services.DataIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

@RestController
@RequestMapping("/api")
public class DataIntegrationController {

    private final DataIntegrationService integrationService;

    @Autowired
    public DataIntegrationController(DataIntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping(value = "/quotes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getQuotes(@RequestParam List<String> symbols) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<QuotesDto> quotes = integrationService.getQuotes(symbols);

            response.put("s", ResStatus.OK.getStatus());
            response.put("d", quotes);
        } catch (Exception ex) {
            response.put("s", ResStatus.ERROR.getStatus());
            response.put("errmsg", ex.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity getHistory(@RequestParam String symbol,
                                     @RequestParam String resolution,
                                     @RequestParam long from,
                                     @RequestParam long to,
                                     @RequestParam(required = false) int countback) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<CandleDto> history = integrationService.getHistory(symbol, resolution, from, to, countback);

            if (CollectionUtils.isEmpty(history)) {
                // todo: get previous candle time
                LocalDateTime nextTime = null;

                response.put("s", ResStatus.NO_DATA.getStatus());

                if (nonNull(nextTime)) {
                    response.put("nb", nextTime);
                }
            } else {
                response.putAll(BarDataConverter.convert(history));
            }
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            response.put("s", ResStatus.ERROR.getStatus());
            response.put("errmsg", ex.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}