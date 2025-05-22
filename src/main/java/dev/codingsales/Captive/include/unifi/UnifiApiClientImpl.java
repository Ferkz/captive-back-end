package dev.codingsales.Captive.include.unifi;

import dev.codingsales.Captive.include.unifi.dto.ClientDTO; // Assuming you have this from previous suggestions
import dev.codingsales.Captive.include.unifi.dto.MetaDTO;
import dev.codingsales.Captive.include.unifi.dto.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class UnifiApiClientImpl implements UnifiApiClient {
    private static final Logger logger = LoggerFactory.getLogger(UnifiApiClientImpl.class);
    @Value("${unifi.api.baseurl}") // Should be: https://10.0.2.0/proxy/network
    private String baseUrl;
    @Value("${unifi.api.key}") // Your X-API-KEY
    private String apiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    public UnifiApiClientImpl(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }
    @Override
    public boolean login() { // This method is from the X-API-KEY version.
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("Verifique a chave API");
            return false;
        }
        logger.info("Login via API realizado");
        return true;
    }

    private HttpHeaders createApiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("UniFi API Key (X-API-KEY) is not configured.");
            throw new IllegalStateException("UniFi API Key (X-API-KEY) is not configured.");
        }
        headers.set("X-API-KEY", apiKey);
        return headers;
    }

    @Override
    public ClientDTO getClientByMac(String siteId, String macAddress) {
        String encodedFilter;
        try {
            // The filter format from UniFi API docs: <property>.<function>(<arguments>)
            String filterValue = String.format("macAddress.eq('%s')", macAddress.toLowerCase());
            encodedFilter = URLEncoder.encode(filterValue, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            logger.error("Error encoding filter for MAC address {}: {}", macAddress, e.getMessage(), e);
            return null;
        }

        // Using the /integration/v1 path
        String url = baseUrl + "/integration/v1/sites/" + siteId + "/clients?filter=" + encodedFilter;
        HttpEntity<Void> entity = new HttpEntity<>(createApiHeaders());

        try {
            logger.debug("Fetching UniFi client by MAC. URL: {}", url);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                Map<String, Object> responseMap = objectMapper.readValue(responseEntity.getBody(), new TypeReference<Map<String, Object>>() {});
                Object dataObject = responseMap.get("data");

                if (dataObject instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> clientDataList = (List<Map<String, Object>>) dataObject;
                    if (!clientDataList.isEmpty()) {
                        ClientDTO client = objectMapper.convertValue(clientDataList.get(0), ClientDTO.class);
                        logger.info("Client found: ID (UUID)={}, MAC={}", client.getId(), client.getMacAddress());
                        return client;
                    } else {
                        logger.warn("No client found with MAC {} in site {}. Response body: {}", macAddress, siteId, responseEntity.getBody());
                    }
                } else {
                    logger.warn("UniFi API response for getClientByMac did not contain a 'data' list or it was not a list. MAC: {}, Site: {}. Response body: {}", macAddress, siteId, responseEntity.getBody());
                }
            } else {
                logger.error("Failed to fetch client by MAC {}. Status: {}, Response: {}",
                        macAddress, responseEntity.getStatusCode(), responseEntity.getBody());
            }
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching UniFi client by MAC {}: {} - Response: {}", macAddress, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Unexpected error fetching UniFi client by MAC {}: {}", macAddress, e.getMessage(), e);
        }
        return null;
    }
    @Override
    public ResponseDTO executeClientAction(String siteId, String clientIdUuid,
                                                                dev.codingsales.Captive.include.unifi.dto.RequestAuthorizeGuestDTO payload) {
        String url = baseUrl + "/integration/v1/sites/" + siteId + "/clients/" + clientIdUuid + "/actions";
        HttpEntity<dev.codingsales.Captive.include.unifi.dto.RequestAuthorizeGuestDTO> entity =
                new HttpEntity<>(payload, createApiHeaders());

        dev.codingsales.Captive.include.unifi.dto.ResponseDTO customResponseDto =
                new dev.codingsales.Captive.include.unifi.dto.ResponseDTO();

        try {
            // The action string for logging comes from the payload itself.
            String actionForLogging = payload.getAction(); // Assuming RequestAuthorizeGuestDTOIntegrationV1 has getAction()

            logger.debug("Executing UniFi client action. URL: {}, ClientID (UUID): {}, Action: {}, Payload: {}",
                    url, clientIdUuid, actionForLogging, payload);

            ResponseEntity<Map<String, Object>> apiResponse = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            MetaDTO meta = new MetaDTO();
            if (apiResponse.getStatusCode() == HttpStatus.OK && apiResponse.getBody() != null) {
                logger.info("Action {} executed successfully for client UUID {} in site {}. UniFi Response: {}",
                        actionForLogging, clientIdUuid, siteId, apiResponse.getBody());
                meta.setRc("ok");
                meta.setMsg("Ação realizada com sucesso na UNIFI.");
                customResponseDto.setDataList(Collections.singletonList(apiResponse.getBody()));


            } else {
                logger.error("Falha ao executar ação {} for client UUID {}. Status: {}, UniFi Response: {}",
                        actionForLogging, clientIdUuid, apiResponse.getStatusCode(), apiResponse.getBody());
                meta.setRc("error");
                meta.setMsg("Failed to execute UniFi action: " + apiResponse.getStatusCode());
                if (apiResponse.getBody() != null) {
                    // customResponseDto.setData(apiResponse.getBody()); // If ResponseDTO.data is Map
                    customResponseDto.setDataList(Collections.singletonList(apiResponse.getBody()));
                }
            }
            customResponseDto.setMeta(meta);
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error executing UniFi client action for UUID {}: {} - Response: {}",
                    clientIdUuid, e.getStatusCode(), e.getResponseBodyAsString(), e);
            MetaDTO meta = new MetaDTO("error", "HTTP error: " + e.getStatusCode() + " - " +
                    e.getResponseBodyAsString().substring(0, Math.min(e.getResponseBodyAsString().length(), 250)) + "...");
            customResponseDto.setMeta(meta);
            try {
                // Check if response body is not empty and is valid JSON before parsing
                String responseBodyString = e.getResponseBodyAsString();
                if (responseBodyString != null && !responseBodyString.trim().isEmpty() &&
                        responseBodyString.trim().startsWith("{") && responseBodyString.trim().endsWith("}")) {
                    Map<String, Object> errorBodyMap = objectMapper.readValue(responseBodyString, new TypeReference<Map<String, Object>>() {});
                    // customResponseDto.setData(errorBodyMap); // If ResponseDTO.data is Map
                    customResponseDto.setDataList(Collections.singletonList(errorBodyMap));

                } else {
                    logger.warn("Erro no corpo da requisição, não e um json valido ou está vazio.");
                }
            } catch (Exception parseEx) {
                logger.warn("Could not parse JSON error response body from UniFi API: {}", parseEx.getMessage());
            }
        } catch (Exception e) {
            logger.error("Erro inesperado na unifi para o UUID {}: {}", clientIdUuid, e.getMessage(), e);
            MetaDTO meta = new MetaDTO("error", "Erro inesperado: " + e.getMessage());
            customResponseDto.setMeta(meta);
        }
        return customResponseDto;
    }
}