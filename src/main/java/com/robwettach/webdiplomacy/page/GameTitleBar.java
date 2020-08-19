package com.robwettach.webdiplomacy.page;

import com.robwettach.webdiplomacy.model.GameDate;
import com.robwettach.webdiplomacy.model.GamePhase;
import com.robwettach.webdiplomacy.model.GameState;
import org.jsoup.nodes.Element;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class GameTitleBar {
    private final Element titleBar;

    public GameTitleBar(Element parentNode) {
        this.titleBar = parentNode.select(".titleBar").first();
    }

    public GameState getGameGlobalState(int gameId) {
        GameState.Builder gameBuilder = GameState.builder()
                .id(gameId);
        gameBuilder.name(titleBar.select(".gameName").first().text());

        gameBuilder.date(GameDate.parse(titleBar.select(".gameDate").first().text()));
        GamePhase phase = GamePhase.fromString(titleBar.select(".gamePhase").first().text());
        gameBuilder.phase(phase);
        Element gameTimeRemaining = titleBar.select(".gameTimeRemaining").first();
        if (gameTimeRemaining.text().startsWith("Paused")) {
            gameBuilder.paused(true);
        } else {
            String next = titleBar.select("span.timeremaining").first().attr("unixtime");
            gameBuilder.nextTurnAt(
                    ZonedDateTime.ofInstant(
                            Instant.ofEpochSecond(Long.parseLong(next)),
                            ZoneOffset.UTC));
        }
        return gameBuilder.build();
    }
}
