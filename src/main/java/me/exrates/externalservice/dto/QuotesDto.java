package me.exrates.externalservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.api.ExratesPublicApi;

@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class QuotesDto {

    private static final String OK = "ok";

    @JsonProperty("s")
    private String status;
    @JsonProperty("n")
    private String pair;
    @JsonProperty("v")
    private PriceDto price;

    public static QuotesDto of(String pair, ExratesPublicApi.TickerResponse response) {
        return QuotesDto.builder()
                .status(OK)
                .pair(pair)
                .price(PriceDto.of(response))
                .build();
    }
}