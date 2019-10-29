package me.exrates.externalservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.api.models.TickerResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PriceDto {

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

    public static PriceDto of(TickerResponse response) {
        return PriceDto.builder()
                .change(response.getValueChange())
                .changePercent(response.getPercentChange())
                .lastPrice(response.getLast())
                .askPrice(response.getLowestAsk())
                .bidPrice(response.getHighestBid())
                .openPrice(response.getFirst())
                .highPrice(response.getHigh24hr())
                .lowPrice(response.getLow24hr())
                .previousClosePrice(response.getFirst())
                .volume(response.getBaseVolume())
                .buyPipValue(1)
                .sellPipValue(1)
                .build();
    }
}