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

    public static OrderDto of(OrderDataDto orderDataDto) {
        return OrderDto.builder()
                .pair(Objects.nonNull(orderDataDto.getCurrencyPairName())
                        ? orderDataDto.getCurrencyPairName().replace("/", StringUtils.EMPTY)
                        : null)
                .price(orderDataDto.getExrate())
                .volume(orderDataDto.getAmountBase())
                .createDate(Objects.nonNull(orderDataDto.getCreateDate())
                        ? Timestamp.valueOf(orderDataDto.getCreateDate()).getTime()
                        : null)
                .type(orderDataDto.getOperationTypeId() == 3 ? "a" : "b")
                .build();
    }
}