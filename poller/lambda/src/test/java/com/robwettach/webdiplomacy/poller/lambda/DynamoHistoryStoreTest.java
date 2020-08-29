package com.robwettach.webdiplomacy.poller.lambda;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Iterables;
import com.robwettach.webdiplomacy.json.Json;
import com.robwettach.webdiplomacy.model.GameDate;
import com.robwettach.webdiplomacy.model.GamePhase;
import com.robwettach.webdiplomacy.model.GameState;
import com.robwettach.webdiplomacy.model.Season;
import com.robwettach.webdiplomacy.notify.Snapshot;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.RegisterExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.BeanTableSchema;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

class DynamoHistoryStoreTest {
    public static final String TABLE_NAME = "GameHistoryRecords";
    private static LocalDynamoDb localDynamoDb;
    private static DynamoDbTable<GameHistoryRecord> recordsTable;

    @RegisterExtension
    static final IncrementingIntResolver INT_PROVIDER = new IncrementingIntResolver();

    private DynamoHistoryStore historyStore;

    @BeforeAll
    static void setupDynamo() {
        localDynamoDb = new LocalDynamoDb();
        localDynamoDb.start();
        DynamoDbEnhancedClient dynamo = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(localDynamoDb.createClient())
                .build();
        recordsTable = dynamo.table(
                TABLE_NAME,
                BeanTableSchema.create(GameHistoryRecord.class));
        recordsTable.createTable();
    }

    @AfterAll
    static void tearDownDynamo() {
        localDynamoDb.stop();
    }

    @BeforeEach
    void setUp() {
        historyStore = new DynamoHistoryStore(recordsTable);
    }

    @Test
    void shouldStoreHistoryRecordInProperSchema(int gameId) throws JsonProcessingException {
        ZonedDateTime time = ZonedDateTime.now();
        GameState state = GameState.builder()
                .id(gameId)
                .name("test")
                .date(GameDate.create(Season.Spring, 1901))
                .phase(GamePhase.Diplomacy)
                .build();
        Snapshot expectedSnap = Snapshot.create(time, state);
        historyStore.addSnapshot(gameId, expectedSnap);

        GetItemResponse response = localDynamoDb.createClient()
                .getItem(r -> r.tableName(TABLE_NAME)
                         .key(Map.of(
                                "gameId", AttributeValue.builder().n(String.valueOf(gameId)).build(),
                                "time", AttributeValue.builder().s(time.toString()).build())));
        assertThat("Missing response item", response.hasItem());
        Map<String, AttributeValue> item = response.item();
        assertThat("Unexpected gameId", item.get("gameId").n(), is(String.valueOf(gameId)));
        assertThat("Unexpected time", item.get("time").s(), is(time.toString()));
        assertThat("Unexpected state", Json.OBJECT_MAPPER.readValue(item.get("state").s(), GameState.class), is(state));
    }

    @Test
    void shouldAddSnapshot(int gameId) {
        Snapshot expectedSnap = Snapshot.create(ZonedDateTime.now(), GameState.builder()
                .id(gameId)
                .name("test")
                .date(GameDate.create(Season.Spring, 1901))
                .phase(GamePhase.Diplomacy)
                .build());
        historyStore.addSnapshot(gameId, expectedSnap);

        List<Snapshot> allSnaps = historyStore.getSnapshotsForGame(gameId);
        assertThat("Unexpected snapshots", allSnaps, contains(expectedSnap));

        Optional<Snapshot> snap = historyStore.getLatestSnapshotForGame(gameId);
        assertThat("Missing latest", snap.isPresent());
        assertThat("Unexpected snapshot", snap, is(optionalWithValue(equalTo(expectedSnap))));
    }

    @Test
    void shouldGetNoSnapshots(int gameId) {
        List<Snapshot> allSnaps = historyStore.getSnapshotsForGame(gameId);
        assertThat("Unexpected snapshots", allSnaps, is(empty()));
    }

    @Test
    void shouldGetNoLatestSnapshot(int gameId) {
        Optional<Snapshot> latest = historyStore.getLatestSnapshotForGame(gameId);
        assertThat("Unexpected latest snapshot", latest, is(emptyOptional()));
    }

    @Test
    void shouldGetManySnapshots(int gameId) {
        ZonedDateTime startTime = ZonedDateTime.now();
        int startYear = 1901;
        List<Snapshot> expectedSnapshots = IntStream.range(0, 10)
                .mapToObj(i -> Snapshot.create(startTime.plusHours(i), GameState.builder()
                            .id(gameId)
                            .name("test")
                            .date(GameDate.create(Season.Spring, startYear + i))
                            .phase(GamePhase.Diplomacy)
                            .build()))
                .collect(toList());
        expectedSnapshots.forEach(s -> historyStore.addSnapshot(gameId, s));

        List<Snapshot> actualSnapshots = historyStore.getSnapshotsForGame(gameId);
        assertThat("Unexpected snapshots", actualSnapshots, is(expectedSnapshots));
    }

    @Test
    void shouldGetLatestOfManySnapshots(int gameId) {
        ZonedDateTime startTime = ZonedDateTime.now();
        int startYear = 1901;
        List<Snapshot> expectedSnapshots = IntStream.range(0, 10)
                .mapToObj(i -> Snapshot.create(startTime.plusHours(i), GameState.builder()
                        .id(gameId)
                        .name("test")
                        .date(GameDate.create(Season.Spring, startYear + i))
                        .phase(GamePhase.Diplomacy)
                        .build()))
                .collect(toList());
        expectedSnapshots.forEach(s -> historyStore.addSnapshot(gameId, s));

        Optional<Snapshot> actualSnapshot = historyStore.getLatestSnapshotForGame(gameId);
        assertThat(
                "Unexpected latest snapshot",
                actualSnapshot,
                is(optionalWithValue(equalTo(Iterables.getLast(expectedSnapshots)))));
    }

    /**
     * Super-simple {@link ParameterResolver} that returns an incrementing integer value.
     *
     * <p>Useful because we don't necessarily want to tear down the {@link LocalDynamoDb} on every test, and also want
     * to guarantee the tests won't interfere with each other.
     */
    private static class IncrementingIntResolver implements ParameterResolver {
        private int value = 0;

        @Override
        public boolean supportsParameter(
                ParameterContext parameterContext, ExtensionContext extensionContext)
                throws ParameterResolutionException {
            Class<?> type = parameterContext.getParameter().getType();
            return type.equals(Integer.TYPE) || type.equals(Integer.class);
        }

        @Override
        public Integer resolveParameter(
                ParameterContext parameterContext, ExtensionContext extensionContext)
                throws ParameterResolutionException {
            return value++;
        }
    }
}
