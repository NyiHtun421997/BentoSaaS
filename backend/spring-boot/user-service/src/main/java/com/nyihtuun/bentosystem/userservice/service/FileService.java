package com.nyihtuun.bentosystem.userservice.service;

public interface FileService {
    String getPresignedUploadUrl(String imageKey);
    String generatePresignedUrl(String imageKey);
}
