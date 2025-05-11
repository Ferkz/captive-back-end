package dev.codingsales.Captive.dto.item;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class OsCount {
    @JsonProperty("os")
    private String os;

    private Integer quantity;
}
