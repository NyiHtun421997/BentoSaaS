package com.nyihtuun.bentosystem.planmanagementservice.application_service.impl.service;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.FileService;
import com.nyihtuun.bentosystem.planmanagementservice.configuration.AwsConfigData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Slf4j
@AllArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    private final AwsConfigData awsConfigData;
    private final S3Presigner s3Presigner;

    @Override
    public String getPresignedUploadUrl(String imageKey, String parentKey) {
        log.info("Generating pre-signed URL for file: {}", imageKey);
        if (imageKey == null) {
            log.warn("Image key for upload url is null. Returning empty string.");
            return "";
        }
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsConfigData.bucketName())
                .key(parentKey + imageKey)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(awsConfigData.expirationTimeMin()))
                .putObjectRequest(putObjectRequest)
                .build();

        log.info("Pre-signed URL generated for upload image: {}", imageKey);
        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    @Override
    public String generatePresignedUrl(String imageKey, String parentKey) {
        log.info("Generating pre-signed URL for image: {}", imageKey);
        if (imageKey == null) {
            log.warn("Image key is null. Returning empty string.");
            return "";
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                                                            .bucket(awsConfigData.bucketName())
                                                            .key(parentKey + imageKey)
                                                            .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                                       .signatureDuration(Duration.ofMinutes(awsConfigData.expirationTimeMin()))
                                       .getObjectRequest(getObjectRequest)
                                       .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        log.info("Pre-signed URL generated for image: {}", imageKey);
        return presignedRequest.url().toString();
    }
}
