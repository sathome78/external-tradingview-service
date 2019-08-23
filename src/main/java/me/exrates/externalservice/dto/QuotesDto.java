package me.exrates.externalservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.api.ExratesPublicApi;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class QuotesDto {

    @JsonProperty("ch")
    private BigDecimal change;
    @JsonProperty("chp")
    private BigDecimal changePercent;
    @JsonProperty("lp")
    private BigDecimal lastPrice;
    @JsonProperty("ask")
    private BigDecimal askPrice;
    @JsonProperty("bid")
    private BigDecimal bidPrice;
    @JsonProperty("open_price")
    private BigDecimal openPrice;
    @JsonProperty("high_price")
    private BigDecimal highPrice;
    @JsonProperty("low_price")
    private BigDecimal lowPrice;
    @JsonProperty("prev_close_price")
    private BigDecimal previousClosePrice;
    private BigDecimal volume;
    private int buyPipValue;
    private int sellPipValue;

    public static QuotesDto of(ExratesPublicApi.TickerResponse response) {
        final double last = response.getLast().doubleValue();
        final double percentChange = response.getPercentChange().doubleValue();
        final double changeDouble = last * percentChange / 100;
        final double previousLastDouble = last - changeDouble;

        final BigDecimal change = BigDecimal.valueOf(changeDouble).setScale(8, RoundingMode.HALF_UP);
        final BigDecimal previousLast = BigDecimal.valueOf(previousLastDouble).setScale(8, RoundingMode.HALF_UP);

        return QuotesDto.builder()
                .change(change)
                .changePercent(response.getPercentChange())
                .lastPrice(response.getLast())
                .askPrice(response.getLowestAsk())
                .bidPrice(response.getHighestBid())
                .openPrice(previousLast)
                .highPrice(response.getHigh())
                .lowPrice(response.getLow())
                .previousClosePrice(previousLast)
                .volume(response.getBaseVolume())
                .buyPipValue(1)
                .sellPipValue(1)
                .build();
    }
}