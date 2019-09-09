package me.exrates.externalservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.api.models.Candle;

import java.math.BigDecimal;

@Data
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor
public class CandleDto {

    @JsonProperty("id")
    private String symbol;
    @JsonProperty("c")
    private BigDecimal close;
    @JsonProperty("o")
    private BigDecimal open;
    @JsonProperty("h")
    private BigDecimal high;
    @JsonProperty("l")
    private BigDecimal low;
    @JsonProperty("v")
    private BigDecimal volume;
    @JsonProperty("f")
    private String type;

    public static CandleDto of(String symbol, Candle candle) {
        return CandleDto.builder()
                .symbol(symbol)
                .close(candle.getClose())
                .open(candle.getOpen())
                .high(candle.getHigh())
                .low(candle.getLow())
                .volume(candle.getVolume())
                .type("d")
                .build();
    }
}