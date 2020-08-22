package com.robwettach.webdiplomacy.page;

import static com.google.common.base.Verify.verify;
import static java.lang.String.format;

import com.google.auto.value.AutoValue;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;

/**
 * Representation of a single game panel on a <em>webDiplomacy</em> game listings page.
 */
@AutoValue
public abstract class GamePanel {
    private static final String GAME_ID_KEY = "id";
    private static final Pattern GAME_ID_PATTERN = Pattern.compile(
            format("board\\.php\\?gameID=(?<%s>\\d+)", GAME_ID_KEY));

    public abstract int getId();
    public abstract GameTitleBar getTitleBar();
    public abstract Optional<MembersTable> getMembersTable();

    /**
     * Extract a {@link GamePanel} from an HTML {@link Element}.
     *
     * @param element The HTML {@link Element} containing the game panel
     * @return A {@link GamePanel} instance
     */
    public static GamePanel fromElement(Element element) {
        String openGameUrl = element.select(".enterBarOpen > a").first().attr("href");
        Matcher gameIdMatcher = GAME_ID_PATTERN.matcher(openGameUrl);
        verify(gameIdMatcher.find(), "Failed to parse game ID: %s", openGameUrl);
        int gameId = Integer.parseInt(gameIdMatcher.group(GAME_ID_KEY));

        GameTitleBar titleBar = GameTitleBar.fromParent(element);
        Optional<MembersTable> membersTable = Optional.empty();
        if (!Constants.PRE_GAME.equals(titleBar.getPhase())) {
            membersTable = Optional.of(MembersTable.fromParent(element));
        }
        return new AutoValue_GamePanel(gameId, titleBar, membersTable);
    }
}
