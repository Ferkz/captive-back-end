package dev.codingsales.Captive.controller;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    /** The session service. */
    @Autowired
    private SessionService sessionService;

    /** The logger. */
    private static Logger logger = Logger.getLogger(SessionController.class);

    /**
     * Gets the sessions.
     *
     * @param request the request
     * @return the sessions
     * @throws NoContentException the not found exception
     */
    @GetMapping("/sessions")
    public ResponseEntity<Object> getSessions() throws NoContentException {
        List<SessionDTO> sessionsDTO = new ArrayList<SessionDTO>();
        this.sessionService.getSessions().forEach(session -> {
            sessionsDTO.add(SessionMapper.INSTANCE.SessionToSessionDTO(session));
        });
        return new ResponseEntity<Object>(
                new SuccessResponseDTO(HttpStatus.OK.value(), HttpStatus.OK.toString(), sessionsDTO), HttpStatus.OK);
    }

    /**
     * Gets the session.
     *
     * @param id      the id
     * @param request the request
     * @return the session
     * @throws NoContentException the not found exception
     */
    @GetMapping("/sessions/{id}")
    public ResponseEntity<Object> getSession(@PathVariable("id") Long id)
            throws NoContentException {
        SessionDTO sessionDTO = SessionMapper.INSTANCE.SessionToSessionDTO(sessionService.getSession(id));
        return new ResponseEntity<Object>(
                new SuccessResponseDTO(HttpStatus.OK.value(), HttpStatus.OK.toString(), sessionDTO), HttpStatus.OK);
    }

    /**
     * Update session.
     *
     * @param id      the id
     * @param session the session
     * @param result  the result
     * @return the response entity
     * @throws NoContentException the not found exception
     */
    @ConditionalOnProperty(name = "jespresso.sessions.full_update_enable", matchIfMissing = false)
    @PutMapping("/sessions")
    public ResponseEntity<Object> updateSession(@PathVariable("id") Long id, @RequestBody @Valid Session session) throws NoContentException {
        SessionDTO updatedDTO = SessionMapper.INSTANCE.SessionToSessionDTO(sessionService.updateSession(id, session));
        return new ResponseEntity<Object>(
                new SuccessResponseDTO(HttpStatus.OK.value(), HttpStatus.OK.toString(), updatedDTO), HttpStatus.OK);
    }


    /**
     * Update session.
     *
     * @param id the id
     * @param body the body
     * @param result the result
     * @return the response entity
     * @throws NoContentException the not found exception
     */
    @PatchMapping("/sessions/{id}")
    public ResponseEntity<Object> updateSession(@PathVariable("id") Long id, @RequestBody SessionPatchRequestDTO body) throws NoContentException {
        Session oldSession = sessionService.getSession(id);
        oldSession.setRemoveSessionOn(body.getRemoveSessionOn());
        oldSession.setExpireLoginOn(body.getExpireSessionOn());
        logger.info("updating: " + body.toString());
        SessionDTO updatedDTO = SessionMapper.INSTANCE
                .SessionToSessionDTO(sessionService.updateSession(id, oldSession));
        return new ResponseEntity<Object>(
                new SuccessResponseDTO(HttpStatus.OK.value(), HttpStatus.OK.toString(), updatedDTO), HttpStatus.OK);
    }

    /**
     * Delete session.
     *
     * @param id      the id
     * @param request the request
     * @return the response entity
     * @throws NoContentException the not found exception
     */
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Object> deleteSession(@PathVariable("id") Long id)
            throws NoContentException {
        boolean isDeleted = this.sessionService.deleteSession(id);
        return new ResponseEntity<Object>(
                new SuccessResponseDTO(HttpStatus.OK.value(), HttpStatus.OK.toString(), isDeleted), HttpStatus.OK);
    }

    /**
     * Gets the expired sessions.
     *
     * @param request the request
     * @return the expired sessions
     */
    @GetMapping("/sessions/expired")
    public ResponseEntity<Object> getExpiredSessions() {
        List<Session> result = this.sessionService.getExpiredSessions();
        List<SessionDTO> resultDTO = new ArrayList<SessionDTO>();
        result.forEach(session -> {
            resultDTO.add(SessionMapper.INSTANCE.SessionToSessionDTO(session));
        });
        return new ResponseEntity<Object>(
                new SuccessResponseDTO(HttpStatus.OK.value(), HttpStatus.OK.toString(), resultDTO), HttpStatus.OK);
    }

    /**
     * Gets the valid sessions.
     *
     * @param request the request
     * @return the valid sessions
     */
    @GetMapping("/sessions/valid")
    public ResponseEntity<Object> getValidSessions() {
        List<Session> result = this.sessionService.getValidSessions();
        List<SessionDTO> resultDTO = new ArrayList<SessionDTO>();
        result.forEach(session -> {
            resultDTO.add(SessionMapper.INSTANCE.SessionToSessionDTO(session));
        });
        return new ResponseEntity<Object>(
                new SuccessResponseDTO(HttpStatus.OK.value(), HttpStatus.OK.toString(), resultDTO), HttpStatus.OK);
    }
}
