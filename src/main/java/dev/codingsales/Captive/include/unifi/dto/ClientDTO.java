package dev.codingsales.Captive.include.unifi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientDTO {
    @JsonProperty("id") //this is the UUID used as clientId
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("macAddress") // UniFi API uses "macAddress"
    private String macAddress;
    @JsonProperty("ipAddress")
    private String ipAddress;
    @JsonProperty("connectedAt")
    private String connectedAt; // Consider mapping to Date/Instant with custom deserializer
    @JsonProperty("type") // e.g., WIRED, WIRELESS
    private String type;
    @JsonProperty("hostname")
    private String hostname;
    @JsonProperty("osName")
    private String osName;
    // Add any other fields you might need from the client object
    // For example, from the API docs:
    // "access": { "type": "string" },
    // "uplinkDeviceId": "c2692e57-1e51-4519-bb90-c2bdad5882ca"


}
