package com.robwettach.webdiplomacy.page;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

@AutoValue
public abstract class GameListingsPage {
    public abstract ImmutableMap<Integer, GamePanel> getGamePanels();

    public static GameListingsPage load(Map<String, String> cookies) throws IOException {
        return fromDocument(Jsoup.connect("http://webdiplomacy.net/gamelistings.php?gamelistType=My%20games")
                .cookies(cookies)
                .get());
    }

    public static GameListingsPage fromDocument(Document document) {
        Elements games = document.select(".gamesList .gamePanel");
        return new AutoValue_GameListingsPage(games.stream()
                .map(GamePanel::fromElement)
                .collect(toImmutableMap(GamePanel::getId, Function.identity())));
    }
}
