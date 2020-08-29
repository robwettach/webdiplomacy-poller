package com.robwettach.webdiplomacy.poller.lambda;

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.robwettach.webdiplomacy.notify.Notifier;
import com.robwettach.webdiplomacy.notify.StdOutNotifier;
import com.robwettach.webdiplomacy.poller.lib.HistoryStore;
import com.robwettach.webdiplomacy.poller.lib.Poller;

/**
 * Lambda entrypoint for periodic poller.
 */
public class LambdaPoller {
    private static final HistoryStore HISTORY_STORE = new DynamoHistoryStore(System.getenv("GAME_HISTORY_TABLE_NAME"));
    private static final Notifier NOTIFIER = StdOutNotifier.create();

    /**
     * Lambda entrypoint for periodic poller.
     * @param event The CloudWatch {@link ScheduledEvent} that triggered this execution
     */
    public void handle(ScheduledEvent event) {
        // TODO: get game IDs either from input or Dynamo
        int gameId = 313359;

        Poller poller = new Poller(gameId, HISTORY_STORE, NOTIFIER);
        poller.poll();
    }
}
