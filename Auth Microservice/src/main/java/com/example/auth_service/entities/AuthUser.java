package com.example.auth_service.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_users", indexes = {
        @Index(name = "ux_auth_users_username", columnList = "username", unique = true)
})
public class AuthUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;
    // TODO change field name to password for cleaner json
    @Column(nullable = false, length = 1000)
    private String passwordHash;

    public AuthUser() {
    }

    public AuthUser(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
