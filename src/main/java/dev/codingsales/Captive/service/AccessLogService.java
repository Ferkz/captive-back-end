package dev.codingsales.Captive.service;

import java.util.List;

import dev.codingsales.Captive.entity.AccessLog;
import dev.codingsales.Captive.exeption.AlreadyExistsException;
import dev.codingsales.Captive.exeption.NoContentException;

public interface AccessLogService {
    /**
     * Adds the access log.
     *
     * @param accessLog the access log
     * @return the access log
     * @throws AlreadyExistsException the already exists exception
     */
    public AccessLog addAccessLog(AccessLog accessLog);

    /**
     * Update access log.
     *
     * @param id the id
     * @param accessLog the access log
     * @return the user
     * @throws NoContentException the not found exception
     */
    public AccessLog updateAccessLog(Long id, AccessLog accessLog) throws NoContentException;

    /**
     * Delete access log.
     *
     * @param id the id
     * @return true, if successful
     * @throws NoContentException the not found exception
     */
    public boolean deleteAccessLog(Long id) throws NoContentException;

    /**
     * Gets the access log.
     *
     * @param id the id
     * @return the access log
     * @throws NoContentException the not found exception
     */
    public AccessLog getAccessLog(Long id) throws NoContentException;

    /**
     * Gets the access logs.
     *
     * @return the access logs
     * @throws NoContentException the not found exception
     */
    public List<AccessLog> getAccessLogs() throws NoContentException;
}
