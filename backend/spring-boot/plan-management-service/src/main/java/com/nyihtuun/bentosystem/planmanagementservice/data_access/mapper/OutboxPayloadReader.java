package com.nyihtuun.bentosystem.planmanagementservice.data_access.mapper;

import com.google.protobuf.Message;

public interface OutboxPayloadReader {
    Message read(String payload);
}
