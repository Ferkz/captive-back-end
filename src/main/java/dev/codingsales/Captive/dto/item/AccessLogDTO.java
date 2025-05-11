package dev.codingsales.Captive.dto.item;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessLogDTO {
    private Long id;

    @NotNull @NotBlank @NotEmpty
    private String deviceMac;

    @NotNull @NotBlank @NotEmpty
    private String deviceIp;

    @NotNull @NotBlank @NotEmpty
    private String accesspointMac;

    @NotNull
    private Date lastLoginOn;

    @NotNull
    private Date expireLoginOn;

    @NotNull
    private Date removeSessionOn;

    private String browser;

    private String operatingSystem;
}
