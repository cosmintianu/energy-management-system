package org.example.monitoring_microservice.dto;

public class SyncEvent {
    private String type; // "DEVICE"
    private String event; // "CREATED", "UPDATED", "DELETED"
    private String id;
    private Double maxConsumption; // Max hourly consumption threshold in kWh

    public SyncEvent() {}

    public SyncEvent(String type, String event, String id) {
        this.type = type;
        this.event = event;
        this.id = id;
    }

    public SyncEvent(String type, String event, String id, Double maxConsumption) {
        this.type = type;
        this.event = event;
        this.id = id;
        this.maxConsumption = maxConsumption;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Double getMaxConsumption() { return maxConsumption; }
    public void setMaxConsumption(Double maxConsumption) { this.maxConsumption = maxConsumption; }
}
