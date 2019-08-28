package me.exrates.externalservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.api.models.TickerResponse;
import me.exrates.externalservice.entities.enums.ResStatus;

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

    public static QuotesDto of(String pair, TickerResponse response) {
        return QuotesDto.builder()
                .status(ResStatus.OK.getStatus())
                .pair(pair)
                .price(PriceDto.of(response))
                .build();
    }
}