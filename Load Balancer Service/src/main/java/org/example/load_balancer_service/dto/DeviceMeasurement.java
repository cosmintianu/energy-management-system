package org.example.load_balancer_service.dto;

import java.time.Instant;
import java.util.UUID;

public class DeviceMeasurement {

    private UUID deviceId;
    private Instant timestamp;
    private double energy;

    public DeviceMeasurement() {}

    public DeviceMeasurement(UUID deviceId, Instant timestamp, double energy) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.energy = energy;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    @Override
    public String toString() {
        return "DeviceMeasurement{" +
                "deviceId=" + deviceId +
                ", timestamp=" + timestamp +
                ", energy=" + energy +
                '}';
    }
}
