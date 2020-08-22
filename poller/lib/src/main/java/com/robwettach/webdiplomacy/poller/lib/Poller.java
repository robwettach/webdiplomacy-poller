package com.robwettach.webdiplomacy.poller.lib;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.CountryStatus;
import com.robwettach.webdiplomacy.model.GameDate;
import com.robwettach.webdiplomacy.model.GamePhase;
import com.robwettach.webdiplomacy.model.GameState;
import com.robwettach.webdiplomacy.model.UserInfo;
import com.robwettach.webdiplomacy.model.Vote;
import com.robwettach.webdiplomacy.notify.GameNotifications;
import com.robwettach.webdiplomacy.notify.Notifier;
import com.robwettach.webdiplomacy.page.CountryUserLink;
import com.robwettach.webdiplomacy.page.GameBoardPage;
import com.robwettach.webdiplomacy.page.GameTitleBar;
import com.robwettach.webdiplomacy.page.MemberRow;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Main entrypoint for webDiplomacy Poller applications.
 */
public class Poller {
    private final int gameId;
    private final HistoryStore history;
    private final GameNotifications notifications;

    /**
     * Create a {@link Poller} for a given game.
     *
     * @param gameId The ID of the game to poll
     * @param history The {@link HistoryStore} to store and retrieve history to/from
     * @param notifier The {@link Notifier} to send notifications to
     */
    public Poller(int gameId, HistoryStore history, Notifier notifier) {
        this.gameId = gameId;
        this.history = history;
        this.notifications = new GameNotifications(gameId, notifier);
        prepare();
    }

    private void prepare() {
        List<Snapshot> snapshots = history.getSnapshotsForGame(gameId);
        snapshots.stream().map(Snapshot::getState).forEach(notifications::updateSilently);
    }

    /**
     * Poll the current status of a <em>webDiplomacy</em> game, send notifications, and update the history.
     */
    public void poll() {
        GameBoardPage page;
        try {
            page = GameBoardPage.loadGame(gameId);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load webDiplomacy game board page for game: " + gameId, e);
        }

        GameState state = stateFromPage(gameId, page);
        System.out.print(".");

        ZonedDateTime snapshotDate = ZonedDateTime.now(ZoneOffset.UTC);
        Snapshot current = Snapshot.create(snapshotDate, state);

        notifications.updateAndNotify(state);

        // Diffs imply a change, and a change implies diffs, but it's not necessarily 1-to-1
        // We track more pieces of state than we notify about (SC/unit count, etc), and we want to notify
        // even if there's no state change, specifically for the "one hour remaining" case.
        Optional<Snapshot> previous = history.getLatestSnapshotForGame(gameId);
        if (previous.isEmpty() || !previous.get().getState().equals(current.getState())) {
            history.addSnapshot(gameId, current);
        }
    }

    private static GameState stateFromPage(int gameId, GameBoardPage page) {
        GameState.Builder builder = GameState.builder();

        builder.id(gameId);
        translateTitleBar(page.getTitleBar(), builder);
        builder.countries(page.getMembersTable()
                                  .stream()
                                  .flatMap(t -> t.getRows().stream())
                                  .map(Poller::countryFromRow)
                                  .collect(toList()));

        return builder.build();
    }

    private static void translateTitleBar(GameTitleBar titleBar, GameState.Builder builder) {
        builder.name(titleBar.getName());
        builder.date(GameDate.parse(titleBar.getDate()));
        builder.phase(GamePhase.fromString(titleBar.getPhase()));
        builder.paused(titleBar.isPaused());
        titleBar.getNextTurnAt().ifPresent(builder::nextTurnAt);
    }

    private static CountryState countryFromRow(MemberRow memberRow) {
        CountryState.Builder builder = CountryState.builder();

        builder.countryName(memberRow.getCountryName());
        builder.user(userInfoFromLink(memberRow.getUser()));
        builder.status(CountryStatus.fromString(memberRow.getStatus()));
        builder.supplyCenterCount(memberRow.getSupplyCenterCount());
        builder.unitCount(memberRow.getUnitCount());
        builder.votes(memberRow.getVotes().stream().map(Vote::valueOf).collect(toSet()));

        return builder.build();
    }

    private static UserInfo userInfoFromLink(CountryUserLink link) {
        return UserInfo.create(link.getName(), link.getId());
    }
}
