package dev.codingsales.Captive.dto.captivelportal;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfoDTO {
    private String macAddress;
    private Long minutesLeft;
    private Long secondsLeft;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp expireOn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp lastLogin;

    /**
     * Checks if is valid.
     *
     * @return true, if is valid
     */
    public boolean isValid() {
        return (getMinutesLeft() > 0 || getSecondsLeft() > 10);
    }
}
