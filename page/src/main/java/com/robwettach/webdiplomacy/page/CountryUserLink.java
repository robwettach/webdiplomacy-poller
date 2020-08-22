package com.robwettach.webdiplomacy.page;

import static com.google.common.base.Verify.verify;
import static java.lang.String.format;

import com.google.auto.value.AutoValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;

/**
 * Representation of a player in the countries table.
 *
 * <p>Includes the player's name, ID and current points.
 */
@AutoValue
public abstract class CountryUserLink {
    private static final String POINTS_KEY = "points";
    private static final Pattern POINTS_PATTERN = Pattern.compile(
            format("\\((?<%s>\\d+) \\)", POINTS_KEY));

    public abstract int getId();
    public abstract String getName();
    public abstract int getPoints();

    /**
     * Extract a {@link CountryUserLink} instance from a given HTML {@link Element}.
     *
     * @param element The HTML {@link Element} containing the user link
     * @return A {@link CountryUserLink} instance
     */
    public static CountryUserLink fromElement(Element element) {
        Element userA = element.select("a").first();
        String name = userA.text();

        String pointsSpanText = element.select("span.points").first().text(); // (109 )
        Matcher pointsMatcher = POINTS_PATTERN.matcher(pointsSpanText);
        verify(pointsMatcher.find(), "Failed to parse points: %s", pointsSpanText);
        int points = Integer.parseInt(pointsMatcher.group(POINTS_KEY));

        return new AutoValue_CountryUserLink(UserProfileLink.fromElement(userA).getId(), name, points);
    }
}
