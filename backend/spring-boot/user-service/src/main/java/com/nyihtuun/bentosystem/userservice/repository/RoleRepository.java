package com.nyihtuun.bentosystem.userservice.repository;

import com.nyihtuun.bentosystem.userservice.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    RoleEntity findByName(RoleEntity.Role role);
}
