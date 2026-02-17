package com.nyihtuun.bentosystem.userservice.controller;

import com.nyihtuun.bentosystem.userservice.service.FileService;
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

import static com.nyihtuun.bentosystem.userservice.controller.ApiPaths.*;

@Slf4j
@RestController
@RequestMapping(VERSION1 + USER +FILE)
@Tag(name = "File", description = "Endpoints for file operations, such as generating pre-signed URLs for uploads.")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    @Operation(summary = "Generate pre-signed URL", description = "Generates a pre-signed S3 URL for uploading a file.")
    @ApiResponse(responseCode = "200", description = "Pre-signed URL generated successfully")
    public ResponseEntity<Map<String, Object>> generateUrl(@RequestParam(name = "filename", required = false, defaultValue = "") String filename) {
        log.info("Generating pre-signed URL for file: {}", filename);
        filename = buildFilename(filename);
        String url = fileService.getPresignedUploadUrl(filename);
        log.info("Pre-signed URL generated: {}", url);
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
