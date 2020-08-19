package com.robwettach.webdiplomacy.page;

import com.robwettach.webdiplomacy.model.GamePhase;
import com.robwettach.webdiplomacy.model.GameState;
import com.robwettach.webdiplomacy.poller.LocalCookieProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Verify.verify;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

public class GameListingsPage extends WebDiplomacyPage {
    public static final String GAME_ID_KEY = "id";

    private static final Pattern GAME_ID_PATTERN = Pattern.compile(
            format("board\\.php\\?gameID=(?<%s>\\d+)", GAME_ID_KEY));

    public GameListingsPage(Map<String, String> cookies) throws IOException {
        this(Jsoup.connect("http://webdiplomacy.net/gamelistings.php?gamelistType=My%20games")
                .cookies(cookies)
                .get());
    }

    public GameListingsPage(Document doc) {
        super(doc);
    }

    public Map<Integer, GameState> getGames() {
        Elements games = getDocument().select(".gamesList .gamePanel");
        return games.stream().map(this::parseGameState)
                .collect(toMap(GameState::getId, Function.identity()));
    }

    private GameState parseGameState(Element g) {
        String openGameUrl = g.select(".enterBarOpen > a").first().attr("href");
        Matcher gameIdMatcher = GAME_ID_PATTERN.matcher(openGameUrl);
        verify(gameIdMatcher.find(), "Failed to parse game ID: %s", openGameUrl);
        int gameId = Integer.parseInt(gameIdMatcher.group(GAME_ID_KEY));

        GameTitleBar titleBar = new GameTitleBar(g);
        GameState globalState = titleBar.getGameGlobalState(gameId);
        GameState.Builder gameBuilder = globalState.toBuilder();

        if (!GamePhase.PreGame.equals(globalState.getPhase())) {
            MembersTable membersTable = new MembersTable(this, g);
            gameBuilder.countries(membersTable.getCountries());
        }
        return gameBuilder.build();
    }
}
