package com.nyihtuun.bentosystem.userservice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public record AwsConfigData (
        String region,
        String bucketName,
        String userImageFolder,
        Long expirationTimeMin
) {}
