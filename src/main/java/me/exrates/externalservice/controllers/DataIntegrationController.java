package me.exrates.externalservice.controllers;

import me.exrates.externalservice.api.models.CandleChartResponse;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static me.exrates.externalservice.api.ExratesPublicApi.FORMATTER;

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
            List<QuotesDto> data = integrationService.getQuotes(symbols);

            response.put("s", ResStatus.OK.getStatus());
            response.put("d", data);
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
            CandleChartResponse data = integrationService.getHistory(symbol, resolution, from, to, countback);

            List<CandleDto> candlesList = data.getBody();
            if (CollectionUtils.isEmpty(candlesList)) {
                response.put("s", ResStatus.NO_DATA.getStatus());

                List<String> errors = data.getErrors();
                if (Objects.nonNull(errors.get(0))) {
                    response.put("nb", FORMATTER.parse(errors.get(0)));
                }
            } else {
                response.putAll(BarDataConverter.convert(candlesList));
            }
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            response.put("s", ResStatus.ERROR.getStatus());
            response.put("errmsg", ex.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}