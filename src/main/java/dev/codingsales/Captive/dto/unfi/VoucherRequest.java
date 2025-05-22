package dev.codingsales.Captive.dto.unfi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherRequest {
    private Integer minutes;
    private Integer quantity;

}
