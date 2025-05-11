package dev.codingsales.Captive.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class Setting.
 */
@Entity
@Table(name="settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Setting {
    /** The id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Min(1)
    private Long id;

    /** The name. */
    @Column
    @NotNull @NotBlank @NotEmpty
    private String name;

    /** The description. */
    @Column
    @NotNull @NotBlank @NotEmpty
    private String description;

    /** The type. */
    @NotNull
    private String type;

    /** The value. */
    @Column
    @NotNull
    private String value;
}
