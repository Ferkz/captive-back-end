package dev.codingsales.Captive.mapper;

import dev.codingsales.Captive.dto.item.SessionDTO;
import dev.codingsales.Captive.entity.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.cpf", target = "cpf")
    @Mapping(source = "user.email", target = "email")
    SessionDTO SessionToSessionDTO(Session session);

    List<SessionDTO> toSessionDTOList(List<Session> sessions);

    @Mapping(target = "user", ignore = true)
    Session SessionDTOToSession(SessionDTO sessionDTO);
}
