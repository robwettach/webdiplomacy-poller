package com.robwettach.webdiplomacy.page;

import com.google.auto.value.AutoValue;
import org.jsoup.nodes.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Verify.verify;
import static java.lang.String.format;

@AutoValue
public abstract class UserProfileLink {
    private static final String USER_ID_KEY = "userId";
    private static final Pattern PROFILE_USER_ID_PATTERN = Pattern.compile(
            format("profile\\.php\\?userID=(?<%s>\\d+)", USER_ID_KEY));

    public abstract int getId();

    public static UserProfileLink fromElement(Element element) {
        String profileLink = element.attr("href"); //./profile.php?userID=1234
        Matcher profileMatcher = PROFILE_USER_ID_PATTERN.matcher(profileLink);
        verify(profileMatcher.find(), "Failed to parse profile link: %s", profileLink);
        int userId = Integer.parseInt(profileMatcher.group(USER_ID_KEY));
        return new AutoValue_UserProfileLink(userId);
    }
}
