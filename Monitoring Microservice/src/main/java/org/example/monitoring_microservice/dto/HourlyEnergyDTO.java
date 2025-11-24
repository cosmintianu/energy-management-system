package org.example.monitoring_microservice.dto;

public class HourlyEnergyDTO {
    private int hour;          // 0-23
    private double totalEnergy; // kWh

    public HourlyEnergyDTO() {}

    public HourlyEnergyDTO(int hour, double totalEnergy) {
        this.hour = hour;
        this.totalEnergy = totalEnergy;
    }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }
    public double getTotalEnergy() { return totalEnergy; }
    public void setTotalEnergy(double totalEnergy) { this.totalEnergy = totalEnergy; }
}
