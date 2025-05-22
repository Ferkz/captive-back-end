package dev.codingsales.Captive.dto.unfi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnifiLoginRequest {
    private String username;
    private String password;
}
