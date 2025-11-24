package org.example.monitoring_microservice.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class MonitoredDevice {
    @Id
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    public UUID getDeviceId() {return deviceId;}

    public void setDeviceId(UUID deviceId) {this.deviceId = deviceId;}
}
