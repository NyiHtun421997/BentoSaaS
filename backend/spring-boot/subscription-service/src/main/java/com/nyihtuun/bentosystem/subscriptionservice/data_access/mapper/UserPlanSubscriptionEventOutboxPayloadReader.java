package com.nyihtuun.bentosystem.subscriptionservice.data_access.mapper;

import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import subscription.events.UserPlanSubscriptionEvent;

import static com.nyihtuun.bentosystem.domain.utility.CommonConstants.DATA_CHANGED;

@Slf4j
@Component(DATA_CHANGED)
public class UserPlanSubscriptionEventOutboxPayloadReader implements OutboxPayloadReader {
    @Override
    public UserPlanSubscriptionEvent read(String payload) {
        try {
            UserPlanSubscriptionEvent.Builder builder = UserPlanSubscriptionEvent.newBuilder();
            JsonFormat.parser()
                      .ignoringUnknownFields()
                      .merge(payload, builder);
            return builder.build();
        } catch (Exception e) {
            log.error("Could not read UserPlanSubscriptionEvent from string!", e);
            throw new RuntimeException("Could not read UserPlanSubscriptionEvent from string!", e);
        }
    }
}