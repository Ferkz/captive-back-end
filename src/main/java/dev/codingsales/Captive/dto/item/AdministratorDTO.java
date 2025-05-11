package dev.codingsales.Captive.dto.item;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdministratorDTO {

    private Long id;

    private String email;

    private String fullName;

    private Timestamp creationDate;

    private Timestamp lastModification;

    private Boolean enabled;
}
