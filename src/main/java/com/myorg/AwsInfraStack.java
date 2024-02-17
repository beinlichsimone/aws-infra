package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class AwsInfraStack extends Stack {
    public AwsInfraStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public AwsInfraStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // The code that defines your stack goes here

        // example resource
        // final Queue queue = Queue.Builder.create(this, "AwsInfraQueue")
        //         .visibilityTimeout(Duration.seconds(300))
        //         .build();
    }
}
