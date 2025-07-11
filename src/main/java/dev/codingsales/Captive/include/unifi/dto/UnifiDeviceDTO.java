package dev.codingsales.Captive.include.unifi.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Date;
@Data
public class UnifiDeviceDTO {

    private String id;
    private String name;
    private String model;
    private boolean supported;

    @JsonProperty("macAddress")
    private String macAddress;

    @JsonProperty("ipAddress")
    private String ipAddress;

    /**
     * O estado atual do dispositivo (ex: "ONLINE", "OFFLINE", "ADOPTING").
     */
    private String state;

    /**
     * A versão do firmware atualmente instalada no dispositivo.
     */
    @JsonProperty("firmwareVersion")
    private String firmwareVersion;

    /**
     * Indica se há uma atualização de firmware disponível para o dispositivo.
     */
    @JsonProperty("firmwareUpdatable")
    private boolean firmwareUpdatable;

    /**
     * A data e hora em que o dispositivo foi adotado pelo controlador.
     */
    @JsonProperty("adoptedAt")
    private Date adoptedAt;
}
