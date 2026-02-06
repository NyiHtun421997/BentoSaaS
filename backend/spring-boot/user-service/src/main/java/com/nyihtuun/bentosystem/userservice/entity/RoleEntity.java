package com.nyihtuun.bentosystem.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"userEntities"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "role", schema = "userinfo")
public class RoleEntity {

    public enum Role {
        ROLE_USER, ROLE_ADMIN, ROLE_PROVIDER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "name", nullable = false)
    private Role name;

    @ManyToMany(mappedBy = "roleEntities", fetch = FetchType.LAZY)
    private Set<UserEntity> userEntities;
}
