package dev.codingsales.Captive.service.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.codingsales.Captive.dto.item.BrowserCount;
import dev.codingsales.Captive.dto.item.ItemList;
import dev.codingsales.Captive.dto.item.OsCount;
import dev.codingsales.Captive.service.InfoService;
import dev.codingsales.Captive.service.SessionService;
import dev.codingsales.Captive.util.DateUtils;

@Service
public class InfoServiceImpl implements InfoService {
    /** The session service. */
    @Autowired
    private SessionService sessionService;


    /**
     * Gets the browsers count.
     *
     * @return the browsers count
     */
    @Override
    public List<BrowserCount> getBrowsersCount() {
        return this.sessionService.getBrowsersCount();
    }

    /**
     * Gets the available browsers.
     *
     * @return the available browsers
     */
    @Override
    public List<String> getAvailableBrowsers() {
        return sessionService.getAvailableBrowsers();
    }

    /**
     * Gets the available OS.
     *
     * @return the available OS
     */
    @Override
    public List<String> getAvailableOS() {
        return this.sessionService.getAvailableOS();
    }

    /**
     * Gets the browsers count.
     *
     * @return the browsers count
     */
    @Override
    public List<OsCount> getOsCount() {
        return this.sessionService.getOsCount();
    }

    @Override
    public ItemList getLastSessions(Boolean paging, Integer pageSize, Integer page, Integer lastDays) {
        Date startDate = DateUtils.sumDays(new Date(), (-1)*lastDays);
        Date endDate = new Date();
        return sessionService.getSessionsBetween(new Timestamp(startDate.getTime()),
                new Timestamp(endDate.getTime()), paging, page, pageSize);
    }
}
