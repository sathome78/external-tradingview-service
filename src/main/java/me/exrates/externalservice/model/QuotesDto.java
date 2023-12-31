package me.exrates.externalservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.api.models.TickerResponse;
import me.exrates.externalservice.model.enums.ResStatus;

import java.util.Objects;

@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class QuotesDto {

    @JsonProperty("s")
    private String status;
    @JsonProperty("n")
    private String pair;
    @JsonProperty("v")
    private PriceDto price;

    public static QuotesDto of(String symbol, TickerResponse response) {
        return QuotesDto.builder()
                .status(ResStatus.OK.getStatus())
                .pair(symbol)
                .price(Objects.nonNull(response) ? PriceDto.of(response) : null)
                .build();
    }
}