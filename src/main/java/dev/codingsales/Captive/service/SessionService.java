package dev.codingsales.Captive.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import dev.codingsales.Captive.dto.item.BrowserCount;
import dev.codingsales.Captive.dto.item.ItemList;
import dev.codingsales.Captive.dto.item.OsCount;
import dev.codingsales.Captive.entity.Session;
import dev.codingsales.Captive.exeption.AlreadyExistsException;
import dev.codingsales.Captive.exeption.NoContentException;

public interface SessionService {
    /**
     * Adds the user.
     *
     * @param session the session
     * @return the user
     * @throws AlreadyExistsException the already exists exception
     */
    public Session addSession(Session session) throws AlreadyExistsException;

    /**
     * Update user.
     *
     * @param id the id
     * @param session the session
     * @return the user
     * @throws NoContentException the not found exception
     */
    public Session updateSession(Long id, Session session) throws NoContentException;

    /**
     * Delete user.
     *
     * @param id the id
     * @return the boolean
     * @throws NoContentException the not found exception
     */
    public Boolean deleteSession(Long id) throws NoContentException;

    /**
     * Gets the user.
     *
     * @param id the id
     * @return the user
     * @throws NoContentException the not found exception
     */
    public Session getSession(Long id) throws NoContentException;


    /**
     * Gets the sessions.
     *
     * @param usePaging the use paging
     * @param page the page
     * @param size the size
     * @return the sessions
     * @throws NoContentException the no content exception
     */
    public List<Session> getSessions(Boolean usePaging, Integer page, Integer size) throws NoContentException;


    /**
     * Exists by device mac.
     *
     * @param deviceMac the device mac
     * @return true, if successful
     */
    public boolean existsByDeviceMac(String deviceMac);


    /**
     * Find by device mac.
     *
     * @param deviceMac the device mac
     * @return the session
     * @throws NoContentException the not found exception
     */
    public Session findByDeviceMac(String deviceMac) throws NoContentException;
    /**
     * Checks if is expired.
     *
     * @param session the session
     * @return true, if is expired
     */
    public boolean isExpired(Session session);


    /**
     * Find all.
     *
     * @return the list
     * @throws NoContentException
     */
    public List<Session> getSessions() throws NoContentException;


    /**
     * Gets the browsers.
     *
     * @return the browsers
     */
    public List<String> getAvailableBrowsers();


    /**
     * Gets the expired sessions.
     *
     * @return the expired sessions
     */
    public List<Session> getExpiredSessions();


    /**
     * Gets the valid sessions.
     *
     * @return the valid sessions
     */
    public List<Session> getValidSessions();

    /**
     * Gets the available OS.
     *
     * @return the available OS
     */
    public List<String> getAvailableOS();

    /**
     * Gets the browsers count.
     *
     * @return the browsers count
     */
    public List<BrowserCount> getBrowsersCount();

    /**
     * Gets the os count.
     *
     * @return the os count
     */
    public List<OsCount> getOsCount();


    /**
     * Gets the last sessions between.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param usePaging the use paging
     * @param page the page
     * @param size the size
     * @return the last sessions between
     */
    public ItemList getSessionsBetween(Timestamp startDate, Timestamp endDate, Boolean usePaging, Integer page,
                                       Integer size);


    /**
     * Clean session table removing old unblocked session. (Devices can reconnect again)
     */
    public void cleanSessionTable();
    /**
     * Tenta encontrar uma sessão válida por email e MAC.
    // * @param email o email do convidado
    // * @param deviceMac o MAC do dispositivo
    // * @return um Optional contendo a sessão se encontrada e válida, ou Optional.empty()
    // */
    //Optional<Session> findValidSessionByEmailAndMac(String email, String deviceMac);
   // /**
    // * Tenta encontrar uma sessão válida por email e MAC.
    // * @param cpf o email do convidado
     //* @param deviceMac o MAC do dispositivo
     //* @return um Optional contendo a sessão se encontrada e válida, ou Optional.empty()
     //*/
   // O/tional<Session> findValidSessionByCpfAndMac(String cpf, String deviceMac);

   // /**
   //  * Tenta encontrar uma sessão por email, independente do MAC.
    // * @param email o email do convidado
    // * @return um Optional contendo a sessão se encontrada, ou Optional.empty()
    // */
    //Optional<Session> findByEmail(String email);

   // /**
    // * Finds a Session by the given CPF
   //  * @param cpf
    // * @return An Optional containing the Session if found, or empty if not.
    // */
    // Optional<Session> findByCpf(String cpf);
}
