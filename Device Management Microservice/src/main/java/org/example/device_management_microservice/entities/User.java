package org.example.device_management_microservice.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue()
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "user_id")
    private UUID id;

    @Column(name = "username", unique = true,
            nullable = false)
    private String username;

    @OneToMany(mappedBy = "owner",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Device> devices = new ArrayList<>();

    public User() {
    }

    public User(String username) {
        this.username = username;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID userId) {
        this.id = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void addDevice(Device device) {
        devices.add(device);
        device.setOwner(this);
    }

    public void removeDevice(Device device) {
        devices.remove(device);
        device.setOwner(null);
    }
}
