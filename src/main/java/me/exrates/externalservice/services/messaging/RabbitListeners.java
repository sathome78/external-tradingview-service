package me.exrates.externalservice.services.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import me.exrates.externalservice.model.OrderDataDto;
import me.exrates.externalservice.model.OrderDto;
import me.exrates.externalservice.model.TradeDto;
import me.exrates.externalservice.services.DataIntegrationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
    public void receiveTrade(OrderDataDto message) {
        log.info("<<< NEW MESSAGE FROM CORE SERVICE >>> Received message: {}", message);

        if (message.getStatusId() == 3) {
            final TradeDto tradeDto = TradeDto.of(message);

            integrationService.getBufferQueue().add(serialisePayload(tradeDto));
        } else {
            final OrderDto orderDto = OrderDto.of(message);

            integrationService.getBufferQueue().add(serialisePayload(orderDto));
        }
    }

    private String serialisePayload(final Object messagePayload) {
        try {
            return objectMapper.writeValueAsString(messagePayload);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}