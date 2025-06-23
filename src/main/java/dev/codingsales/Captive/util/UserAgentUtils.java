package dev.codingsales.Captive.util;

import jakarta.servlet.http.HttpServletRequest;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;

public class UserAgentUtils {

    private static final UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();

    /**
     * Extrai e analisa a string User-Agent da requisição.
     *
     * @param request A requisição HTTP.
     * @return um objeto ReadableUserAgent que contém todas as informações analisadas.
     */
    private static ReadableUserAgent getParsedAgent(HttpServletRequest request) {
        String userAgentString = request.getHeader("User-Agent");

        return parser.parse(userAgentString != null ? userAgentString:"");
    }

    /**
     * Obtém o nome do navegador (ex: "Chrome Mobile", "Safari", "Edge").
     *
     * @param request A requisição HTTP.
     * @return O nome do navegador.
     */
    public static String getBrowser(HttpServletRequest request) {
        return getParsedAgent(request).getName();
    }

    /**
     * Obtém o nome do sistema operacional de forma precisa.
     * Resolve o problema de "Linux" vs "Android".
     *
     * @param request A requisição HTTP.
     * @return O nome do sistema operacional (ex: "Android", "iOS", "Windows").
     */
    public static String getOperatingSystem(HttpServletRequest request) {
        return getParsedAgent(request).getOperatingSystem().getName();
    }

    /**
     * Obtém a categoria do dispositivo (ex: "Smartphone", "Tablet", "Desktop").
     *
     * @param request A requisição HTTP.
     * @return A categoria do dispositivo.
     */
    public static String getDeviceCategory(HttpServletRequest request) {
        return getParsedAgent(request).getDeviceCategory().getName();
    }

    /**
     * Retorna a string User-Agent original, se necessário.
     *
     * @param request the request
     * @return the agent
     */
    public static String getRawAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
