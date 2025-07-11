package dev.codingsales.Captive.include.unifi.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatsDTO {
    private long totalDevices;
    private long onlineDevices;
    private long offlineDevices;
}
