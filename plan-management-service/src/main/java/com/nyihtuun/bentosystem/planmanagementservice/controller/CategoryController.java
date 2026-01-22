package com.nyihtuun.bentosystem.planmanagementservice.controller;

import com.nyihtuun.bentosystem.domain.dto.CategoryDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.PlanManagementCommandService;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.PlanManagementQueryService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.nyihtuun.bentosystem.planmanagementservice.controller.ApiPaths.CATEGORY;
import static com.nyihtuun.bentosystem.planmanagementservice.controller.ApiPaths.VERSION1;

@Slf4j
@RestController
@RequestMapping(VERSION1 + CATEGORY)
@Tag(name = "Category", description = "Endpoints for managing bento categories.")
public class CategoryController {
    private final PlanManagementQueryService planManagementQueryService;
    private final PlanManagementCommandService planManagementCommandService;

    @Autowired
    public CategoryController(PlanManagementQueryService planManagementQueryService,
                              PlanManagementCommandService planManagementCommandService) {
        this.planManagementQueryService = planManagementQueryService;
        this.planManagementCommandService = planManagementCommandService;
    }

    @PostMapping
    @Operation(summary = "Create category", description = "Adds a new bento category to the system.")
    @ApiResponse(responseCode = "200", description = "Category created")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        log.info("Creating category: {}", categoryDto.getName());
        CategoryDto saved = planManagementCommandService.createCategory(categoryDto);
        log.info("Category created: {}", saved);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves a list of all available bento categories.")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        log.info("Fetching all categories");
        List<CategoryDto> categories = planManagementQueryService.getCategories();
        log.info("All categories: {}", categories);
        return ResponseEntity.ok(categories);
    }
}
