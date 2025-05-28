package dev.codingsales.Captive.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import dev.codingsales.Captive.dto.item.AccessLogDTO;
import dev.codingsales.Captive.entity.AccessLog;
import dev.codingsales.Captive.entity.Session;

@Mapper
public interface AccessLogMapper {
    /** The instance. */
    AccessLogMapper INSTANCE = Mappers.getMapper(AccessLogMapper.class);
    /**
     * @param accessLog the access log
     * @return the access log DTO
     */
    AccessLogDTO accessLogToAccessLogDTO(AccessLog accessLog);
    /**
     * @param accessLogDTO the access log DTO
     * @return the access log
     */
    AccessLog accessLogDTOToAccessLog(AccessLogDTO accessLogDTO);
    /**
     * @param session the access log
     * @return the access log
     */
    @Mapping(target = "id", ignore = true)
    AccessLog sessionToAccessLog(Session session);
}