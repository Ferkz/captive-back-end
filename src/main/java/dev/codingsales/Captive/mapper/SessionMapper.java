package dev.codingsales.Captive.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import dev.codingsales.Captive.dto.item.SessionDTO;
import dev.codingsales.Captive.entity.Session;

@Mapper
public interface SessionMapper {
    SessionMapper INSTANCE = Mappers.getMapper(SessionMapper.class);

    /**
     * Session to session DTO.
     *
     * @param session the session
     * @return the session DTO
     */
    SessionDTO SessionToSessionDTO(Session session);

    /**
     * Session DTO to session.
     *
     * @param sessionDTO the session DTO
     * @return the session
     */
    Session SessionDTOToSession(SessionDTO sessionDTO);
}
