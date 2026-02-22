package com.nyihtuun.bentosystem.planmanagementservice.data_access.mapper;

import com.google.protobuf.util.JsonFormat;
import plan_management.events.PlanChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.nyihtuun.bentosystem.domain.utility.CommonConstants.DATA_CHANGED;

@Slf4j
@Component(DATA_CHANGED)
public class PlanChangedEventOutboxPayloadReader implements OutboxPayloadReader {
    @Override
    public PlanChangedEvent read(String payload) {
        try {
            PlanChangedEvent.Builder builder = PlanChangedEvent.newBuilder();
            JsonFormat.parser()
                      .ignoringUnknownFields()
                      .merge(payload, builder);
            return builder.build();
        } catch (Exception e) {
            log.error("Could not read PlanChangedEvent from string!", e);
            throw new RuntimeException("Could not read PlanChangedEvent from string!", e);
        }
    }
}
