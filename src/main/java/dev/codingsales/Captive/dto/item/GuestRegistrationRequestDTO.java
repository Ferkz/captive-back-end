package dev.codingsales.Captive.dto.item;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestRegistrationRequestDTO {
    @NotBlank(message = "nome completo e obrigatorio")
    @Size(min= 3, max = 100, message = "Nome completo deve ter entre 3 e 100 caracteres")
    private String fullName;
    @NotBlank(message = "Email e obrigatorio")
    @Email(message ="Formato de email inválido")
    @Size(max = 100)
    private String email;
    @NotBlank(message = "CPF é obrigatório")
    @NotNull
    private String cpf;
    @NotBlank(message = "Número de telefone e obrigatorio")
    @Size(min = 10, max = 20, message = "Numero de telefone invalido")
    private String phoneNumber;
    private String deviceName;
    private String deviceMac;
    private String deviceIp;
    private String accessPointMac;
    private String browser;
    private String operatingSystem;
    @NotNull(message = "Você deve aceitar os Termos de Uso.")
    @AssertTrue(message = "Você deve aceitar os Termos de Uso para continuar.")
    private Boolean acceptTou = Boolean.FALSE;
}
