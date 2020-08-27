package com.robwettach.webdiplomacy.page;

import com.google.auto.value.AutoValue;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.jsoup.nodes.Element;

/**
 * Representation of a <em>webDiplomacy</em> game's title bar.
 */
@AutoValue
public abstract class GameTitleBar {
    public abstract String getName();
    public abstract String getDate();
    public abstract String getPhase();
    public abstract boolean isPaused();
    public abstract boolean isFinished();
    public abstract Optional<ZonedDateTime> getNextTurnAt();

    /**
     * Extract a {@link GameTitleBar} from a parent HTML {@link Element}.
     *
     * <p>The {@code parent} element is expected to contain a {@code div} with class {@code titleBar}.
     *
     * @param parent The parent HTML {@link Element} containing the title bar
     * @return A {@link GameTitleBar} instance
     */
    public static GameTitleBar fromParent(Element parent) {
        return fromElement(parent.select(".titleBar").first());
    }

    /**
     * Extract a {@link GameTitleBar} from an HTML {@link Element}.
     *
     * @param element The HTML {@link Element} containing the title bar
     * @return A {@link GameTitleBar} instance
     */
    public static GameTitleBar fromElement(Element element) {
        String name = element.select(".gameName").first().text();
        String date = element.select(".gameDate").first().text();
        String phase = element.select(".gamePhase").first().text();

        boolean paused = false;
        boolean finished = false;
        Optional<ZonedDateTime> nextTurnAt = Optional.empty();

        Element gameTimeRemaining = element.select(".gameTimeRemaining").first();
        if (gameTimeRemaining.text().startsWith("Paused")) {
            paused = true;
        } else if (gameTimeRemaining.text().startsWith("Finished")) {
            finished = true;
        } else {
            String next = element.select("span.timeremaining").first().attr("unixtime");
            nextTurnAt = Optional.of(ZonedDateTime.ofInstant(
                    Instant.ofEpochSecond(Long.parseLong(next)),
                    ZoneOffset.UTC));
        }
        return new AutoValue_GameTitleBar(name, date, phase, paused, finished, nextTurnAt);
    }
}
