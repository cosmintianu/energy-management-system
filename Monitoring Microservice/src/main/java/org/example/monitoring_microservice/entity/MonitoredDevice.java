package org.example.monitoring_microservice.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class MonitoredDevice {
    @Id
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "max_consumption", nullable = false)
    private double maxConsumption; // Max hourly consumption threshold in kWh

    @Column(name = "owner_username")
    private String ownerUsername; // Username of the device owner for notifications

    public UUID getDeviceId() { return deviceId; }

    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public double getMaxConsumption() { return maxConsumption; }

    public void setMaxConsumption(double maxConsumption) { this.maxConsumption = maxConsumption; }

    public String getOwnerUsername() { return ownerUsername; }

    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
}
