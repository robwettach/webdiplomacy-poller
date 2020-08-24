package com.robwettach.webdiplomacy.poller;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;
import static com.robwettach.webdiplomacy.json.Json.OBJECT_MAPPER;
import static java.lang.String.format;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.robwettach.webdiplomacy.poller.lib.HistoryStore;
import com.robwettach.webdiplomacy.poller.lib.Snapshot;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link HistoryStore} that manages history on local disk.
 *
 * <p>Writes per-game history to {@code WEBDIP_POLLER_HOME/$gameId-snapshots.json}.
 */
public class LocalHistoryStore implements HistoryStore {
    private static final Logger LOG = LogManager.getLogger(LocalHistoryStore.class);

    private static final String GAME_SNAPSHOTS_FORMAT = "%d-snapshots.json";
    private static final String GAME_ID_KEY = "gameId";
    private static final Pattern GAME_SNAPSHOTS_PATTERN = Pattern.compile(format(
            "(?<%s>\\d+)-snapshots.json",
            GAME_ID_KEY));
    private static final String LEGACY_SNAPSHOTS_FILE_NAME = "snapshots.json";

    private final Path configDirPath;
    private final Path snapshotsPath;

    private Map<Integer, List<Snapshot>> snapshots = new HashMap<>();

    /**
     * Create a {@link LocalHistoryStore} instance rooted at {@code configDirPath}.
     *
     * <p>Synchronously loads all {@code $gameId-snapshots.json} files found at {@code configDirPath}.
     *
     * @param configDirPath The root {@code WEBDIP_POLLER_HOME} directory
     */
    public LocalHistoryStore(Path configDirPath) {
        checkNotNull(configDirPath, "configDirPath must not be null");
        this.configDirPath = configDirPath;
        this.snapshotsPath = configDirPath.resolve(LEGACY_SNAPSHOTS_FILE_NAME);
        load();
    }

    private void load() {
        loadLegacySnapshots();
        try {
            Files.list(configDirPath)
                    .filter(p -> GAME_SNAPSHOTS_PATTERN.matcher(p.getFileName().toString()).find())
                    .forEach((path) -> {
                        Matcher gameIdMatcher = GAME_SNAPSHOTS_PATTERN.matcher(path.getFileName().toString());
                        verify(
                                gameIdMatcher.find(),
                                "Failed to parse filename that was already filtered to match! %s",
                                path.getFileName());
                        int gameId = Integer.parseInt(gameIdMatcher.group(GAME_ID_KEY));
                        List<Snapshot> gameSnapshots = new ArrayList<>();
                        try {
                            gameSnapshots = OBJECT_MAPPER.readValue(
                                    path.toFile(),
                                    new TypeReference<>() {});
                        } catch (IOException e) {
                            LOG.error("Failed to read JSON from: {}", path, e);
                        }
                        snapshots.put(gameId, gameSnapshots);
                        LOG.info(
                                "Loaded {} snapshots for game {} from: {}",
                                gameSnapshots.size(),
                                gameId,
                                path);
                    });
        } catch (IOException e) {
            LOG.error("Failed to list files in: {}", configDirPath, e);
        }
    }

    private void loadLegacySnapshots() {
        verify(snapshots.isEmpty(), "Cannot load legacy snapshots after initializing");
        if (Files.exists(snapshotsPath)) {
            LOG.info("Found legacy snapshots file: {}", snapshotsPath);
            try {
                // Make a known-mutable copy
                snapshots = new HashMap<>(OBJECT_MAPPER.readValue(
                        snapshotsPath.toFile(),
                        new TypeReference<>() {}));
            } catch (IOException e) {
                LOG.error("Failed to read JSON from: {}", snapshotsPath, e);
            }
            int snapshotCount = snapshots.values().stream().mapToInt(List::size).sum();
            LOG.info(
                    "Loaded {} snapshots from {} games from: {}",
                    snapshotCount,
                    snapshots.size(),
                    snapshotsPath);

            // Save the loaded snapshots out to the new format
            save();
            // Delete the legacy file
            try {
                Files.deleteIfExists(snapshotsPath);
                LOG.info("Deleted legacy snapshots file: {}", snapshotsPath);
            } catch (IOException e) {
                LOG.error("Failed to delete legacy snapshots file: {}", snapshotsPath, e);
            }
        }
    }

    @Override
    public ImmutableList<Snapshot> getSnapshotsForGame(int gameId) {
        return ImmutableList.copyOf(snapshots.getOrDefault(gameId, new ArrayList<>()));
    }

    @Override
    public Optional<Snapshot> getLatestSnapshotForGame(int gameId) {
        List<Snapshot> gameSnapshots = getSnapshotsForGame(gameId);
        return Optional.ofNullable(Iterables.getLast(gameSnapshots, null));
    }

    @Override
    public void addSnapshot(int gameId, Snapshot snapshot) {
        List<Snapshot> gameSnapshots = snapshots.computeIfAbsent(gameId, (x) -> new ArrayList<>());
        gameSnapshots.add(snapshot);
        save();
    }

    private void save() {
        snapshots.forEach((gameId, gameSnapshots) -> {
            Path path = configDirPath.resolve(format(GAME_SNAPSHOTS_FORMAT, gameId));
            try {
                OBJECT_MAPPER.writeValue(path.toFile(), gameSnapshots);
                LOG.info(
                        "Saved {} snapshots for game {} to: {}",
                        gameSnapshots.size(),
                        gameId,
                        path);
            } catch (IOException e) {
                LOG.error("Failed to write snapshots to: {}", path, e);
            }
        });
    }
}
