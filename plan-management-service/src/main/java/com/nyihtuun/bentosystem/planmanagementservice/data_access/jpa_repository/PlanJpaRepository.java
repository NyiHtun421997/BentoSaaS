package com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_repository;

import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.PlanEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanJpaRepository extends JpaRepository<PlanEntity, UUID> {
    public List<PlanEntity> findPlanEntitiesByDeleteFlag(Boolean deleteFlag, Pageable pageable);
    public Optional<PlanEntity> findPlanEntityByTitleContainingIgnoreCaseAndCode(String title, String code);

    @Query("select distinct p from PlanEntity p join p.categoryEntities c where c.id = :categoryId and p.deleteFlag = false")
    List<PlanEntity> findPlanEntityByCategoryId(@Param("categoryId") UUID categoryId);

    List<PlanEntity> findPlanEntitiesByDeleteFlagFalseAndCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    List<PlanEntity> findPlanEntitiesByDeleteFlagFalseAndCreatedAtBetweenAndPlanStatus(LocalDateTime from, LocalDateTime to,
                                                                                       PlanStatus planStatus);
    List<PlanEntity> findPlanEntitiesByUserId(UUID userId);

    @NativeQuery(
            value = """
    SELECT p.*
    FROM planmanagement.plan p
    JOIN planmanagement.address a ON a.id = p.address_id
    WHERE p.delete_flag = false
      AND ST_DWithin(
            a.location,
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
            :radiusMeters
          )
    ORDER BY ST_Distance(
            a.location,
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
          )
    """
    )
    List<PlanEntity> findActivePlansNearLocation(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMeters") double radiusMeters
    );
}
