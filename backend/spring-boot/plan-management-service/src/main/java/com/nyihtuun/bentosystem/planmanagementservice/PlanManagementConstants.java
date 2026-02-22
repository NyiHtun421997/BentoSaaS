package com.nyihtuun.bentosystem.planmanagementservice;

public class PlanManagementConstants {
    private PlanManagementConstants() {
    }

    public static final String PLAN_MANAGEMENT_SCHEDULER_CRON = "${plan-management.scheduler-cron}";
    public static final String PLAN_MANAGEMENT_ZONE = "${plan-management.zone}";
    public static final String SCHEDULER_FIXED_RATE = "${plan-management.outbox-scheduler-fixed-rate}";
    public static final String SCHEDULER_INITIAL_DELAY = "${plan-management.outbox-scheduler-initial-delay}";
    public static final String PLAN_MANAGEMENT_PLAN_CHANGED_TOPIC_NAME = "${plan-management.plan-changed-topic-name}";
    public static final String PLAN_MANAGEMENT_USER_SUBSCRIPTION_TOPIC_NAME = "${plan-management.user-subscription-topic-name}";
    public static final String PLAN_MANAGEMENT_GROUP_ID = "${plan-management.group-id}";
}
