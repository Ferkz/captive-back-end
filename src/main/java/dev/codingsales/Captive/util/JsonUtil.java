package dev.codingsales.Captive.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    /**
     * @param obj the obj
     * @return the string
     * @throws JsonProcessingException the json processing exception
     */
    public static String toJsonString(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(obj);
        return json;
    }
}
