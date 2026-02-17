package com.nyihtuun.bentosystem.userservice.service;

import com.nyihtuun.bentosystem.userservice.configuration.AwsConfigData;
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
    public String getPresignedUploadUrl(String imageKey) {
        log.info("Generating pre-signed URL for file: {}", imageKey);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsConfigData.bucketName())
                .key(awsConfigData.userImageFolder() + imageKey)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(awsConfigData.expirationTimeMin()))
                .putObjectRequest(putObjectRequest)
                .build();

        log.info("Pre-signed URL generated for file: {}", imageKey);
        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    @Override
    public String generatePresignedUrl(String imageKey) {
        log.info("Generating pre-signed URL for image: {}", imageKey);
        if (imageKey == null || imageKey.isBlank()) {
            log.warn("Image key is null or blank. Returning empty string.");
            return "";
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                                                            .bucket(awsConfigData.bucketName())
                                                            .key(awsConfigData.userImageFolder() + imageKey)
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
