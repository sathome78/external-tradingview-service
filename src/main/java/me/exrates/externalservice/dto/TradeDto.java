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
public class TradeDto {

    @JsonProperty("id")
    private String pair;
    @JsonProperty("p")
    private BigDecimal price;
    @JsonProperty("s")
    private BigDecimal volume;
    @JsonProperty("t")
    private Long acceptDate;
    @JsonProperty("f")
    private String type;

    public static TradeDto of(ExternalOrderDto orderDto) {
        return TradeDto.builder()
                .pair(Objects.nonNull(orderDto.getPairName())
                        ? orderDto.getPairName().replace("/", StringUtils.EMPTY)
                        : null)
                .price(orderDto.getExrate())
                .volume(orderDto.getAmountBase())
                .acceptDate(Objects.nonNull(orderDto.getAcceptDate())
                        ? Timestamp.valueOf(orderDto.getAcceptDate()).getTime()
                        : null)
                .type("t")
                .build();
    }
}