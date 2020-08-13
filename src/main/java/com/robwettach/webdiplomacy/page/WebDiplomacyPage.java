package com.robwettach.webdiplomacy.page;

import com.robwettach.webdiplomacy.model.UserInfo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Verify.verify;
import static java.lang.String.format;

public class WebDiplomacyPage {
    private static final String POINTS_KEY = "points";
    private static final String USER_ID_KEY = "userId";
    private static final String USER_NAME_KEY = "userName";

    private static final Pattern PROFILE_USER_ID_PATTERN = Pattern.compile(
            format("profile\\.php\\?userID=(?<%s>\\d+)", USER_ID_KEY));
    private static final Pattern USERNAME_POINTS_PATTERN = Pattern.compile(
            format("(?<%s>\\w+) \\((?<%s>\\d+) \\)", USER_NAME_KEY, POINTS_KEY));
    private final Document doc;

    public WebDiplomacyPage(Document doc) {
        this.doc = doc;
    }

    protected final Document getDocument() {
        return doc;
    }

    public Optional<UserInfo> getCurrentlyLoggedInUser() {
        Element headerWelcome = doc.select("#header-welcome").first();
        Element userA = headerWelcome.select("a[href^=./profile.php]").first();
        if (userA != null) {
            int userId = getUserIdFromATag(userA);

            String usernamePoints = userA.text(); // robwettach (109 )
            Matcher unameMatcher = USERNAME_POINTS_PATTERN.matcher(usernamePoints);
            verify(unameMatcher.find(), "Failed to parse username: %s", usernamePoints);

            String name = unameMatcher.group(USER_NAME_KEY);

            return Optional.of(UserInfo.create(name, userId));
        } else {
            return Optional.empty();
        }
    }

    protected int getUserIdFromATag(Element userA) {
        String profileLink = userA.attr("href"); //./profile.php?userID=1234
        Matcher profileMatcher = PROFILE_USER_ID_PATTERN.matcher(profileLink);
        verify(profileMatcher.find(), "Failed to parse profile link: %s", profileLink);
        int userId = Integer.parseInt(profileMatcher.group(USER_ID_KEY));
        return userId;
    }
}
