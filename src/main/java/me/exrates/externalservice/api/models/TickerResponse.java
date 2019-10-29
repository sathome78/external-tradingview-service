package me.exrates.externalservice.api.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TickerResponse {

    @JsonProperty("currency_pair_id")
    private Integer currencyPairId;
    @JsonProperty("currency_pair_name")
    private String currencyPairName;
    private BigDecimal first;
    private BigDecimal last;
    @JsonProperty("base_volume")
    private BigDecimal baseVolume;
    @JsonProperty("quote_volume")
    private BigDecimal quoteVolume;
    @JsonProperty("high_24hr")
    private BigDecimal high24hr;
    @JsonProperty("low_24hr")
    private BigDecimal low24hr;
    @JsonProperty("highest_bid")
    private BigDecimal highestBid;
    @JsonProperty("lowest_ask")
    private BigDecimal lowestAsk;
    @JsonProperty("is_frozen")
    private Integer isFrozen;
    @JsonProperty("percent_change")
    private BigDecimal percentChange;
    @JsonProperty("value_change")
    private BigDecimal valueChange;
}