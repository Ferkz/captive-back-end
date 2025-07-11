package dev.codingsales.Captive.service.impl;

import dev.codingsales.Captive.include.unifi.UnifiApiClient;
import dev.codingsales.Captive.include.unifi.dto.DeviceStatsDTO;
import dev.codingsales.Captive.include.unifi.dto.UnifiDeviceDTO;
import dev.codingsales.Captive.service.UnifiDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class UnifiDeviceServiceImpl implements UnifiDeviceService {
    @Autowired
    private UnifiApiClient unifiApiClient;

    @Value("${unifi.default.site.id}")
    private String defaultSiteId;

    @Override
    public List<UnifiDeviceDTO> getAllDevices() {
        return unifiApiClient.listDevices(defaultSiteId);
    }
    @Override
    public DeviceStatsDTO getDeviceStats(){
        List<UnifiDeviceDTO> devices = getAllDevices();
        long total = devices.size();
        long online = devices.stream().filter(d -> "ONLINE".equalsIgnoreCase(d.getState())).count();
        long offline = total - online;
        return new DeviceStatsDTO(total, online, offline);
    }

    @Override
    public void restartDevice(String deviceId) {
        unifiApiClient.restartDevice(defaultSiteId, deviceId);
    }

    @Override
    public void powerCyclePort(String deviceId, int portIndex) {
        unifiApiClient.powerCyclePort(defaultSiteId, deviceId, portIndex);
    }
}
