package org.example.device_management_microservice.services;


import jakarta.xml.bind.SchemaOutputResolver;
import org.example.device_management_microservice.dtos.SyncEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import static org.example.device_management_microservice.config.DeviceSyncRabbitConfig.*;


@Service
public class DeviceSyncPublisherService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public DeviceSyncPublisherService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishDeviceDeleted(String deviceId) {
        SyncEvent event = new SyncEvent("DEVICE", "DELETED", deviceId);
        try {
            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(SYNC_EXCHANGE, DEVICE_SYNC_ROUTING_KEY, json);
            System.out.println("Device deleted sync event published " + json);
        } catch (Exception e) {
            System.err.println("Failed to publish DEVICE.DELETED event " + e);
        }
    }

    public void publishDeviceCreated(String deviceId) {
        SyncEvent event = new SyncEvent("DEVICE", "CREATED", deviceId);
        try {
            String json = objectMapper.writeValueAsString(event);
            System.out.println("Device created sync event published " + json);
            rabbitTemplate.convertAndSend(SYNC_EXCHANGE, DEVICE_SYNC_ROUTING_KEY, json);
        } catch (Exception e) {
            e.printStackTrace(); // Or use a logger
        }
    }
}
