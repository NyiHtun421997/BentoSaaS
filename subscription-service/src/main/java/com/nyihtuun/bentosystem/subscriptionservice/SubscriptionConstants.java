package com.nyihtuun.bentosystem.subscriptionservice;

public class SubscriptionConstants {
    private SubscriptionConstants() {
    }

    public static final String ASIA_TOKYO_ZONE = "Asia/Tokyo";
    public static final String SCHEDULER_FIXED_RATE = "${subscription.outbox-scheduler-fixed-rate}";
    public static final String SCHEDULER_INITIAL_DELAY = "${subscription.outbox-scheduler-initial-delay}";
    public static final String SUBSCRIPTION_USER_SUBSCRIPTION_TOPIC_NAME = "${subscription.user-subscription-topic-name}";
    public static final String SUBSCRIPTION_PLAN_CHANGED_TOPIC_NAME = "${subscription.plan-changed-topic-name}";
    public static final String SUBSCRIPTION_GROUP_ID = "${subscription.group-id}";
}
