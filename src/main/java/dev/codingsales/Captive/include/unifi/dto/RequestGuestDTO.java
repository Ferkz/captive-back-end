package dev.codingsales.Captive.include.unifi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class RequestStaDTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestGuestDTO {
    /** The cmd. */
    @NotNull @NotEmpty @NotBlank
    private String cmd;

    /** The mac address */
    @NotNull @NotEmpty @NotBlank
    private String mac;
}
