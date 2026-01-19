package com.nyihtuun.bentosystem.domain.utility;

public class MessageUtil {
    private MessageUtil() {
    }

    public static final String PLAN_ERROR = "plan.error.";

    public static String toKey(String code) {
        return code.toLowerCase().replace('_', '-');
    }
}
