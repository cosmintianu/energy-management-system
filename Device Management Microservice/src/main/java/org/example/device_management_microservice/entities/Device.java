package org.example.device_management_microservice.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "max_consumption", nullable = false)
    private double maxConsumption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_username",
            referencedColumnName = "username",
            nullable = false)
    private User owner;

    public Device() {
    }

    public Device(String name, double maxConsumption) {
        this.name = name;
        this.maxConsumption = maxConsumption;
    }

    public Device(String name, double max_consumption, User owner) {
        this.name = name;
        this.maxConsumption = max_consumption;
        this.owner = owner;
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

    public double getMaxConsumption() {
        return maxConsumption;
    }

    public void setMaxConsumption(double maxConsumption) {
        this.maxConsumption = maxConsumption;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
