package com.nyihtuun.bentosystem.planmanagementservice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "plan-management")
public record PlanManagementConfigData(
        String schedulerCron,
        String zone,
        String outboxSchedulerFixedRate,
        String outboxSchedulerInitialDelay,
        String planChangedTopicName,
        String userSubscriptionTopicName,
        String planChangedNotificationTopicName,
        String userNotificationTopicName,
        String groupId
) {}
