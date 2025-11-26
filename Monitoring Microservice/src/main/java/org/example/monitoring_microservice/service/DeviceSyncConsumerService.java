package org.example.monitoring_microservice.service;

import jakarta.transaction.Transactional;
import org.example.monitoring_microservice.dto.SyncEvent;
import org.example.monitoring_microservice.entity.MonitoredDevice;
import org.example.monitoring_microservice.repository.MonitoredDeviceRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.example.monitoring_microservice.config.DeviceSyncRabbitConfig.DEVICE_SYNC_QUEUE;

@Service
public class DeviceSyncConsumerService {

    private final ObjectMapper objectMapper;
    private final MonitoredDeviceRepository deviceRepo;

    public DeviceSyncConsumerService(ObjectMapper objectMapper,
                                     MonitoredDeviceRepository deviceRepo) {
        this.objectMapper = objectMapper;
        this.deviceRepo = deviceRepo;
    }

    @RabbitListener(queues = DEVICE_SYNC_QUEUE)
    @Transactional
    public void handleSyncEvent(String json) {
        try {
            SyncEvent event = objectMapper.readValue(json, SyncEvent.class);

            if (!"DEVICE".equalsIgnoreCase(event.getType())) {
                return;
            }

            if ("CREATED".equalsIgnoreCase(event.getEvent())) {
                handleDeviceCreated(event.getId());
            } else if ("DELETED".equalsIgnoreCase(event.getEvent())) {
                handleDeviceDeleted(event.getId());
            }
        } catch (Exception e) {
            System.err.println("Failed to process sync event " + json + " exception: " + e);
        }
    }

    private void handleDeviceCreated(String id) {
        UUID deviceId = UUID.fromString(id);
        if (!deviceRepo.existsById(deviceId)) {
            MonitoredDevice d = new MonitoredDevice();
            d.setDeviceId(deviceId);
            deviceRepo.save(d);
            System.out.println("Device created sync event consumed " + deviceId);

        }
    }

    private void handleDeviceDeleted(String id) {
        UUID deviceId = UUID.fromString(id);
        if (deviceRepo.existsById(deviceId)) {
            deviceRepo.deleteById(deviceId);
            System.out.println("Device deleted sync event consumed " + deviceId);

        }
    }
}
