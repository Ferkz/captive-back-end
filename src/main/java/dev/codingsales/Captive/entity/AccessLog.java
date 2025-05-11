package dev.codingsales.Captive.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class AccessLog.
 */
@Entity
@Table(name="access_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class AccessLog {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="device_mac")
    @NotNull @NotBlank @NotEmpty
    private String deviceMac;

    @Column(name="device_ip")
    @NotNull @NotBlank @NotEmpty
    private String deviceIp;

    @Column(name="accesspoint_mac")
    @NotNull @NotBlank @NotEmpty
    private String accesspointMac;

    @Column(name="lastlogin")
    @NotNull
    private Timestamp lastLoginOn;

    @Column(name="expire_login_on")
    @NotNull
    private Timestamp expireLoginOn;

    @Column(name="remove_session_on")
    @NotNull
    private Timestamp removeSessionOn;

    @Column(name="browser")
    private String browser;

    @Column(name="operating_system")
    private String operatingSystem;

}
