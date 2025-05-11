package dev.codingsales.Captive.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ErrorResponseDTO extends GenericResponseDTO{

    @JsonProperty("payload")
    private String errorDescription;


    /**
     *
     * @param responseId the response id
     * @param responseDescription the response description
     * @param errorDescription the error description
     */
    public ErrorResponseDTO(Integer responseId, String responseDescription, String errorDescription) {
        super(responseId, responseDescription);
        this.errorDescription = errorDescription;
    }
}
