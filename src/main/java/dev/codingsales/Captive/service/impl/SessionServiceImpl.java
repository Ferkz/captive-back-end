package dev.codingsales.Captive.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import dev.codingsales.Captive.dto.item.BrowserCount;
import dev.codingsales.Captive.dto.item.ItemList;
import dev.codingsales.Captive.dto.item.OsCount;
import dev.codingsales.Captive.entity.Session;
import dev.codingsales.Captive.exeption.AlreadyExistsException;
import dev.codingsales.Captive.exeption.NoContentException;
import dev.codingsales.Captive.mapper.AccessLogMapper;
import dev.codingsales.Captive.repository.AccessLogRepository;
import dev.codingsales.Captive.repository.SessionRepository;
import dev.codingsales.Captive.service.SessionService;
import dev.codingsales.Captive.util.DateUtils;
import dev.codingsales.Captive.util.LoggerConstants;

@Service
public class SessionServiceImpl implements SessionService{
    /** The session repository. */
    @Autowired
    private SessionRepository sessionRepository;

    /** The access logs enabled. */
    private boolean accessLogsEnabled = false;

    /** The logger. */
    private static Logger logger = Logger.getLogger(SessionServiceImpl.class);

    /** The access log repository. */
    @Autowired
    private AccessLogRepository accessLogRepository;

    /**
     * Instantiates a new session service impl.
     *
     * @param accessLogsEnabled the access logs enabled
     */
    public SessionServiceImpl(@Value("${logs.access-sessions.enable}") boolean accessLogsEnabled) {
        this.accessLogsEnabled = accessLogsEnabled;
    }

    /**
     * Adds the session.
     *
     * @param session the session
     * @return the session
     * @throws AlreadyExistsException the already exists exception
     */
    @Override
    @Transactional
    public Session addSession(Session session) throws AlreadyExistsException {
        throwsIfExists(session);

        Session result = sessionRepository.save(session);
        if (result != null && accessLogsEnabled) {
            this.accessLogRepository.save(AccessLogMapper.INSTANCE.sessionToAccessLog(session));
        }
        return result;
    }


    /**
     * Throws if exists.
     *
     * @param session the session
     * @throws AlreadyExistsException the already exists exception
     */
    private void throwsIfExists(Session session) throws AlreadyExistsException {
        if (session != null && session.getId() != null) {
            Optional<Session> sessionFromDb = sessionRepository.findById(session.getId());
            if (sessionFromDb.isPresent()) {
                throw new AlreadyExistsException(String.format(LoggerConstants.ALREADY_EXISTS_EXCEPTION,
                        "SessionServiceImpl", "addSession", "session"));
            }
        }
    }
    //@Override
    //public Optional<Session> findValidSessionByEmailAndMac(String email, String deviceMac) {
        // Encontra a sessão onde o email e MAC correspondem e a sessão ainda não expirou
      //  return sessionRepository.findByEmailAndDeviceMacAndExpireLoginOnGreaterThan(email, deviceMac, new Date());
   // }

    //Override
   // public Optional<Session> findByEmail(String email) {
    //    return sessionRepository.findByEmail(email);
    //}
    /**
     * Update session.
     *
     * @param id      the id
     * @param session the session
     * @return the session
     * @throws NoContentException the not found exception
     */
    @Override
    public Session updateSession(Long id, Session session) throws NoContentException {
        if (sessionRepository.existsById(id)) {
            session = sessionRepository.save(session);
        } else {
            String message = String.format(LoggerConstants.NOT_FOUND_EXCEPTION, this.getClass().getName(),
                    "updateSession", "session", id);
            logger.error(message);
            throw new NoContentException(message);
        }
        return session;
    }

    /**
     * Delete session.
     *
     * @param id the id
     * @return the boolean
     * @throws NoContentException the not found exception
     */
    @Override
    public Boolean deleteSession(Long id) throws NoContentException {
        boolean deleted = false;
        if (sessionRepository.existsById(id)) {
            sessionRepository.deleteById(id);
            deleted = !sessionRepository.existsById(id);
        } else {
            String message = String.format(LoggerConstants.NOT_FOUND_EXCEPTION, this.getClass().getName(), "Session",
                    "session", id);
            logger.error(message);
            throw new NoContentException(message);
        }
        return deleted;
    }

    /**
     * Gets the session.
     *
     * @param id the id
     * @return the session
     * @throws NoContentException the not found exception
     */
    @Override
    public Session getSession(Long id) throws NoContentException {
        Optional<Session> sessionFromDb = sessionRepository.findById(id);
        if (sessionFromDb.isPresent()) {
            return sessionFromDb.get();
        } else {
            String message = String.format(LoggerConstants.NOT_FOUND_EXCEPTION, this.getClass().getName(), "Session",
                    "session", id);
            logger.error(message);
            throw new NoContentException(message);
        }
    }

