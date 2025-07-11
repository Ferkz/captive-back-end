package dev.codingsales.Captive.service;

import dev.codingsales.Captive.include.unifi.dto.DeviceStatsDTO;
import dev.codingsales.Captive.include.unifi.dto.UnifiDeviceDTO;

import java.util.List;

public interface UnifiDeviceService {
    List<UnifiDeviceDTO> getAllDevices();
    DeviceStatsDTO getDeviceStats();
    void restartDevice(String deviceId);
    void powerCyclePort(String deviceId, int portIndex);
}
