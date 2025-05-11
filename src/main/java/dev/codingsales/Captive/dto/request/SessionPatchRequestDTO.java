package dev.codingsales.Captive.dto.request;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * The Class SessionPatchRequest.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

public class SessionPatchRequestDTO {
    private Timestamp expireSessionOn;
    private Timestamp removeSessionOn;
}
