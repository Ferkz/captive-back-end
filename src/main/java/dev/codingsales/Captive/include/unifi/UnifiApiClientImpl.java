package dev.codingsales.Captive.include.unifi;

import dev.codingsales.Captive.include.unifi.dto.ClientDTO;
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

import java.net.URLEncoder; // Será mantido no import, mas não mais usado para o filtro problemático
import java.nio.charset.StandardCharsets; // Será mantido no import
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
        // Removendo completamente o parâmetro 'filter' da URL
        // e buscando todos os clientes para filtrar manualmente no código.
        String url = baseUrl + "/integration/v1/sites/" + siteId + "/clients"; // URL para buscar TODOS os clientes
        HttpEntity<Void> entity = new HttpEntity<>(createApiHeaders());

        try {
            logger.debug("Buscando todos os clientes UniFi para o site {}. URL: {}", siteId, url);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                Map<String, Object> responseMap = objectMapper.readValue(responseEntity.getBody(), new TypeReference<Map<String, Object>>() {});
                Object dataObject = responseMap.get("data");

                if (dataObject instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> clientDataList = (List<Map<String, Object>>) dataObject;

                    // Filtrar manualmente a lista de clientes por endereço MAC no lado da aplicação
                    for (Map<String, Object> clientMap : clientDataList) {
                        if (clientMap.containsKey("macAddress") && clientMap.get("macAddress").toString().equalsIgnoreCase(macAddress)) {
                            ClientDTO client = objectMapper.convertValue(clientMap, ClientDTO.class);
                            logger.info("Cliente encontrado por filtro manual: ID (UUID)={}, MAC={}", client.getId(), client.getMacAddress());
                            return client;
                        }
                    }
                    logger.warn("Nenhum cliente encontrado com MAC {} no site {} após filtragem manual dos clientes recuperados. Total de clientes na resposta: {}", macAddress, siteId, clientDataList.size());
                } else {
                    logger.warn("A resposta da API UniFi para getClientByMac não continha uma lista 'data' ou não era uma lista. MAC: {}, Site: {}. Corpo da resposta: {}", macAddress, siteId, responseEntity.getBody());
                }
            } else {
                logger.error("Falha ao buscar todos os clientes para o site {}. Status: {}, Resposta: {}",
                        siteId, responseEntity.getStatusCode(), responseEntity.getBody());
            }
        } catch (HttpClientErrorException e) {
            logger.error("Erro HTTP ao buscar todos os clientes UniFi para o site {}: {} - Resposta: {}", siteId, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar todos os clientes UniFi para o site {}: {}", siteId, e.getMessage(), e);
        }
        return null; // Cliente não encontrado ou ocorreu um erro
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