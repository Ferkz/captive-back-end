package dev.codingsales.Captive.startup_services;
import org.jboss.logging.Logger;
import dev.codingsales.Captive.startup.StartupService;

public class GenericStartup implements StartupService{
    private Logger logger = Logger.getLogger(GenericStartup.class);

    @Override
    public void init() {
        logger.info("[GenericStartup]: booting raw services");
    }
}
