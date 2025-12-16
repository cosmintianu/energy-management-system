package org.example.monitoring_microservice.service;

import jakarta.transaction.Transactional;
import org.example.monitoring_microservice.dto.SyncEvent;
import org.example.monitoring_microservice.entity.MonitoredDevice;
import org.example.monitoring_microservice.repository.MonitoredDeviceRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
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

            switch (event.getEvent().toUpperCase()) {
                case "CREATED":
                    handleDeviceCreated(event.getId(), event.getMaxConsumption(), event.getOwnerUsername());
                    break;
                case "UPDATED":
                    handleDeviceUpdated(event.getId(), event.getMaxConsumption(), event.getOwnerUsername());
                    break;
                case "DELETED":
                    handleDeviceDeleted(event.getId());
                    break;
            }
        } catch (Exception e) {
            System.err.println("Failed to process sync event " + json + " exception: " + e);
        }
    }

    private void handleDeviceCreated(String id, Double maxConsumption, String ownerUsername) {
        UUID deviceId = UUID.fromString(id);
        if (!deviceRepo.existsById(deviceId)) {
            MonitoredDevice d = new MonitoredDevice();
            d.setDeviceId(deviceId);
            if (maxConsumption != null) {
                d.setMaxConsumption(maxConsumption);
            }
            if (ownerUsername != null) {
                d.setOwnerUsername(ownerUsername);
            }
            deviceRepo.save(d);
            System.out.println("Device created sync event consumed: " + deviceId + 
                    " with maxConsumption: " + d.getMaxConsumption() +
                    " owner: " + d.getOwnerUsername());
        }
    }

    private void handleDeviceUpdated(String id, Double maxConsumption, String ownerUsername) {
        UUID deviceId = UUID.fromString(id);
        Optional<MonitoredDevice> deviceOpt = deviceRepo.findById(deviceId);
        if (deviceOpt.isPresent()) {
            MonitoredDevice device = deviceOpt.get();
            if (maxConsumption != null) {
                device.setMaxConsumption(maxConsumption);
            }
            if (ownerUsername != null) {
                device.setOwnerUsername(ownerUsername);
            }
            deviceRepo.save(device);
            System.out.println("Device updated sync event consumed: " + deviceId + 
                    " new maxConsumption: " + maxConsumption +
                    " owner: " + ownerUsername);
        } else {
            // Device doesn't exist, create it
            handleDeviceCreated(id, maxConsumption, ownerUsername);
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
