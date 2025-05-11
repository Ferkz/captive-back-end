package dev.codingsales.Captive.schedule;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import dev.codingsales.Captive.service.SessionService;

@Service
public class SessionScheduleService {
    /** The session service. */
    @Autowired
    private SessionService sessionService;

    /**
     * Removes the blocked session.
     */
    @Scheduled(fixedDelayString = "${jespresso.sessions.cleantable.delay}", initialDelay = 15000)
    @Transactional
    public void removeBlockedSession() {
        this.sessionService.cleanSessionTable();
    }
}
