package com.nyihtuun.bentosystem.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"addressEntity", "roleEntities"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "user", schema = "userinfo")
public class UserEntity {

    @Id
    @Column(name = "user_id")
    @EqualsAndHashCode.Include
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "encrypted_password", nullable = false, unique = true)
    private String encryptedPassword;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private AddressEntity addressEntity;

    @Column(name = "ph_no", nullable = false)
    private String phNo;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "description")
    private String description;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            schema = "userinfo",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roleEntities;
}
