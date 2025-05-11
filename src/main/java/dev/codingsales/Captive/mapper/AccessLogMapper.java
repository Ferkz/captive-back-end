package dev.codingsales.Captive.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import dev.codingsales.Captive.dto.item.AccessLogDTO;
import dev.codingsales.Captive.entity.AccessLog;
import dev.codingsales.Captive.entity.Session;

@Mapper
public interface AccessLogMapper {
    /** The instance. */
    AccessLogMapper INSTANCE = Mappers.getMapper(AccessLogMapper.class);


    /**
     * Access log to access log DTO.
     *
     * @param accessLog the access log
     * @return the access log DTO
     */
    AccessLogDTO accessLogToAccessLogDTO(AccessLog accessLog);


    /**
     * Access log DTO to access log.
     *
     * @param accessLogDTO the access log DTO
     * @return the access log
     */
    AccessLog accessLogDTOToAccessLog(AccessLogDTO accessLogDTO);


    /**
     * Session to access log.
     *
     * @param accessLog the access log
     * @return the access log
     */
    AccessLog sessionToAccessLog(Session session);
}
