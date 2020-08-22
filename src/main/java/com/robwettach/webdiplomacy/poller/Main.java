package com.robwettach.webdiplomacy.poller;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.CountryStatus;
import com.robwettach.webdiplomacy.model.GameDate;
import com.robwettach.webdiplomacy.model.GamePhase;
import com.robwettach.webdiplomacy.model.GameState;
import com.robwettach.webdiplomacy.model.UserInfo;
import com.robwettach.webdiplomacy.model.Vote;
import com.robwettach.webdiplomacy.notify.CompositeNotifier;
import com.robwettach.webdiplomacy.notify.GameNotifications;
import com.robwettach.webdiplomacy.notify.Notifier;
import com.robwettach.webdiplomacy.notify.SlackNotifier;
import com.robwettach.webdiplomacy.notify.StdOutNotifier;
import com.robwettach.webdiplomacy.page.CountryUserLink;
import com.robwettach.webdiplomacy.page.GameBoardPage;
import com.robwettach.webdiplomacy.page.GameTitleBar;
import com.robwettach.webdiplomacy.page.MemberRow;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Main {

    public static final String ENV_SLACK_WEBHOOK_URL = "SLACK_WEBHOOK_URL";
    public static final String ENV_WEBDIP_POLLER_HOME = "WEBDIP_POLLER_HOME";

    public static void main(String... args) throws InterruptedException {
        checkArgument(args.length == 1, "Must provide a game ID");
        int gameId = Integer.parseInt(args[0]);

        ensureConfigDirectory();
        HistoryStore history = new LocalHistoryStore(getConfigDir());
        history.load();

        Notifier notifier = getNotifier();
        Map<Integer, GameNotifications> notifications = prepopulateNotificationState(history, notifier);

        ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();

        ScheduledFuture<?> result = schedule.scheduleAtFixedRate(
                () -> checkForUpdates(gameId, history, notifications, notifier),
                0,
                2,
                TimeUnit.MINUTES);
        try {
            result.get();
            schedule.shutdown();
        } catch (ExecutionException e) {
            // If the scheduled task fails, log the exception and exit
            e.printStackTrace();
            schedule.shutdown();
            System.exit(1);
        }
    }

    private static Map<Integer, GameNotifications> prepopulateNotificationState(
            HistoryStore history,
            Notifier notifier) {
        Map<Integer, GameNotifications> notifications = new HashMap<>();
        history.getGameIds().forEach(gameId -> {
            List<Snapshot> snapshots = history.getSnapshotsForGame(gameId);
            GameNotifications gameNotifications = new GameNotifications(gameId, notifier);
            notifications.put(gameId, gameNotifications);
            snapshots.stream().map(Snapshot::getState).forEach(gameNotifications::updateSilently);
        });
        return notifications;
    }

    private static Notifier getNotifier() {
        List<Notifier> notifiers = new ArrayList<>();
        notifiers.add(StdOutNotifier.create());
        String webhookUrl = System.getenv(ENV_SLACK_WEBHOOK_URL);
        if (webhookUrl != null) {
            notifiers.add(SlackNotifier.create(webhookUrl));
        }

        return CompositeNotifier.create(notifiers);
    }

    private static void ensureConfigDirectory() {
        // Handles if the directory exists first, too.
        try {
            Files.createDirectories(getConfigDir());
        } catch (IOException e) {
            System.err.println("Failed to create config directory: " + getConfigDir());
            e.printStackTrace();
        }
    }

    private static Path getConfigDir() {
        String configDir = System.getenv(ENV_WEBDIP_POLLER_HOME);
        if (configDir == null) {
            return Path.of(System.getProperty("user.home"), ".config", "webdip-poller");
        } else {
            return Paths.get(configDir);
        }
    }

    private static void checkForUpdates(
            int gameId,
            HistoryStore history,
            Map<Integer, GameNotifications> notifications,
            Notifier notifier) {
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

        GameNotifications gameNotifications = notifications.computeIfAbsent(
                gameId,
                id -> new GameNotifications(id, notifier));
        gameNotifications.updateAndNotify(state);

        // Diffs imply a change, and a change implies diffs, but it's not necessarily 1-to-1
        // We track more pieces of state than we notify about (SC/unit count, etc), and we want to notify
        // even if there's no state change, specifically for the "one hour remaining" case.
        Optional<Snapshot> previous = history.getLatestSnapshotForGame(gameId);
        if (previous.isEmpty() || !previous.get().getState().equals(current.getState())) {
            history.addSnapshot(gameId, current);
            history.save();
        }
    }

    private static GameState stateFromPage(int gameId, GameBoardPage page) {
        GameState.Builder builder = GameState.builder();

        builder.id(gameId);
        translateTitleBar(page.getTitleBar(), builder);
        builder.countries(page.getMembersTable()
                .stream()
                .flatMap(t -> t.getRows().stream())
                .map(Main::countryFromRow)
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
