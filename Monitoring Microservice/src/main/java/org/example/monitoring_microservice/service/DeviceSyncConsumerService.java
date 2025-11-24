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
            if (!"DEVICE".equalsIgnoreCase(event.getType())
                    || !"CREATED".equalsIgnoreCase(event.getEvent()))
                return;

            UUID deviceId = UUID.fromString(event.getId());
            if (!deviceRepo.existsById(deviceId)) {
                MonitoredDevice d = new MonitoredDevice();
                d.setDeviceId(deviceId);
                System.out.println("Received device with id " + deviceId);
                deviceRepo.save(d);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Or use a logger
        }
    }
}
