package me.exrates.externalservice.api.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import me.exrates.externalservice.serializers.LocalDateTimeToLongSerializer;
import me.exrates.externalservice.serializers.LongToLocalDateTimeDeserializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Trade {

    public double amount;
    public BigDecimal price;
    public BigDecimal total;
    public BigDecimal commission;
    @JsonProperty("order_id")
    public long orderId;
    @JsonSerialize(using = LocalDateTimeToLongSerializer.class)
    @JsonDeserialize(using = LongToLocalDateTimeDeserializer.class)
    @JsonProperty("date_acceptance")
    public LocalDateTime dateAcceptance;
    @JsonSerialize(using = LocalDateTimeToLongSerializer.class)
    @JsonDeserialize(using = LongToLocalDateTimeDeserializer.class)
    @JsonProperty("date_creation")
    public LocalDateTime dateCreation;
    @JsonProperty("order_type")
    public String orderType;
}