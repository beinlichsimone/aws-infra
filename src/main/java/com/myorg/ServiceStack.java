package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;

import java.util.HashMap;
import java.util.Map;

public class ServiceStack extends Stack {
    public ServiceStack(final Construct scope, final String id, final Cluster cluster) {

        this(scope, id, null, cluster);
    }

    public ServiceStack(final Construct scope, final String id, final StackProps props, final Cluster cluster) {
        super(scope, id, props);

        Map<String, String> parametros= new HashMap<>();
        parametros.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://" + Fn.importValue("demo-db-endpoint")
                + ":5432/postgres?createDatabaseIfNotExist=true");
        parametros.put("SPRING_DATASOURCE_USERNAME", "postgres");
        parametros.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("demo-db-senha"));
        //parametros.put("SPRING_PROFILES_ACTIVE", "dev");

        IRepository repositorio = Repository.fromRepositoryName(this, "repo-demo", "repo-demo");

        // Create a load-balanced Fargate service and make it public
        ApplicationLoadBalancedFargateService demoService = ApplicationLoadBalancedFargateService.Builder.create(this, "DemoFargateService")
                .serviceName("service-demo")
                .cluster(cluster)           // Required
                .cpu(256)                   // Default is 256
                .memoryLimitMiB(512)       // Default is 512
                .desiredCount(2)            // Default is 1
                .listenerPort(8080)
                .assignPublicIp(true) //para quando definir .natGateways(0) na VPC
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                //.image(ContainerImage.fromEcrRepository(repositorio)) Busca direto do repositório do ECR
                                .image(ContainerImage.fromRegistry("simonebeinlich/vacation-trip:0.0.1-SNAPSHOT")) // Pega do Docker Hub.
                                .containerPort(8080)
                                .containerName("app_demo")
                                .environment(parametros)
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                        .logGroup(LogGroup.Builder.create(this, "DemoLogGroup")
                                                .logGroupName("DemoLog")
                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                .build())
                                        .streamPrefix("Demo")
                                        .build()))
                                .build())
                .publicLoadBalancer(true)   // Default is false
                .build();

        demoService.getTargetGroup().configureHealthCheck(HealthCheck.builder()
                .port("8080")
                .path("/hello").interval(Duration.seconds(30)) // Intervalo entre verificações
                .healthyHttpCodes("200")   // Códigos HTTP que indicam que o serviço está saudável
                //.unhealthyThresholdCount(2)
                .build());

        //definições de Auto Scaling
        ScalableTaskCount scalableTarget = demoService.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(1)
                .maxCapacity(2)
                .build());

        scalableTarget.scaleOnCpuUtilization("CpuScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(70)
                .scaleInCooldown(Duration.minutes(3))
                .scaleOutCooldown(Duration.minutes(2))
                .build());

        scalableTarget.scaleOnMemoryUtilization("MemoryScaling", MemoryUtilizationScalingProps.builder()
                .targetUtilizationPercent(65)
                .scaleInCooldown(Duration.minutes(3))
                .scaleOutCooldown(Duration.minutes(2))
                .build());
    }
}
