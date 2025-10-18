package org.example.user_management_microservice.dtos;

import org.example.user_management_microservice.entities.enums.UserRole;

import java.util.UUID;

public class UserDTO {
    private UUID id;
    private String username;
    private String password;
    private UserRole role;


    public UserDTO() {
    }

    public UserDTO(UUID id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

