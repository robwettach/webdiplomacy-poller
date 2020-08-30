package com.robwettach.webdiplomacy.poller.lambda.cdk;

import software.amazon.awscdk.core.App;
import software.amazon.awscdk.services.lambda.Code;

/**
 * CDK entrypoint.
 */
public class Main {
    /**
     * CDK entrypoint.
     *
     * @param args Command-line arguments
     */
    public static void main(final String[] args) {
        App app = new App();

        Code lambdaPollerCode = Code.fromAsset(System.getenv("POLLER_LAMBDA_ZIP"));
        new WebDiplomacyPollerLambdaStack(
                app,
                "WebDiplomacyPoller",
                new WebDiplomacyPollerLambdaStack.Props(lambdaPollerCode));

        app.synth();
    }
}
