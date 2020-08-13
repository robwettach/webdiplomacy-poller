package com.robwettach.webdiplomacy.page;

import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.CountryStatus;
import com.robwettach.webdiplomacy.model.GameDate;
import com.robwettach.webdiplomacy.model.GamePhase;
import com.robwettach.webdiplomacy.model.GameState;
import com.robwettach.webdiplomacy.model.UserInfo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Verify.verify;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class GameListingsPage extends WebDiplomacyPage {
    public static final String GAME_ID_KEY = "id";
    private static final String SUPPLY_CENTERS_KEY = "supplyCenters";
    private static final String UNITS_KEY = "units";

    private static final Pattern SUPPLY_CENTERS_UNITS_PATTERN = Pattern.compile(
            format("(?<%s>\\d+) supply-centers, (?<%s>\\d+) units", SUPPLY_CENTERS_KEY, UNITS_KEY));
    private static final Pattern GAME_ID_PATTERN = Pattern.compile(
            format("board\\.php\\?gameID=(?<%s>\\d+)", GAME_ID_KEY));

    public GameListingsPage(Document doc) {
        super(doc);
    }

    public Map<Integer, GameState> getGames() {
        Elements games = getDocument().select(".gamesList .gamePanel");
        return games.stream().map(this::parseGameState)
                .collect(toMap(GameState::getId, Function.identity()));
    }

    private GameState parseGameState(Element g) {
        GameState.Builder gameBuilder = GameState.builder();
        gameBuilder.name(g.select(".gameName").first().text());
        String openGameUrl = g.select(".enterBarOpen > a").first().attr("href");
        Matcher gameIdMatcher = GAME_ID_PATTERN.matcher(openGameUrl);
        verify(gameIdMatcher.find(), "Failed to parse game ID: %s", openGameUrl);
        gameBuilder.id(Integer.parseInt(gameIdMatcher.group(GAME_ID_KEY)));

        gameBuilder.date(GameDate.parse(g.select(".gameDate").first().text()));
        GamePhase phase = GamePhase.fromString(g.select(".gamePhase").first().text());
        gameBuilder.phase(phase);
        Element gameTimeRemaining = g.select(".gameTimeRemaining").first();
        if (gameTimeRemaining.text().startsWith("Paused")) {
            gameBuilder.paused(true);
        } else {
            String next = g.select("span.timeremaining").first().attr("unixtime");
            gameBuilder.nextTurnAt(
                    ZonedDateTime.ofInstant(
                            Instant.ofEpochSecond(Long.parseLong(next)),
                            ZoneOffset.UTC));
        }

        if (!GamePhase.PreGame.equals(phase)) {
            Elements members = g.select(".membersList .member");
            gameBuilder.countries(members.stream().map(this::parseCountry).collect(toList()));
        }
        return gameBuilder.build();
    }

    private CountryState parseCountry(Element countryElement) {
        CountryState.Builder countryBuilder = CountryState.builder();
        countryBuilder.countryName(countryElement.select(".memberCountryName > span[class~=country\\d+]").first().text());
        Element statusImg = countryElement.select("span[class*=\"StatusIcon\"] > img").first();
        countryBuilder.status(statusImg != null ? CountryStatus.fromString(statusImg.attr("alt")) : CountryStatus.NoOrders);

        countryBuilder.messageUnread(!countryElement.select("img[src$=mail.png]").isEmpty());
        UserInfo countryPlayer = parseCountryUser(countryElement);

        countryBuilder.currentUser(getCurrentlyLoggedInUser()
                .filter(u -> u.equals(countryPlayer))
                .isPresent());
        countryBuilder.user(countryPlayer);

        Elements memberStatus = countryElement.select(".memberStatus > em");
        if (!memberStatus.isEmpty() && "Defeated".equals(memberStatus.first().text())) {
            countryBuilder.status(CountryStatus.Defeated);
        } else {
            String scUnits = countryElement.select(".memberSCCount").first().text();
            Matcher scUnitMatcher = SUPPLY_CENTERS_UNITS_PATTERN.matcher(scUnits);
            verify(scUnitMatcher.find(), "Failed to parse supply centers and units: %s", scUnits);
            countryBuilder.supplyCenterCount(Integer.parseInt(scUnitMatcher.group(SUPPLY_CENTERS_KEY)));
            countryBuilder.unitCount(Integer.parseInt(scUnitMatcher.group(UNITS_KEY)));
        }
        return countryBuilder.build();
    }

    private UserInfo parseCountryUser(Element countryElement) {
        Element userA = countryElement.select(".memberName > a").first();
        return UserInfo.create(userA.text(), getUserIdFromATag(userA));
    }
}
