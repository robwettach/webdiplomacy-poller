package com.robwettach.webdiplomacy.poller;

import java.util.Map;

public interface CookieProvider {
    Map<String, String> getCookies();
}
