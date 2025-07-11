package dev.codingsales.Captive.controller;

import dev.codingsales.Captive.include.unifi.dto.DeviceStatsDTO;
import dev.codingsales.Captive.include.unifi.dto.UnifiDeviceDTO;
import dev.codingsales.Captive.service.UnifiDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin/devices")
public class DeviceController {
    @Autowired
    private UnifiDeviceService deviceService;

    @GetMapping
    public ResponseEntity<List<UnifiDeviceDTO>> listAllDevices(){
        List<UnifiDeviceDTO> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }
    @GetMapping("/stats")
    public ResponseEntity<DeviceStatsDTO> getDeviceStats() {
        DeviceStatsDTO stats = deviceService.getDeviceStats();
        return ResponseEntity.ok(stats);
    }
    @PostMapping("/{deviceId}/restart")
    public ResponseEntity<Void> restartDevice(@PathVariable String deviceId) {
        deviceService.restartDevice(deviceId);
        return ResponseEntity.ok().build();
    }

    /**
     * Executa a ação de POWER_CYCLE em uma porta específica de um dispositivo.
     * @param deviceId O ID do dispositivo.
     * @param portIndex O índice da porta (portIdx).
     */
    @PostMapping("/{deviceId}/ports/{portIndex}/power-cycle")
    public ResponseEntity<Void> powerCyclePort(
            @PathVariable String deviceId,
            @PathVariable int portIndex) {
        deviceService.powerCyclePort(deviceId, portIndex);
        return ResponseEntity.ok().build();
    }
}
