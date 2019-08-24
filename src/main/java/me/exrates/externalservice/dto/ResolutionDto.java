package me.exrates.externalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.entities.enums.ResolutionType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResolutionDto {

    private ResolutionType type;
    private int value;

    @Override
    public String toString() {
        return String.join(" ", String.valueOf(value), type.name());
    }
}