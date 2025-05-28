package dev.codingsales.Captive.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * The Class Session.
 */
@Entity
@Table(name="sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Session {
    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Min(1)
    private Long id;

    // mac do dispositivo
    @Column(name="device_mac", unique = true)
    @NotNull @NotBlank @NotEmpty
    private String deviceMac;

    /** The device ip. */
    @Column(name="device_ip")
    @NotNull @NotBlank @NotEmpty
    private String deviceIp;

    /** The accesspoint mac. */
    @Column(name="accesspoint_mac")
    @NotNull @NotBlank @NotEmpty
    private String accesspointMac;

    /** The last login date. */
    @Column(name="lastlogin")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp lastLoginOn;

    /** The expire login date. */
    @Column(name="expire_login_on")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp expireLoginOn;

    /** The remove session date. */
    @Column(name="remove_session_on")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp removeSessionOn;

    /** The browser. */
    @Column(name="browser")
    private String browser;

    /** The operating system. */
    @Column(name="operating_system")
    private String operatingSystem;
    @Column(name="full_name")
    @NotNull @NotBlank
    @Size(min = 3, max = 100)
    private String fullName;

    @Column(name="email")
    @NotNull @NotBlank @Email
    @Size(max = 100)
    private String email;

    @Column(name="phone_number")
    @Size(max = 20) // Ajuste o tamanho conforme necessário
    private String phoneNumber;

    @Column(name="accepted_tou") // Termos de Uso
    private Boolean acceptedTou = Boolean.FALSE;


}
