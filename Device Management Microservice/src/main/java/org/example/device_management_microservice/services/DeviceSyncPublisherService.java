package org.example.device_management_microservice.services;


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

    public void publishDeviceCreated(String deviceId) {
        SyncEvent event = new SyncEvent("DEVICE", "CREATED", deviceId);
        try {
            String json = objectMapper.writeValueAsString(event);
            System.out.println("Device created with id: " + deviceId);
            rabbitTemplate.convertAndSend(SYNC_EXCHANGE, DEVICE_SYNC_ROUTING_KEY, json);
        } catch (Exception e) {
            e.printStackTrace(); // Or use a logger
        }
    }
}
