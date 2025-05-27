package dev.codingsales.Captive.include.unifi.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestLoginRequestDTO {
    @NotBlank(message = "Email é obrigatório")
    @Email(message ="Formato de email inválido")
    @Size(max = 100)
    private String email;

    // MAC do dispositivo do cliente, essencial para autorizar no UniFi
    @NotBlank(message = "Mac do dispositivo é obrigatório")
    private String deviceMac;

    // Opcional: pode ser útil para cenários onde o AP envia o MAC para você
    private String accessPointMac;
}
