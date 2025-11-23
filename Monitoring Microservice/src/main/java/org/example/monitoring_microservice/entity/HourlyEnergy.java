package org.example.monitoring_microservice.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "hourly_energy",
        uniqueConstraints = @UniqueConstraint(columnNames = {"device_id", "hour_start"})
)
public class HourlyEnergy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "hour_start", nullable = false)
    private Instant hourStart;

    @Column(name = "total_energy", nullable = false)
    private double totalEnergy;

    public HourlyEnergy() {}

    public UUID getId() {
        return id;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public Instant getHourStart() {
        return hourStart;
    }

    public void setHourStart(Instant hourStart) {
        this.hourStart = hourStart;
    }

    public double getTotalEnergy() {
        return totalEnergy;
    }

    public void setTotalEnergy(double totalEnergy) {
        this.totalEnergy = totalEnergy;
    }
}
