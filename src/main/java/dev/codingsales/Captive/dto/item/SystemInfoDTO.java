package dev.codingsales.Captive.dto.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class SystemInfoDTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

public class SystemInfoDTO {
    private String hostname;

    private String ipAddress;

    private String operatingSystem;

    private String operatingSystemVersion;

    private String javaVersion;

    private String javaVendor;

    private String osArch;
}
