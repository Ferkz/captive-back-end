package dev.codingsales.Captive.dto.captivelportal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizeDeviceRequestDTO {
    @NotNull @NotEmpty @NotBlank
    private String macAddress;
    private String ipAddress;
    private String AccessPointMacAddress;
    @NotNull @NotEmpty @NotBlank
    private String email;
    private String browser;
    private String operatingSystem;
    private Boolean acceptTou = Boolean.FALSE;
}
