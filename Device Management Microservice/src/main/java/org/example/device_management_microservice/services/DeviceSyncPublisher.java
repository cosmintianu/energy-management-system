package org.example.device_management_microservice.services;


import com.fasterxml.jackson.core.JsonProcessingException;

import org.example.device_management_microservice.dtos.SyncEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import static org.example.device_management_microservice.config.SyncRabbitConfig.SYNC_EXCHANGE;
import static org.example.device_management_microservice.config.SyncRabbitConfig.SYNC_ROUTING_KEY;


@Service
public class DeviceSyncPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public DeviceSyncPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishDeviceCreated(String deviceId) {
        SyncEvent event = new SyncEvent("DEVICE", "CREATED", deviceId);
        try {
            String json = objectMapper.writeValueAsString(event);
            System.out.println("Device created with id: " + deviceId);
            rabbitTemplate.convertAndSend(SYNC_EXCHANGE, SYNC_ROUTING_KEY, json);
        } catch (Exception e) {
            e.printStackTrace(); // Or use a logger
        }
    }
}
