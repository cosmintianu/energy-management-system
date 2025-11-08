package com.example.auth_service.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "auth_users")
public class AuthUser {
    @Id
    @GeneratedValue()
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 100) // BCrypt encrypts it up to 72 bytes
    private String password;

    public AuthUser() {
    }

    public AuthUser(String username, String passwordHash) {
        this.username = username;
        this.password = passwordHash;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String passwordHash) { this.password = passwordHash; }
}
