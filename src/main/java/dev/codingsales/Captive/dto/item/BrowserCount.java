package dev.codingsales.Captive.dto.item;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class BrowserCountDTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrowserCount {
    @JsonProperty("browserName")
    private String browser;

    private Integer quantity;
}
