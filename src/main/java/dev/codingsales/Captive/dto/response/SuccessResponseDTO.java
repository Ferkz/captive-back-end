package dev.codingsales.Captive.dto.response;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The Class SuccessResponseDTO.
 */
@Data
@EqualsAndHashCode(callSuper=true)

public class SuccessResponseDTO extends GenericResponseDTO {
    @JsonProperty("payload")
    private Object payload;
    private int status;
    private String description;
    private String redirectUrl;

    /**
     * Instantiates a new success response DTO.
     *
     * @param responseId the response id
     * @param responseDescription the response description
     * @param payload the payload
     * @param redirectUrl
     */
    public SuccessResponseDTO(Integer responseId, String responseDescription, String redirectUrl ,Object payload) {
        super(responseId, responseDescription);
        this.payload = payload;
        this.redirectUrl = redirectUrl;
    }
    public SuccessResponseDTO(Integer responseId, String responseDescription,Object payload ){
        super(responseId, responseDescription);
        this.payload = payload;
        this.redirectUrl = null;
    }
}
