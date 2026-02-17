package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service;

public interface FileService {
    String getPresignedUploadUrl(String imageKey, String parentKey);
    String generatePresignedUrl(String imageKey, String parentKey);
}
