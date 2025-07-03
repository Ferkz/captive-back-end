package dev.codingsales.Captive.entity;

import java.sql.Timestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Min(1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_user_id", nullable = false)
    private GuestUser user;

    @Column(name="device_mac")
    @NotNull @NotBlank @NotEmpty
    private String deviceMac;

    @Column(name="device_name")
    @NotNull @NotBlank @NotEmpty
    private String deviceName;

    @Column(name="device_hostname")
    @NotNull @NotBlank @NotEmpty
    private String deviceHostName;

    @Column(name="device_ip")
    @NotNull @NotBlank @NotEmpty
    private String deviceIp;

    @Column(name="accesspoint_mac")
    @NotNull @NotBlank @NotEmpty
    private String accesspointMac;

    @Column(name="lastlogin")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp lastLoginOn;

    @Column(name="expire_login_on")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp expireLoginOn;

    @Column(name="remove_session_on")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp removeSessionOn;

    @Column(name="browser")
    private String browser;

    @Column(name="operating_system")
    private String operatingSystem;

}
