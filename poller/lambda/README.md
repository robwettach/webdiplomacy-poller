## webDiplomacy Poller Lambda
This project contains a Lambda+DynamoDB implementation of *webDiplomacy Poller*.  It builds on top of the
[`poller-lib`](../lib) project and implements a DynamoDB-backed
[`HistoryStore`](../lib/src/main/java/com/robwettach/webdiplomacy/poller/lib/HistoryStore.java).  This can be deployed
via CDK from the [`poller-lambda-cdk` project](./cdk).

### Configuration
There is none (yet)!  I haven't figured out how I want to tell the system which games to poll.  Currently I just
hard-coded a `gameId` within the Lambda handler.  I'm thinking I want some kind of dynamic registration of games in the
DynamoDB table, but also to turn off the CloudWatch Event when there are no active games.

### Local Development
If you've created the **DynamoDB Table** and are authenticated with AWS Credentials, you can use the
[AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-using-invoke.html)
to invoke the function locally.  Just modify the `GAME_HISTORY_TABLE_NAME` environment variable in `template.yml` and
then run `sam local invoke` within the `poller/lambda` directory.
```
$ sam local invoke
...
START RequestId: b4f98dda-4a2e-1afa-0c3f-9bd271b7eebc Version: $LATEST
2020-08-29 16:27:23  DEBUG com.robwettach.webdiplomacy.poller.lib.Poller - Polling for changes to game 313359
2020-08-29 16:27:23  DEBUG com.robwettach.webdiplomacy.page.GameBoardPage - Loading game from http://webdiplomacy.net/board.php?gameID=313359
2020-08-29 16:27:26  INFO  com.robwettach.webdiplomacy.poller.lib.Poller - Found 0 diffs at 2020-08-29T16:27:24.416149Z for game 313359
END RequestId: b4f98dda-4a2e-1afa-0c3f-9bd271b7eebc
REPORT RequestId: b4f98dda-4a2e-1afa-0c3f-9bd271b7eebc	Init Duration: 3543.33 ms	Duration: 4643.88 ms	Billed Duration: 4700 ms	Memory Size: 512 MB	Max Memory Used: 123 MB
```
