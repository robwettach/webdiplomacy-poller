package com.robwettach.webdiplomacy.page;

import com.robwettach.webdiplomacy.model.GamePhase;
import com.robwettach.webdiplomacy.model.GameState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import static java.lang.String.format;

public class GameBoardPage extends WebDiplomacyPage {
    private final int gameId;

    public GameBoardPage(int gameId) throws IOException {
        this(gameId, Jsoup.connect(format("http://webdiplomacy.net/board.php?gameID=%d", gameId)).get());
    }

    public GameBoardPage(int gameId, Document doc) {
        super(doc);
        this.gameId = gameId;
    }

    public GameState getGame() {
        Document doc = getDocument();
        GameTitleBar titleBar = new GameTitleBar(doc);
        GameState globalState = titleBar.getGameGlobalState(gameId);
        GameState.Builder gameBuilder = globalState.toBuilder();

        if (!GamePhase.PreGame.equals(globalState.getPhase())) {
            gameBuilder.countries(new MembersTable(this, doc).getCountries());
        }
        return gameBuilder.build();
    }
}
