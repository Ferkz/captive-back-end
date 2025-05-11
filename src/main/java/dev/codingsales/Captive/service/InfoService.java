package dev.codingsales.Captive.service;

import java.util.List;

import dev.codingsales.Captive.dto.item.BrowserCount;
import dev.codingsales.Captive.dto.item.ItemList;
import dev.codingsales.Captive.dto.item.OsCount;
public interface InfoService {

    /**
     * Gets the browsers.
     *
     * @return the browsers
     */
    public List<BrowserCount> getBrowsersCount();


    /**
     * Gets the available browsers.
     *
     * @return the available browsers
     */
    public List<String> getAvailableBrowsers();


    /**
     * Gets the available OS.
     *
     * @return the available OSes
     */
    public List<String> getAvailableOS();


    /**
     * Gets the os count.
     *
     * @return the os count
     */
    public List<OsCount> getOsCount();


    /**
     * Gets the last sessions.
     *
     * @return the last sessions
     */
    public ItemList getLastSessions(Boolean paging, Integer pageSize, Integer page, Integer lastDays);

}
