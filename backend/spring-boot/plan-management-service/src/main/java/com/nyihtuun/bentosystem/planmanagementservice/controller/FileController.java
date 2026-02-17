package com.nyihtuun.bentosystem.planmanagementservice.controller;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.FileService;
import com.nyihtuun.bentosystem.planmanagementservice.configuration.AwsConfigData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.Normalizer;
import java.util.Map;

import static com.nyihtuun.bentosystem.planmanagementservice.controller.ApiPaths.*;

@Slf4j
@RestController
@RequestMapping(VERSION1 + PROVIDER_PLAN + FILE)
@Tag(name = "File", description = "Endpoints for file operations, such as generating pre-signed URLs for plan-related uploads.")
public class FileController {

    private final FileService fileService;
    private final AwsConfigData awsConfigData;

    public FileController(FileService fileService, AwsConfigData awsConfigData) {
        this.fileService = fileService;
        this.awsConfigData = awsConfigData;
    }

    @PostMapping(PLAN)
    @Operation(summary = "Generate pre-signed URL", description = "Generates a pre-signed S3 URL for uploading a plan-related file.")
    @ApiResponse(responseCode = "200", description = "Pre-signed URL generated successfully")
    public ResponseEntity<Map<String, Object>> generateUrlForPlan(@RequestParam(name = "filename", required = false, defaultValue = "") String filename) {
        log.info("Generating plan's pre-signed URL for file: {}", filename);
        filename = buildFilename(filename);
        String url = fileService.getPresignedUploadUrl(filename, awsConfigData.planImageFolder());
        log.info("Pre-signed plan's URL generated: {}", url);
        return ResponseEntity.ok(Map.of("url", url, "file", filename));
    }

    @PostMapping(MEAL)
    @Operation(summary = "Generate pre-signed URL", description = "Generates a pre-signed S3 URL for uploading a meal-related file.")
    @ApiResponse(responseCode = "200", description = "Pre-signed URL generated successfully")
    public ResponseEntity<Map<String, Object>> generateUrlForMeal(@RequestParam(name = "filename", required = false, defaultValue = "") String filename) {
        log.info("Generating meal's pre-signed URL for file: {}", filename);
        filename = buildFilename(filename);
        String url = fileService.getPresignedUploadUrl(filename, awsConfigData.planMealImageFolder());
        log.info("Pre-signed meal's URL generated: {}", url);
        return ResponseEntity.ok(Map.of("url", url, "file", filename));
    }

    public String buildFilename(String filename) {
        return String.format("%s_%s", System.currentTimeMillis(), sanitizeFileName(filename));
    }

    private String sanitizeFileName(String fileName) {
        String normalizedFileName = Normalizer.normalize(fileName, Normalizer.Form.NFKD);
        return normalizedFileName.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9.\\-_]", "");
    }
}
