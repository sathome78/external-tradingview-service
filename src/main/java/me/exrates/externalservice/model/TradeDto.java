package me.exrates.externalservice.model;

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

    public static TradeDto of(OrderDataDto orderDataDto) {
        return TradeDto.builder()
                .pair(Objects.nonNull(orderDataDto.getCurrencyPairName())
                        ? orderDataDto.getCurrencyPairName().replace("/", StringUtils.EMPTY)
                        : null)
                .price(orderDataDto.getExrate())
                .volume(orderDataDto.getAmountBase())
                .acceptDate(Objects.nonNull(orderDataDto.getTradeDate())
                        ? Timestamp.valueOf(orderDataDto.getTradeDate()).getTime()
                        : null)
                .type("t")
                .build();
    }
}