package me.exrates.externalservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.exrates.externalservice.model.enums.ResolutionType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResolutionDto {

    private int value;
    private ResolutionType type;

    @Override
    public String toString() {
        return String.join(" ", String.valueOf(value), type.name());
    }
}