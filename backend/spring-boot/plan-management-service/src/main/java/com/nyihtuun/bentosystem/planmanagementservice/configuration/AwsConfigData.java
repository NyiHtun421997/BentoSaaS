package com.nyihtuun.bentosystem.planmanagementservice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public record AwsConfigData (
        String region,
        String bucketName,
        String planImageFolder,
        String planMealImageFolder,
        Long expirationTimeMin
) {}
