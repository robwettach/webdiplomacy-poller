package com.robwettach.webdiplomacy.page;

import static com.google.common.base.Verify.verify;
import static java.lang.String.format;

import com.google.auto.value.AutoValue;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Representation of the current user from the header of <a href="https://webDiplomacy.net">webDiplomacy.net</a>.
 */
@AutoValue
public abstract class CurrentUserLink {
    private static final String POINTS_KEY = "points";
    private static final String USER_NAME_KEY = "userName";
    private static final Pattern USERNAME_POINTS_PATTERN = Pattern.compile(
            format("(?<%s>\\w+) \\((?<%s>\\d+) \\)", USER_NAME_KEY, POINTS_KEY));

    public abstract int getId();
    public abstract String getName();
    public abstract int getPoints();

    /**
     * Extract a {@link CurrentUserLink} from a given HTML {@link Document}.
     *
     * <p>A user may not be logged in, so the result is {@link Optional}.
     *
     * @param document The HTML {@link Document} containing the user link
     * @return An {@link Optional} containing the {@link CurrentUserLink}, if present.  Else, {@link Optional#empty()}.
     */
    public static Optional<CurrentUserLink> fromDocument(Document document) {
        Element headerWelcome = document.select("#header-welcome").first();
        Element userA = headerWelcome.select("a[href^=./profile.php]").first();
        if (userA != null) {
            return Optional.of(CurrentUserLink.fromElement(userA));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Extract a {@link CurrentUserLink} from a given HTML {@link Element}.
     *
     * @param element The HTML {@link Element} containing the user link
     * @return A {@link CurrentUserLink} instance
     */
    public static CurrentUserLink fromElement(Element element) {
        String usernamePoints = element.text(); // robwettach (109 )
        Matcher unamePointsMatcher = USERNAME_POINTS_PATTERN.matcher(usernamePoints);
        verify(unamePointsMatcher.find(), "Failed to parse username: %s", usernamePoints);

        String name = unamePointsMatcher.group(USER_NAME_KEY);
        int points = Integer.parseInt(unamePointsMatcher.group(POINTS_KEY));
        return new AutoValue_CurrentUserLink(UserProfileLink.fromElement(element).getId(), name, points);
    }
}
