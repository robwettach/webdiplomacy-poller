## webDiplomacy Poller Diff Checkers
This library contains implementations of the
[`DiffChecker`](./src/main/java/com/robwettach/webdiplomacy/diff/DiffChecker.java) interface for determining if any
changes occurred in a *webDiplomacy* game.

### Design
All `DiffChecker` implementations must be *stateless* checks between the "previous" and "current"
[`Snapshot`](./src/main/java/com/robwettach/webdiplomacy/diff/Snapshot.java).  This makes it *significantly* easier to
run in a distributed environment (e.g. Lambda) without having to keep a single process running to maintain state or
replay history to build up the appropriate state to determine the next "diff".

### Implementation
When creating a new `DiffChecker` instance, make sure to add it to
[`DiffCheckers.CHECKERS`](./src/main/java/com/robwettach/webdiplomacy/diff/DiffChecker.java#L22).  That way it will get
automatically picked up by all users of `DiffCheckers`.

Be sure to add tests, too!
