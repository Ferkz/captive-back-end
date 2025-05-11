package dev.codingsales.Captive.startup_services;
import java.util.TimeZone;
import dev.codingsales.Captive.startup.StartupService;

public class DefaultTimeZone implements StartupService{
    @Override
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
    }
}
