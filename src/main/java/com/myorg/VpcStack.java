package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class VpcStack extends Stack {
    private Vpc vpc;
    public VpcStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public VpcStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        vpc = Vpc.Builder.create(this, "DemoVpc")
                .maxAzs(2)  // Default is all AZs in region
                .natGateways(0) //O natGateways afeta na cobrança independente da utilização, nesse caso desabilitamos o mesmo para evitar cobranças: https://aws.amazon.com/vpc/pricing/
                .build();
    }
    public Vpc getVpc() {
        return vpc;
    }
}
