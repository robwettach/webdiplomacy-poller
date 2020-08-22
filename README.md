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
* `assembleDist` - Zips and Tars up the build artifacts and dependencies in `poller/build/distributions`
* `installDist` - Installs the distribution to `poller/build/install/webdiplomacy-poller` -
  you can run the executable directly from here

### Design
There are (currently) six different Gradle projects:
* `json` - Provides a specially-configured [Jackson](https://github.com/FasterXML/jackson) `ObjectMapper` to be used
  throughout the project for consistency.
* `model` - [Google AutoValue](https://github.com/google/auto/blob/master/value/userguide/index.md)-based model classes
  to represent the current state of a *webDiplomacy* game
* `page` - [JSoup](https://jsoup.org/)-based logic to parse a pages from https://webDiplomacy.net
* `notify` - Logic to detect changes between subsequent game states and send notifications.  Currently supports multiple
  state transitions (defeated, 1-hour remaining, messages, orders, paused, phase change, votes) and sending
  notifications to `stdout` and Slack
* `poller-lib` - Library that ties together the logic for polling a single *webDiplomacy* game, exposing interfaces to
   allow clients to control how game history is stored and how notifications are sent
* `poller` - Top-level CLI application.  Configures storage of state to local disk and orchestrates polling
  https://webDiplomacy.net every 2 minutes via the `poller-lib` library

## Running
If you have the Zip/Tar distribution from `./gradlew assembleDist` or have run `./gradlew installDist`, you have the
`bin/webdiplomacy-poller` CLI - this is the main entry point of the application.  Before running, there are two
environment variables that you may set:
* `SLACK_WEBHOOK_URL` - this is the URL for your Slack webhook.  The URL is it's own authentication, so I don't want to
  commit it to Git.  If you don't provide this variable, you'll only get notifications on `stdout`
* `WEBDIP_POLLER_HOME` - by default, `webdiplomacy-poller` stores state information in `~/.config/webdip-poller`.  Set
  this variable to store the config in a custom directory.

webDiplomacy Poller polls a single publicly-accessible game by ID passed as a command line parameter.  You can specify
this parameter both via `./gradlew run --args 1234` and `webdiplomacy-poller 1234`.

## Slack Integration
See the [`slack`](slack) directory for files directly related to the Slack Workflow integration.  You can load the
[`webdiplomacy_poller.slackworkflow`](slack/webdiplomacy_poller.slackworkflow) into Slack to create the basic workflow,
and use the [`webDiplomacy-D.png`](slack/webDiplomacy-D.png) file as it's icon.  Creating and Publishing that workflow
will generate the webhook URL that you can use for `SLACK_WEBHOOK_URL`.

## Contributing
This repo uses [`commitlint`](https://commitlint.js.org/) to enforce commit message guidelines, and
[Husky](https://github.com/typicode/husky) to make that easy.  It does require you to have NodeJS installed and to run
`npm install` when first checking out the package.  From then on out, Husky will enforce *all* commit messages match
the default `commitlint` format.  We also have `commitlint`'s CLI set up via `npm run commit` that will help writing
valid commit messages.
