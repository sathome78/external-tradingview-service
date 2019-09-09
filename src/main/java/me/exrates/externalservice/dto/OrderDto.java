package me.exrates.externalservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.serializers.LocalDateTimeToLongSerializer;
import me.exrates.externalservice.serializers.LongToLocalDateTimeDeserializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @JsonSerialize(using = LocalDateTimeToLongSerializer.class)
    @JsonDeserialize(using = LongToLocalDateTimeDeserializer.class)
    private LocalDateTime createDate;
    @JsonProperty("f")
    private String type;
}