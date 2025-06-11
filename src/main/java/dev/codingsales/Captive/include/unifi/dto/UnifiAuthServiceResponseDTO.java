package dev.codingsales.Captive.include.unifi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnifiAuthServiceResponseDTO {
    private boolean authorized;
    private String message;
    private String deviceName;
    private String deviceHostname;
    private String deviceOsName;
    private String deviceIpAddress;
    private String redirectUrl;
}
