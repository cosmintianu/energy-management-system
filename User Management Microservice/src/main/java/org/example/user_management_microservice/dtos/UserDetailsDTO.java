package org.example.user_management_microservice.dtos;

import jakarta.validation.constraints.NotBlank;
import org.example.user_management_microservice.entities.enums.UserRole;

import java.util.Objects;
import java.util.UUID;

public class UserDetailsDTO {
    private UUID id;

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    private String password;

//    @NotBlank(message = "role is required")
    private UserRole role;

    @NotBlank(message = "address is required")
    private String address;

    public UserDetailsDTO() {
    }

    public UserDetailsDTO(String username, String password, UserRole role, String address) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
    }

    public UserDetailsDTO(UUID id, String username, String password, UserRole role, String address) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsDTO that = (UserDetailsDTO) o;
        return username.equals(that.username) &&
                password.equals(that.password) &&
                address.equals(that.address) &&
                role.equals(that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, address);
    }
}
