package com.myorg;

import software.amazon.awscdk.App;

public class AwsInfraApp {
    public static void main(final String[] args) {
        App app = new App();

        VpcStack vpc = new VpcStack(app, "Vpc");

        ClusterStack cluster = new ClusterStack(app, "Cluster", vpc.getVpc());
        cluster.addDependency(vpc);

        RdsStack rds = new RdsStack(app, "Rds", vpc.getVpc());
        rds.addDependency(vpc);

        ServiceStack service = new ServiceStack(app, "Service", cluster.getCluster());
        service.addDependency(cluster);
        service.addDependency(rds);

        app.synth();
    }
}

