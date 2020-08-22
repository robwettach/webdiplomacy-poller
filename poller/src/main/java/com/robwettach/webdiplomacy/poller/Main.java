package com.robwettach.webdiplomacy.poller;

import static com.google.common.base.Preconditions.checkArgument;

import com.robwettach.webdiplomacy.notify.CompositeNotifier;
import com.robwettach.webdiplomacy.notify.Notifier;
import com.robwettach.webdiplomacy.notify.SlackNotifier;
import com.robwettach.webdiplomacy.notify.StdOutNotifier;
import com.robwettach.webdiplomacy.poller.lib.HistoryStore;
import com.robwettach.webdiplomacy.poller.lib.Poller;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Main entry point for the webDiplomacy Poller local CLI application.
 */
public class Main {

    public static final String ENV_SLACK_WEBHOOK_URL = "SLACK_WEBHOOK_URL";
    public static final String ENV_WEBDIP_POLLER_HOME = "WEBDIP_POLLER_HOME";

    /**
     * Main entry point for the webDiplomacy Poller local CLI application.
     *
     * @param args Command line arguments.  Expected to have one element: the game ID to poll
     */
    public static void main(String... args) throws InterruptedException {
        checkArgument(args.length == 1, "Must provide a game ID");
        int gameId = Integer.parseInt(args[0]);

        ensureConfigDirectory();
        HistoryStore history = new LocalHistoryStore(getConfigDir());

        Notifier notifier = getNotifier();
        Poller poller = new Poller(gameId, history, notifier);
        poller.prepare();

        ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();

        ScheduledFuture<?> result = schedule.scheduleAtFixedRate(
                poller::poll,
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
}