    /**
     * Gets the sessions.
     *
     * @param usePaging the use paging
     * @param page the page
     * @param size the size
     * @return the sessions
     * @throws NoContentException the not found exception
     */
    @Override
    public List<Session> getSessions(Boolean usePaging, Integer page, Integer size) throws NoContentException {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<Session> sessions = usePaging.booleanValue() ? this.sessionRepository.findAll(pageRequest).getContent() :
                this.sessionRepository.findAll();
        if (!sessions.isEmpty()) {
            return sessions;
        } else {
            String message = String.format(LoggerConstants.NOT_FOUND_EXCEPTION, this.getClass().getName(), "Session",
                    "session");
            logger.error(message);
            throw new NoContentException(message);
        }
    }

    /**
     * Exists by device mac.
     *
     * @param deviceMac the device mac
     * @return true, if successful
     */
    @Override
    public boolean existsByDeviceMac(String deviceMac) {
        return sessionRepository.findByDeviceMac(deviceMac).isPresent();
    }
    /**
     * Find by device mac.
     *
     * @param deviceMac the device mac
     * @return the session
     * @throws NoContentException the not found exception
     */
    @Override
    public Session findByDeviceMac(String deviceMac) throws NoContentException {
        Optional<Session> sessionFromDb = sessionRepository.findByDeviceMac(deviceMac);
        if (sessionFromDb.isPresent()) {
            return sessionFromDb.get();
        } else {
            String error = String.format(LoggerConstants.NOT_FOUND_EXCEPTION, "SessionServiceImpl", "findByDeviceMac",
                    "session", 0);
            logger.error(error);
            throw new NoContentException(error);
        }
    }

    /**
     * Checks if is expired.
     *
     * @param session the session
     * @return true, if is expired
     */
    public boolean isExpired(Session session) {
        return session.getExpireLoginOn().after(session.getLastLoginOn());
    }

    /**
     * Gets the available browsers.
     *
     * @return the available browsers
     */
    @Override
    public List<String> getAvailableBrowsers() {
        return this.sessionRepository.findAllBrowsers();
    }

    /**
     * Gets the expired sessions.
     *
     * @return the expired sessions
     */
    @Override
    public List<Session> getExpiredSessions() {
        return this.sessionRepository.findAllByExpireLoginOnLessThanEqual(new Date());
    }

    /**
     * Gets the valid sessions.
     *
     * @return the valid sessions
     */
    @Override
    public List<Session> getValidSessions() {
        return this.sessionRepository.findAllByExpireLoginOnGreaterThan(new Date());
    }

    /**
     * Gets the available OS.
     *
     * @return the available OS
     */
    @Override
    public List<String> getAvailableOS() {
        return this.sessionRepository.findAllOs();
    }

    /**
     * Gets the browsers count.
     *
     * @return the browsers count
     */
    @Override
    public List<BrowserCount> getBrowsersCount() {
        List<Object[]> results = this.sessionRepository.getBrowsersCount();
        List<BrowserCount> summary = new ArrayList<>();
        results.forEach(result -> {
            summary.add(new BrowserCount(result[0].toString(), Integer.parseInt(result[1].toString())));
        });
        return summary;
    }

    /**
     * Gets the browsers count.
     *
     * @return the browsers count
     */
    @Override
    public List<OsCount> getOsCount() {
        List<Object[]> results = this.sessionRepository.getOsCount();
        List<OsCount> summary = new ArrayList<>();
        results.forEach(result -> {
            summary.add(new OsCount(result[0].toString(), Integer.parseInt(result[1].toString())));
        });
        return summary;
    }

    /**
     * Gets the sessions between.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @param usePaging the use paging
     * @param page the page
     * @param size the size
     * @return the sessions between
     */
    @Override
    public ItemList getSessionsBetween(Timestamp startDate, Timestamp endDate, Boolean usePaging, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<Session> items = usePaging.booleanValue()
                ? sessionRepository.findAllByLastLoginOnBetweenOrderByLastLoginOnDesc(startDate, endDate, pageRequest)
                : sessionRepository.findAllByLastLoginOnBetweenOrderByLastLoginOnDesc(startDate, endDate);
        ItemList item = new ItemList(pageRequest.getPageSize(), pageRequest.getPageNumber(), items);
        item.setPages(pageRequest.getPageSize());
        item.setCurrentPage(pageRequest.getPageNumber());
        return item;
    }


    /**
     * Clean session table removing old unblocked session. (Devices can reconnect again)
     */
    @Override
    public void cleanSessionTable() {
        logger.info("cleanSessionTable(): removing old unblocked sessions..");
        this.sessionRepository.deleteAllByRemoveSessionOnLessThanEqual(DateUtils.getCurrentTimestamp());
    }

    /**
     * Gets the sessions.
     *
     * @return the sessions
     * @throws NoContentException the no content exception
     */
    @Override
    public List<Session> getSessions() throws NoContentException {
        return this.getSessions(false, 1, 1);
    }

   /**
    *  //@Override
    *     public Optional<Session> findByCpf(String cpf){
    *         return sessionRepository.findByCpf(cpf);
    *     }
    *     //@Override
    *     public Optional<Session>findValidSessionByCpfAndMac(String cpf, String deviceMac){
    *         return  sessionRepository.findByCpfAndDeviceMacAndExpireLoginOnGreaterThan(cpf,deviceMac, new Date());
    *     }
    * */
}
