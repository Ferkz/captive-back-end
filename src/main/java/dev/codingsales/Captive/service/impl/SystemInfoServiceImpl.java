package dev.codingsales.Captive.service.impl;

import java.io.IOException;

import org.springframework.stereotype.Service;

import dev.codingsales.Captive.dto.item.SystemInfoDTO;
import dev.codingsales.Captive.dto.item.SystemMemoryDTO;
import dev.codingsales.Captive.service.SystemInfoService;
import dev.codingsales.Captive.util.SystemInfoHelper;

@Service
public class SystemInfoServiceImpl {
    /**
     * Gets the system memory.
     *
     * @return the system memory
     * @throws IOException
     */
    public SystemMemoryDTO getSystemMemory() throws IOException {

        SystemMemoryDTO payload = new SystemMemoryDTO(SystemInfoHelper.getTotalMemory(),
                SystemInfoHelper.getFreeMemory(), SystemInfoHelper.getUsedMemory(), SystemInfoHelper.getMaxMemory(),
                SystemInfoHelper.getFreeSpace(), SystemInfoHelper.getTotalSpace());
        return payload;
    }

    /**
     * Gets the system info.
     *
     * @return the system info
     */
    public SystemInfoDTO getSystemInfo() {
        SystemInfoDTO payload = new SystemInfoDTO(SystemInfoHelper.getHostname(),
                SystemInfoHelper.getIPAddress(), SystemInfoHelper.getOSName(), SystemInfoHelper.getOSversion(),
                SystemInfoHelper.getJavaVersion(), SystemInfoHelper.getJavaVersion(),
                SystemInfoHelper.getOSArchVersion());
        return payload;
    }
}
