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
        int offset = 0;
        int limit = 100;
        boolean moreClients = true; // Flag para controlar se há mais páginas a serem buscadas

        while (moreClients) {
            // Constrói a URL com offset e limit para buscar páginas específicas de clientes
            String url = baseUrl + "/integration/v1/sites/" + siteId + "/clients?offset=" + offset + "&limit=" + limit;
            HttpEntity<Void> entity = new HttpEntity<>(createApiHeaders());

            try {
                logger.debug("Buscando clientes UniFi para o site {} (offset: {}, limit: {}). URL: {}", siteId, offset, limit, url);
                ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                    Map<String, Object> responseMap = objectMapper.readValue(responseEntity.getBody(), new TypeReference<Map<String, Object>>() {});
                    Object dataObject = responseMap.get("data");

                    if (dataObject instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> clientDataList = (List<Map<String, Object>>) dataObject;

                        // Itera a lista de clientes desta página e filtra manualmente pelo MAC
                        for (Map<String, Object> clientMap : clientDataList) {
                            if (clientMap.containsKey("macAddress") && clientMap.get("macAddress").toString().equalsIgnoreCase(macAddress)) {
                                ClientDTO client = objectMapper.convertValue(clientMap, ClientDTO.class);
                                logger.info("Cliente encontrado por filtro manual: ID (UUID)={}, MAC={}",
                                        client.getId(),
                                        client.getHostname(),
                                        client.getMacAddress());
                                return client; // Cliente encontrado, retorna o DTO
                            }
                        }

                        // Lógica para verificar se há mais páginas a serem buscadas
                        int count = (Integer) responseMap.getOrDefault("count", 0); // Quantidade de clientes na resposta atual
                        int totalCount = (Integer) responseMap.getOrDefault("totalCount", 0); // Total de clientes disponíveis no UniFi

                        if (count < limit || (offset + count) >= totalCount) {
                            moreClients = false;
                        } else {
                            offset += count; // Avança o offset para a próxima página
                            logger.debug("Continuando para a próxima página de clientes. Próximo offset: {}", offset);
                        }

                    } else {
                        logger.warn("A resposta da API UniFi para getClientByMac não continha uma lista 'data' ou não era uma lista. MAC: {}, Site: {}. Corpo da resposta: {}", macAddress, siteId, responseEntity.getBody());
                        moreClients = false; // Interrompe a busca se o formato da resposta for inesperado
                    }
                } else {
                    logger.error("Falha ao buscar clientes para o site {} (offset: {}, limit: {}). Status: {}, Resposta: {}",
                            siteId, offset, limit, responseEntity.getStatusCode(), responseEntity.getBody());
                    moreClients = false; // Interrompe a busca se a chamada da API falhou
                }
            } catch (HttpClientErrorException e) {
                logger.error("Erro HTTP ao buscar clientes UniFi para o site {} (offset: {}, limit: {}): {} - Resposta: {}",
                        siteId, offset, limit, e.getStatusCode(), e.getResponseBodyAsString(), e);
                moreClients = false; // Interrompe a busca em caso de erro HTTP
            } catch (Exception e) {
                logger.error("Erro inesperado ao buscar clientes UniFi para o site {} (offset: {}, limit: {}): {}",
                        siteId, offset, limit, e.getMessage(), e);
                moreClients = false; // Interrompe a busca em caso de erro inesperado
            }
        }
        logger.warn("Nenhum cliente encontrado com MAC {} no site {} após verificar todas as páginas.", macAddress, siteId);
        return null; // Cliente não encontrado após verificar todas as páginas
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