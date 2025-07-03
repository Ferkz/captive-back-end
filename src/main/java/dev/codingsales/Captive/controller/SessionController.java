package dev.codingsales.Captive.controller;

import java.util.List;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import dev.codingsales.Captive.dto.item.SessionDTO;
import dev.codingsales.Captive.dto.request.SessionPatchRequestDTO;
import dev.codingsales.Captive.dto.response.SuccessResponseDTO;
import dev.codingsales.Captive.entity.Session;
import dev.codingsales.Captive.exeption.NoContentException;
import dev.codingsales.Captive.mapper.SessionMapper;
import dev.codingsales.Captive.service.SessionService;

@RestController
@RequestMapping("api/admin")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SessionMapper sessionMapper;

    private static Logger logger = Logger.getLogger(SessionController.class);

    @GetMapping("/sessions")
    public ResponseEntity<Object> getSessions() throws NoContentException {
        List<Session> sessions = this.sessionService.getSessions();
        List<SessionDTO> sessionsDTO = sessionMapper.toSessionDTOList(sessions);

        return new ResponseEntity<>(
                new SuccessResponseDTO(HttpStatus.OK.value(), HttpStatus.OK.toString(), sessionsDTO), HttpStatus.OK);
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<Object> getSession(@PathVariable("id") Long id) throws NoContentException {
        Session session = sessionService.getSession(id);
        SessionDTO sessionDTO = sessionMapper.SessionToSessionDTO(session);

        return new ResponseEntity<>(
                new SuccessResponseDTO(HttpStatus.OK.value(), HttpStatus.OK.toString(), sessionDTO), HttpStatus.OK);
    }
    @GetMapping("/sessions/expired")
    public ResponseEntity<Object> getExpiredSessions() {
        List<Session> result = this.sessionService.getExpiredSessions();
        List<SessionDTO> resultDTO = sessionMapper.toSessionDTOList(result);

        return new ResponseEntity<>(
                new SuccessResponseDTO(HttpStatus.OK.value(), HttpStatus.OK.toString(), resultDTO), HttpStatus.OK);
    }

    @GetMapping("/sessions/valid")
    public ResponseEntity<Object> getValidSessions() {
        List<Session> result = this.sessionService.getValidSessions();
        List<SessionDTO> resultDTO = sessionMapper.toSessionDTOList(result);

        return new ResponseEntity<>(
                new SuccessResponseDTO(HttpStatus.OK.value(), HttpStatus.OK.toString(), resultDTO), HttpStatus.OK);
    }

    @ConditionalOnProperty(name = "captive.sessions.full_update_enable", matchIfMissing = false)
    @PutMapping("/sessions")
    public ResponseEntity<Object> updateSession(@PathVariable("id") Long id, @RequestBody @Valid Session session) throws NoContentException {
        SessionDTO updatedDTO = sessionMapper.SessionToSessionDTO(sessionService.updateSession(id, session));
        return new ResponseEntity<Object>(
                new SuccessResponseDTO(HttpStatus.OK.value(), HttpStatus.OK.toString(), updatedDTO), HttpStatus.OK);
    }

    @PatchMapping("/sessions/{id}")
    public ResponseEntity<Object> updateSession(@PathVariable("id") Long id, @RequestBody SessionPatchRequestDTO body) throws NoContentException {
        Session oldSession = sessionService.getSession(id);
        oldSession.setRemoveSessionOn(body.getRemoveSessionOn());
        oldSession.setExpireLoginOn(body.getExpireSessionOn());
        logger.info("updating: " + body.toString());
        SessionDTO updatedDTO = sessionMapper
                .SessionToSessionDTO(sessionService.updateSession(id, oldSession));
        return new ResponseEntity<Object>(
                new SuccessResponseDTO(HttpStatus.OK.value(), HttpStatus.OK.toString(), updatedDTO), HttpStatus.OK);
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Object> deleteSession(@PathVariable("id") Long id) throws NoContentException {
        boolean isDeleted = this.sessionService.deleteSession(id);
        return new ResponseEntity<>(
                new SuccessResponseDTO(HttpStatus.OK.value(), HttpStatus.OK.toString(), isDeleted), HttpStatus.OK);
    }
}
