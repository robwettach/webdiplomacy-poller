## WebDiplomacy Poller Lambda CDK
This is the [CDK](https://aws.amazon.com/cdk/) configuration for *webDiplomacy Poller*.  It deploys the Lambda+DynamoDB
version of the app to AWS via CDK+CloudFormation.

### Prerequisites
Using this package requires that you have the `cdk` command installed, which in turn depends on NodeJS.  You can follow
CDK's own instructions [here](https://docs.aws.amazon.com/cdk/latest/guide/cli.html) to install the command globally.

Simply:
```
$ brew install node
$ npm install -g aws-cdk
```

You also must be authenticated with an AWS account and have a default region set in your `~/.aws/config` file.

### Useful commands
CDK integrates with our Gradle builds to build all relevant dependencies before deploying the application.  *You must
run these commands from this `lambda/poller/cdk` directory!*

* `cdk ls`          list all stacks in the app
* `cdk synth`       emits the synthesized CloudFormation template
* `cdk deploy`      deploy this stack to your default AWS account/region
* `cdk diff`        compare deployed stack with current state
* `cdk docs`        open CDK documentation

Simply run `cdk deploy` from this directory to deploy the Stack via CloudFormation.  You can run `cdk destroy` when
you're done testing if you'd like to stop running the application and potentially incurring AWS charges (though I'm
*assuming* this app will easily fit within the free tier).
