package com.example.auth_service.repositories;

import com.example.auth_service.entities.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
    Optional<AuthUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
