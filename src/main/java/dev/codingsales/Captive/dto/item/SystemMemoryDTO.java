package dev.codingsales.Captive.dto.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class SystemMemoryDTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

public class SystemMemoryDTO {
    private Long total;

    private Long free;

    private Long used;

    private Long max;

    private Long fsfree;

    private Long fstotal;
}
