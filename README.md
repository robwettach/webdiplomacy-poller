# webDiplomacy Poller
A small application to poll https://webDiplomacy.net and send notifications to Slack when the game progresses.

## Status
This is an early work-in-progress.  I've got it set up locally to notify my office Slack channel of game updates, but
I'm still finding plenty of bugs and potential enhancements.  Feel free to open
[GitHub Issues](https://github.com/robwettach/webdiplomacy-poller/issues) for any bugs or feature requests, and start
tackling them yourself!

In the long run, I'd like to migrate this to an AWS Lambda-based system so I don't have to keep my personal laptop
open all day!

## Developing
This app is built with [Gradle](https://gradle.org/).  I've never used Gradle before, so I have no idea if I'm doing it
well or not.  Feel free to contribute updates to the build system.

You should be able to build and run locally as much as you'd like within IntelliJ, but you can also use `./gradlew`
to build and run in your terminal.  Some helpful Gradle tasks:
* `build` - will build the code (and run non-existent unit tests?)
* `run` - will run the app within the context of Gradle.
  This doesn't support receiving username/password from the command line
* `assembleDist` - Zips and Tars up the build artifacts and dependencies in `build/distributions`
* `installDist` - Installs the distribution to `build/install/webdiplomacy-poller` -
  you can run the executable directly from here

### Design
There are (currently) four different Java sub-packages.  I'd be interested in splitting these into separate Gradle
modules, as ideally they're logically separate.
* `model` - [Google AutoValue](https://github.com/google/auto/blob/master/value/userguide/index.md)-based model classes
  and [Jackson](https://github.com/FasterXML/jackson) JSON serialization/deserialization to represent the current state
  of a *webDiplomacy* game
* `page` - [JSoup](https://jsoup.org/)-based logic to parse a
  page from https://webDiplomacy.net into the `model` classes
* `notify` - Logic to detect changes between subsequent game states and send notifications.  Currently supports multiple
  state transitions (defeated, 1-hour remaining, messages, orders, paused, phase change, votes) and sending
  notifications to `stdout` and Slack
* `poller` - Top-level CLI application.  Configures storage of state to local disk and orchestrates polling
  https://webDiplomacy.net every 2 minutes, checking for notifications, and saving updated state

## Running
If you have the Zip/Tar distribution from `./gradlew assembleDist` or have run `./gradlew installDist`, you have the
`bin/webdiplomacy-poller` CLI - this is the main entry point of the application.  Before running, there are two
environment variables that you may set:
* `SLACK_WEBHOOK_URL` - this is the URL for your Slack webhook.  The URL is it's own authentication, so I don't want to
  commit it to Git.  If you don't provide this variable, you'll only get notifications on `stdout`
* `WEBDIP_POLLER_HOME` - by default, `webdiplomacy-poller` stores state information in `~/.config/webdip-poller`.  Set
  this variable to store the config in a custom directory.

The first time you run `webdiplomacy-poller` it will ask you for your username and password for
https://webDiplomacy.net.  *webDiplomacy* doesn't have too-great security on their login, so the app doesn't store this
password anywhere - it simply exchanges it for cookies that get written to `$WEBDIP_POLLER_HOME/cookies.json`.  As long
as `cookies.json` exists, whenever you run `webdiplomacy-poller` it will read the cookies from that file.

## Slack Integration
See the [`slack`](slack) directory for files directly related to the Slack Workflow integration.  You can load the [`webdiplomacy_poller.slackworkflow`](slack/webdiplomacy_poller.slackworkflow) into Slack to create the basic workflow, and use the [`webDiplomacy-D.png`](slack/webDiplomacy-D.png) file as it's icon.  Creating and Publishing that workflow will generate the webhook URL that you can use for `SLACK_WEBHOOK_URL`.
