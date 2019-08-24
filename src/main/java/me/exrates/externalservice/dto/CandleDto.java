package me.exrates.externalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.api.ExratesPublicApi;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZoneOffset;

@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CandleDto {

    private Long time;
    private BigDecimal close;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal volume;

    public static CandleDto of(ExratesPublicApi.CandleChartResponse response) {
        return CandleDto.builder()
                .time(response.getTime().toEpochSecond(ZoneOffset.UTC))
                .close(response.getClose())
                .open(response.getOpen())
                .high(response.getHigh())
                .low(response.getLow())
                .volume(response.getVolume())
                .build();
    }
}