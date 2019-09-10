package me.exrates.externalservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    @JsonProperty("id")
    private String pair;
    @JsonProperty("p")
    private BigDecimal price;
    @JsonProperty("s")
    private BigDecimal volume;
    @JsonProperty("t")
    private Long createDate;
    @JsonProperty("f")
    private String type;

    public static OrderDto of(ExternalOrderDto orderDto) {
        return OrderDto.builder()
                .pair(Objects.nonNull(orderDto.getPairName())
                        ? orderDto.getPairName().replace("/", StringUtils.EMPTY)
                        : null)
                .price(orderDto.getExrate())
                .volume(orderDto.getAmountBase())
                .createDate(Objects.nonNull(orderDto.getCreateDate())
                        ? Timestamp.valueOf(orderDto.getCreateDate()).getTime()
                        : null)
                .type(orderDto.getOperationType().equalsIgnoreCase("BUY") ? "a" : "b")
                .build();
    }
}