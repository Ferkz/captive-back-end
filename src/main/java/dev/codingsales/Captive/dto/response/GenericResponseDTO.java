package dev.codingsales.Captive.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GenericResponseDTO {
    @JsonProperty("response")
    private Integer responseId;

    @JsonProperty("description")
    private String responseDescription;

    public GenericResponseDTO() {
        super();
    }

    public GenericResponseDTO(Integer responseId, String responseDescription) {
        super();
        this.responseId = responseId;
        this.responseDescription = responseDescription;
    }
}

