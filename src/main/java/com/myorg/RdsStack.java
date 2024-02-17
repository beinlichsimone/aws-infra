package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;

import java.util.Collections;

public class RdsStack extends Stack {
    public RdsStack(final Construct scope, final String id, final Vpc vpc) {
        this(scope, id, null, vpc);
    }

    public RdsStack(final Construct scope, final String id, final StackProps props, final Vpc vpc) {
        super(scope, id, props);

        //Parâmetro que será preenchido no momento do deploy
        CfnParameter senha = CfnParameter.Builder.create (this, "senha")
            .type("String")
                    .description("Senha do database demo")
                    .build();

        ISecurityGroup iSecurityGroup = SecurityGroup.fromSecurityGroupId(this, id, vpc.getVpcDefaultSecurityGroup());
        iSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(5432));

        DatabaseInstance database = DatabaseInstance.Builder
                .create(this, "Rds-demo")
                .instanceIdentifier("demo-db")
                .engine(DatabaseInstanceEngine.postgres(PostgresInstanceEngineProps.builder()
                        .version(PostgresEngineVersion.VER_12)
                        .build()))
                .vpc(vpc)
                .credentials(Credentials.fromUsername("postgres",
                CredentialsFromUsernameOptions.builder()
                        .password(SecretValue.unsafePlainText(senha.getValueAsString()))
                        .build()))
                .instanceType(InstanceType.of(InstanceClass.T2, InstanceSize.MICRO))
                .multiAz(false)
                .allocatedStorage(10)
                .securityGroups(Collections.singletonList(iSecurityGroup))
                .vpcSubnets(SubnetSelection.builder()
                        //.subnets(vpc.getPrivateSubnets())
                        .subnets(vpc.getPublicSubnets()) // //para quando definir .natGateways(0) na VPC
                        .build())
                .build();

        CfnOutput.Builder.create(this, "demo-db-endpoint")
                .exportName("demo-db-endpoint")
                .value(database.getDbInstanceEndpointAddress())
                .build();

        CfnOutput.Builder.create(this, "demo-db-senha")
                .exportName("demo-db-senha")
                .value(senha.getValueAsString())
                .build();

    }
}
