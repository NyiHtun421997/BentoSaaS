package com.nyihtuun.bentosystem.subscriptionservice.data_access.mapper;

import com.google.protobuf.Message;

public interface OutboxPayloadReader {
    Message read(String payload);
}