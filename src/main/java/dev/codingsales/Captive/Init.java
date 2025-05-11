package dev.codingsales.Captive;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import dev.codingsales.Captive.startup.StartupService;
import dev.codingsales.Captive.startup_services.DefaultTimeZone;
import dev.codingsales.Captive.startup_services.GenericStartup;

@Component
public class Init implements StartupService {
    private Logger logger = LoggerFactory.getLogger(Init.class);
    private Map<String, StartupService> serviceMap;

    public Init() {
        this.serviceMap = new HashMap<String, StartupService>();

        // add services here
        this.serviceMap.put("generic", new GenericStartup());
        this.serviceMap.put("default timezone", new DefaultTimeZone());
    }
    @PostConstruct
    public void init() {
        logger.info("[Init]: running startup services..");
        this.serviceMap.keySet().stream().forEach(key -> {
            logger.info("[INIT]: booting " + key + " service");
            this.serviceMap.get(key).init();
        });
    }

    /**
     * Gets the service map.
     *
     * @return the service map
     */
    public Map<String, StartupService> getServiceMap() {
        return serviceMap;
    }
}
