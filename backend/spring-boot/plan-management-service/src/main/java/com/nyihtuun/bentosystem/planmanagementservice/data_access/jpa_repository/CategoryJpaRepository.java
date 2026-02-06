package com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_repository;

import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {
}
