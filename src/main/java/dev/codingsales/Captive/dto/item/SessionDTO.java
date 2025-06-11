package dev.codingsales.Captive.dto.item;

import java.sql.Timestamp;
import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {
    private Long id;

    @NotNull @NotBlank @NotNull
    private String deviceMac;
    @NotNull @NotBlank
    private String fullName;
    @NotNull @NotBlank
    private String email;
    @NotNull @NotBlank @NotNull
    private String deviceIp;
    @NotNull@NotBlank
    private String cpf;

    @NotNull @NotBlank @NotNull
    private String accesspointMac;

    @NotNull

    private Timestamp lastLoginOn;

    @NotNull

    private Timestamp expireLoginOn;

    @NotNull
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp removeSessionOn;

    private String browser;

    private String operatingSystem;


    /**
     * Checks if is valid.
     *
     * @return true, if is valid
     */
    @JsonProperty("valid")
    public boolean isValid() {
        Timestamp now = new Timestamp((new Date()).getTime());
        return expireLoginOn.after(now);
    }
}
