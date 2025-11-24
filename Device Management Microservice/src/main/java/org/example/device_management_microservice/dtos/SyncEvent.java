package org.example.device_management_microservice.dtos;

public class SyncEvent {
    private String type; // "DEVICE"
    private String event; // "CREATED"
    private String id;

    public SyncEvent() {}

    public SyncEvent(String type, String event, String id) {
        this.type = type;
        this.event = event;
        this.id = id;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}

