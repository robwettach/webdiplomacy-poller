package com.robwettach.webdiplomacy.page;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Representation of a list of <em>webDiplomacy</em> games from the {@code gamelistings.php} page.
 */
@AutoValue
public abstract class GameListingsPage {
    private static final Logger LOG = LogManager.getLogger(GameListingsPage.class);

    public abstract ImmutableMap<Integer, GamePanel> getGamePanels();

    /**
     * Load a list of games for the current user, as represented by {@code cookies}.
     *
     * <p>Makes an authenticated HTTP request to http://webdiplomacy.net/gamelistings.php?gamelistType=My%20games.
     *
     * @param cookies The map of HTTP cookies that authenticate a given user
     * @return A {@link GameListingsPage} instance
     * @throws IOException if there is an error downloading the game listing data from the Internet
     */
    public static GameListingsPage load(Map<String, String> cookies) throws IOException {
        String url = "http://webdiplomacy.net/gamelistings.php?gamelistType=My%20games";
        LOG.debug("Loading games from {}", url);
        return fromDocument(Jsoup.connect(url)
                .cookies(cookies)
                .get());
    }

    /**
     * Extract a {@link GameListingsPage} from an HTML {@link Document}.
     *
     * @param document The HTML {@link Document} containing the game listings
     * @return A {@link GameListingsPage} instance
     */
    public static GameListingsPage fromDocument(Document document) {
        Elements games = document.select(".gamesList .gamePanel");
        return new AutoValue_GameListingsPage(games.stream()
                .map(GamePanel::fromElement)
                .collect(toImmutableMap(GamePanel::getId, Function.identity())));
    }
}
