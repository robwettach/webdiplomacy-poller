package com.robwettach.webdiplomacy.poller;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verifyNotNull;
import static com.robwettach.webdiplomacy.json.Json.OBJECT_MAPPER;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.robwettach.webdiplomacy.poller.lib.CookieProvider;
import java.io.Console;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * {@link CookieProvider} that manages cookies on local disk.
 *
 * <p>On first run, asks for a username and password that it immediately exchanges for cookies from
 * <a href="https://webDiplomacy.net">webDiplomacy.net</a>.  Cookies are stored to local disk and read on future runs.
 *
 * <p>Writes cookies to {@code WEBDIP_POLLER_HOME/cookies.json}.
 */
public class LocalCookieProvider implements CookieProvider {
    private static final String COOKIES_FILE_NAME = "cookies.json";

    private final Path cookiesPath;

    public LocalCookieProvider(Path configDirPath) {
        checkNotNull(configDirPath, "configDirPath must not be null");
        this.cookiesPath = configDirPath.resolve(COOKIES_FILE_NAME);
    }

    @Override
    public Map<String, String> getCookies() {
        if (Files.exists(cookiesPath)) {
            System.out.println("Loading cookies from: " + cookiesPath);
            try {
                return OBJECT_MAPPER.readValue(cookiesPath.toFile(), new TypeReference<>() {});
            } catch (IOException e) {
                System.err.println("Failed to read cookies from: " + cookiesPath);
                e.printStackTrace();
                return null;
            }
        } else {
            Console console = System.console();
            verifyNotNull(console, "This command must be run from a console to receive password input!");

            String username = console.readLine("Username: ");
            String password = new String(console.readPassword("Password: "));

            HttpRequest loginRequest;
            try {
                loginRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://webdiplomacy.net/index.php"))
                        .POST(HttpRequest.BodyPublishers.ofString(
                                format(
                                        "loginuser=%s&loginpass=%s",
                                        URLEncoder.encode(username, StandardCharsets.UTF_8.toString()),
                                        URLEncoder.encode(password, StandardCharsets.UTF_8.toString()))))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .build();
            } catch (UnsupportedEncodingException e) {
                System.err.println("Failed to encode login body");
                e.printStackTrace();
                return null;
            }
            HttpResponse<String> loginResponse;
            try {
                HttpClient client = HttpClient.newHttpClient();
                loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                System.err.println("Failed to login");
                e.printStackTrace();
                return null;
            }
            Map<String, String> cookies = loginResponse.headers()
                    .allValues("set-cookie")
                    .stream()
                    .map(HttpCookie::parse)
                    .flatMap(List::stream)
                    .collect(toMap(HttpCookie::getName, HttpCookie::getValue));
            try {
                OBJECT_MAPPER.writeValue(cookiesPath.toFile(), cookies);
            } catch (IOException e) {
                System.err.println("Failed to store cookies to: " + cookiesPath);
                e.printStackTrace();
            }
            return cookies;
        }
    }
}
