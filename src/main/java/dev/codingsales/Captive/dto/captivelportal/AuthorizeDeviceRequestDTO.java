package dev.codingsales.Captive.dto.captivelportal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Nome completo é obrigatório")
    @Size(min= 3, max = 100, message = "Nome completo deve ter entre 3 e 100 caracteres")
    private String fullName;

    @Size(max = 20, message = "Número de telefone inválido")
    private String phoneNumber;
}
