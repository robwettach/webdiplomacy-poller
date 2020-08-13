package com.robwettach.webdiplomacy.poller;

import com.robwettach.webdiplomacy.model.GameState;
import com.robwettach.webdiplomacy.notify.CompositeNotifier;
import com.robwettach.webdiplomacy.notify.Diff;
import com.robwettach.webdiplomacy.notify.GameNotifications;
import com.robwettach.webdiplomacy.notify.Notifier;
import com.robwettach.webdiplomacy.notify.SlackNotifier;
import com.robwettach.webdiplomacy.notify.StdOutNotifier;
import com.robwettach.webdiplomacy.page.GameListingsPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
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

    public static void main(String... args) throws ExecutionException, InterruptedException {
        ensureConfigDirectory();
        Map<String, String> cookies = new LocalCookieProvider(getConfigDir()).getCookies();
        HistoryStore history = new LocalHistoryStore(getConfigDir());
        history.load();

        Map<Integer, GameNotifications> notifications = prepopulateNotificationState(history);


        ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();

        ScheduledFuture<?> result = schedule.scheduleAtFixedRate(
                () -> checkForUpdates(cookies, history, notifications),
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

    private static Map<Integer, GameNotifications> prepopulateNotificationState(HistoryStore history) {
        Map<Integer, GameNotifications> notifications = new HashMap<>();
        history.getGameIds().forEach(gameId -> {
            List<Snapshot> snapshots = history.getSnapshotsForGame(gameId);

            List<Notifier> notifiers = new ArrayList<>();
            notifiers.add(StdOutNotifier.create());
            String webhookUrl = System.getenv(ENV_SLACK_WEBHOOK_URL);
            if (webhookUrl != null) {
                notifiers.add(SlackNotifier.create(webhookUrl));
            }

            GameNotifications gameNotifications = new GameNotifications(
                    gameId,
                    CompositeNotifier.create(notifiers));
            notifications.put(gameId, gameNotifications);
            snapshots.stream().map(Snapshot::getState).forEach(gameNotifications::updateSilently);
        });
        return notifications;
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
            Map<String, String> cookies,
            HistoryStore history,
            Map<Integer, GameNotifications> notifications) {
        ZonedDateTime snapshotDate = ZonedDateTime.now(ZoneOffset.UTC);
        Map<Integer, GameState> games = getGames(cookies);
        System.out.print(".");

        boolean hasUpdates = false;
        for (Map.Entry<Integer, GameState> entry : games.entrySet()) {
            Integer id = entry.getKey();
            GameState state = entry.getValue();
            Optional<Snapshot> previous = history.getLatestSnapshotForGame(id);
            Snapshot current = Snapshot.create(snapshotDate, state);

            notifications.get(id).updateAndNotify(state);

            // Diffs imply a change, and a change implies diffs, but it's not necessarily 1-to-1
            // We track more pieces of state than we notify about (SC/unit count, messages), and we want to notify
            // even if there's no state change, specifically for the "one hour remaining" case.
            if (previous.isEmpty() || !previous.get().getState().equals(current.getState())) {
                history.addSnapshot(id, current);
                hasUpdates = true;
            }
        }
        if (hasUpdates) {
            history.save();
        }
    }

    private static Map<Integer, GameState> getGames(Map<String, String> cookies) {
        Document loggedInDoc = null;
        try {
            loggedInDoc = Jsoup.connect("http://webdiplomacy.net/gamelistings.php?gamelistType=My%20games")
                    .cookies(cookies)
                    .get();
        } catch (IOException e) {
            System.err.println("Failed to get game listing");
            e.printStackTrace();
        }

        GameListingsPage page = new GameListingsPage(loggedInDoc);

        return page.getGames();
    }
}