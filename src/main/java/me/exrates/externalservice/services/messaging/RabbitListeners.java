package me.exrates.externalservice.services.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import me.exrates.externalservice.api.models.Candle;
import me.exrates.externalservice.api.models.CandleChartResponse;
import me.exrates.externalservice.dto.CandleDto;
import me.exrates.externalservice.dto.OrderDto;
import me.exrates.externalservice.dto.ResolutionDto;
import me.exrates.externalservice.dto.TradeDto;
import me.exrates.externalservice.entities.enums.ResolutionType;
import me.exrates.externalservice.services.DataIntegrationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

import static me.exrates.externalservice.api.ExratesPublicApi.DELIMITER;
import static me.exrates.externalservice.configurations.ApplicationConfiguration.JSON_MAPPER;

@Log4j2
@Component
public class RabbitListeners {

    private final DataIntegrationService integrationService;
    private final ObjectMapper objectMapper;

    @Autowired
    public RabbitListeners(DataIntegrationService integrationService,
                           @Qualifier(JSON_MAPPER) ObjectMapper objectMapper) {
        this.integrationService = integrationService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(id = "${spring.rabbitmq.trades-topic}", queues = "${spring.rabbitmq.trades-topic}")
    public void receiveTrade(TradeDto message) {
        log.info("<<< NEW TRADE MESSAGE FROM CORE SERVICE >>> Received message: {}", message);

        final String symbol = message.getPair().replace(DELIMITER, StringUtils.EMPTY);

        message.setPair(symbol);
        integrationService.getBufferQueue().add(serialisePayload(message));

        final ResolutionDto resolution = new ResolutionDto(ResolutionType.DAY, 1);
        final LocalDateTime fromDate = LocalDateTime.now();
        final LocalDateTime toDate = fromDate.plusDays(1);

        try {
            CandleChartResponse data = integrationService.getHistory(symbol, resolution, fromDate, toDate, null);

            List<Candle> candlesList = data.getBody();
            if (!CollectionUtils.isEmpty(candlesList)) {
                integrationService.getBufferQueue().add(serialisePayload(CandleDto.of(symbol, candlesList.get(0))));
            }
        } catch (Exception ex) {
            log.error("Could not get candle data for pair: {}", symbol);
        }
    }

    @RabbitListener(id = "${spring.rabbitmq.orders-topic}", queues = "${spring.rabbitmq.orders-topic}")
    public void receiveOrder(OrderDto message) {
        log.info("<<< NEW ORDER MESSAGE FROM CORE SERVICE >>> Received message: {}", message);

        final String symbol = message.getPair().replace(DELIMITER, StringUtils.EMPTY);

        message.setPair(symbol);
        integrationService.getBufferQueue().add(serialisePayload(message));
    }

    private String serialisePayload(final Object messagePayload) {
        try {
            return objectMapper.writeValueAsString(messagePayload);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}