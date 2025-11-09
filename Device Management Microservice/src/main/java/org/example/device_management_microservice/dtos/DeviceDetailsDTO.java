package org.example.device_management_microservice.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

public class DeviceDetailsDTO {

    private UUID id;

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "max consumption value is required")
    private double max_consumption;

    @NotNull(message = "owner username is required")
    private String ownerUsername;

    public DeviceDetailsDTO() {
    }

    public DeviceDetailsDTO(UUID id, String name, double max_consumption, String ownerUsername) {
        this.id = id;
        this.name = name;
        this.max_consumption = max_consumption;
        this.ownerUsername = ownerUsername;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMax_consumption() {
        return max_consumption;
    }

    public void setMax_consumption(double max_consumption) {
        this.max_consumption = max_consumption;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDetailsDTO that = (DeviceDetailsDTO) o;
        return max_consumption == that.max_consumption &&
                Objects.equals(name, that.name) &&
                Objects.equals(ownerUsername, that.ownerUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ownerUsername, max_consumption);
    }
}
