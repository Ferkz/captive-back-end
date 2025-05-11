package dev.codingsales.Captive.dto.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class EntityCountDTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

public class EntityCountDTO {
    private String entityName;

    private Integer count;
}
