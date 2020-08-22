package com.robwettach.webdiplomacy.poller.lib;

import java.util.Map;

/**
 * Simple interface for retrieving authentication cookies for a user.
 */
public interface CookieProvider {
    /**
     * Get authentication cookies for a user.
     *
     * @return The HTTP cookies required to authenticate a user on
     *         <a href="https://webDiplomacy.net>webDiplomacy.net</a>
     */
    Map<String, String> getCookies();
}
