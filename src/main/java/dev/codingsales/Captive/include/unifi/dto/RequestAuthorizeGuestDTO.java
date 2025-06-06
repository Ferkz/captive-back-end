package dev.codingsales.Captive.include.unifi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import lombok.AllArgsConstructor;

@Data // Includes getters, setters, toString, equals, hashCode
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestAuthorizeGuestDTO extends RequestGuestDTO {
    @JsonProperty("action")
    private String action; // e.g., "AUTHORIZE_GUEST_ACCESS", "UNAUTHORIZE_GUEST_ACCESS"

    @JsonProperty("timeLimitMinutes")
    private Integer timeLimitMinutes;

    @JsonProperty("dataUsageLimitMBytes")
    private Long dataUsageLimitMBytes;

    @JsonProperty("rxRateLimitKbps")
    private Integer rxRateLimitKbps;

    @JsonProperty("txRateLimitKbps")
    private Integer txRateLimitKbps;
}
