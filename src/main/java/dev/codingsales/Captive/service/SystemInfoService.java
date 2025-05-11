package dev.codingsales.Captive.service;

import java.io.IOException;

import dev.codingsales.Captive.dto.item.SystemInfoDTO;
import dev.codingsales.Captive.dto.item.SystemMemoryDTO;

public interface SystemInfoService {

    /**
     * Gets the system memory.
     *
     * @return the system memory
     * @throws IOException
     */
    public SystemMemoryDTO getSystemMemory() throws IOException;

    /**
     * Gets the system info.
     *
     * @return the system info
     */
    public SystemInfoDTO getSystemInfo();
}
