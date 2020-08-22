package com.robwettach.webdiplomacy.page;

import static java.lang.String.format;

import com.google.auto.value.AutoValue;
import java.io.IOException;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@AutoValue
public abstract class GameBoardPage {
    public abstract GameTitleBar getTitleBar();
    public abstract Optional<MembersTable> getMembersTable();

    public static GameBoardPage loadGame(int gameId) throws IOException {
        return fromDocument(Jsoup.connect(format("http://webdiplomacy.net/board.php?gameID=%d", gameId)).get());
    }

    public static GameBoardPage fromDocument(Document document) {
        GameTitleBar titleBar = GameTitleBar.fromParent(document);
        Optional<MembersTable> membersTable = Optional.empty();
        if (!Constants.PRE_GAME.equals(titleBar.getPhase())) {
            membersTable = Optional.of(MembersTable.fromParent(document));
        }
        return new AutoValue_GameBoardPage(titleBar, membersTable);
    }
}
