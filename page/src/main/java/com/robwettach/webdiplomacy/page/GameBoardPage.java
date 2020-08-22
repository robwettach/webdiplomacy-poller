package com.robwettach.webdiplomacy.page;

import static java.lang.String.format;

import com.google.auto.value.AutoValue;
import java.io.IOException;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Representation of a <em>webDiplomacy</em> game board.
 */
@AutoValue
public abstract class GameBoardPage {
    public abstract GameTitleBar getTitleBar();
    public abstract Optional<MembersTable> getMembersTable();

    /**
     * Load a given {@code gameId} from <a href="https://webDiplomacy.net>webDiplomacy.net</a>.
     *
     * <p>Makes an HTTP request to http://webdiplomacy.net/board.php?gameId={@code gameId}.
     *
     * @param gameId The ID of the game to load
     * @return A {@link GameBoardPage} instance
     * @throws IOException if there is an error downloading the game board data from the Internet
     */
    public static GameBoardPage loadGame(int gameId) throws IOException {
        return fromDocument(Jsoup.connect(format("http://webdiplomacy.net/board.php?gameID=%d", gameId)).get());
    }

    /**
     * Extract a {@link GameBoardPage} from an HTML {@link Document}.
     *
     * @param document The HTML {@link Document} containing the game board
     * @return A {@link GameBoardPage} instance
     */
    public static GameBoardPage fromDocument(Document document) {
        GameTitleBar titleBar = GameTitleBar.fromParent(document);
        Optional<MembersTable> membersTable = Optional.empty();
        if (!Constants.PRE_GAME.equals(titleBar.getPhase())) {
            membersTable = Optional.of(MembersTable.fromParent(document));
        }
        return new AutoValue_GameBoardPage(titleBar, membersTable);
    }
}
