package org.example.user_management_microservice.dtos;

import jakarta.validation.constraints.NotBlank;

import java.util.Objects;
import java.util.UUID;

public class UserDetailsDTO {
    private UUID id;

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "address is required")
    private String address;

    @NotBlank(message = "email is required")
    private String email;

    public UserDetailsDTO() {
    }

    public UserDetailsDTO(String username, String address, String email) {
        this.username = username;
        this.address = address;
        this.email = email;
    }

    public UserDetailsDTO(UUID id, String username, String address, String email) {
        this.id = id;
        this.username = username;
        this.address = address;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
                email.equals(that.email) &&
                address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email, address);
    }
}
