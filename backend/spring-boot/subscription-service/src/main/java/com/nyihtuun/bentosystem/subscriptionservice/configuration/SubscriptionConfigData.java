package com.nyihtuun.bentosystem.subscriptionservice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "subscription")
public record SubscriptionConfigData(
        String planManagementServiceUrl,
        String planManagementVersion,
        String planManagementPlanDetailsPath,
        long connectTimeoutSeconds,
        long readTimeoutSeconds,
        String[] listenTopicNames,
        String planChangedTopicName,
        String planChangedNotificationTopicName,
        String userSubscriptionTopicName,
        String userNotificationTopicName,
        String groupId,
        String outboxSchedulerFixedRate,
        String outboxSchedulerInitialDelay) {}
