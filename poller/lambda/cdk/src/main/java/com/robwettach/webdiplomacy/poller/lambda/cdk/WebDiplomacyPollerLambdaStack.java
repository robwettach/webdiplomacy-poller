package com.robwettach.webdiplomacy.poller.lambda.cdk;

import java.util.List;
import java.util.Map;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.Schedule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;

/**
 * Main CloudFormation Stack for webDiplomacy Poller Lambda.
 */
public class WebDiplomacyPollerLambdaStack extends Stack {
    /**
     * Stack Props for {@link WebDiplomacyPollerLambdaStack}.
     */
    public static class Props implements StackProps {
        private final Code pollerLambdaCode;

        public Props(Code pollerLambdaCode) {
            this.pollerLambdaCode = pollerLambdaCode;
        }
    }

    /**
     * Main CDK Stack.
     *
     * @param scope The parent App scope
     * @param id This stack's ID
     * @param props Stack properties
     */
    public WebDiplomacyPollerLambdaStack(final Construct scope, final String id, final Props props) {
        super(scope, id, props);

        Table history = Table.Builder.create(this, "GameHistoryRecords")
                .partitionKey(Attribute.builder().name("gameId").type(AttributeType.NUMBER).build())
                .sortKey(Attribute.builder().name("time").type(AttributeType.STRING).build())
                .readCapacity(5)
                .writeCapacity(1)
                .build();

        Function poller = Function.Builder.create(this, "Poller")
                .runtime(Runtime.JAVA_11)
                .handler("com.robwettach.webdiplomacy.poller.lambda.LambdaPoller::handle")
                .code(props.pollerLambdaCode)
                .memorySize(512)
                .timeout(Duration.minutes(1))
                .environment(Map.of("GAME_HISTORY_TABLE_NAME", history.getTableName()))
                .build();
        history.grantReadWriteData(poller);

        Rule.Builder.create(this, "Timer")
                .schedule(Schedule.rate(Duration.minutes(2)))
                .targets(List.of(new LambdaFunction(poller)))
                .build();
    }
}
