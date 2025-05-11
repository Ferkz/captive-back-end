package dev.codingsales.Captive.exeption;

import org.jboss.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import dev.codingsales.Captive.dto.response.ErrorResponseDTO;
import dev.codingsales.Captive.util.LoggerConstants;

public class BadRequestException extends HandledException {
    private static Logger logger = Logger.getLogger(BadRequestException.class);
    private static final long serialVersionUID = 1L;
    /**
     * @param message the message
     */
    public BadRequestException(String message) {
        super(message);
    }
    /**
     * @return the response entity
     */
    @Override
    public ResponseEntity<ErrorResponseDTO> getResponseEntity(){
        logger.error(String.format(LoggerConstants.BAD_REQUEST_EXCEPTION,"BadRequestException","getResponseEntity()","item"));
        return new ResponseEntity<ErrorResponseDTO>(
                new ErrorResponseDTO(HttpStatus.BAD_REQUEST.value(), HttpStatus.NO_CONTENT.toString(),
                        String.format(LoggerConstants.BAD_REQUEST_EXCEPTION,"BadRequestException","getResponseEntity()","item")),
                HttpStatus.BAD_REQUEST);
    }
}
