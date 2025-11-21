package org.example.device_management_microservice.dtos;

import java.util.Objects;
import java.util.UUID;

public class DeviceDTO {
    private UUID id;
    private String name;
    private double max_consumption;
    private String ownerUsername;

    public DeviceDTO(UUID id, String name, double max_consumption, String ownerUsername) {
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

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDTO that = (DeviceDTO) o;
        return max_consumption == that.max_consumption && Objects.equals(name, that.name);
    }
    @Override public int hashCode() { return Objects.hash(name, max_consumption); }
}
