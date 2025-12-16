package org.example.monitoring_microservice.service;

import org.example.monitoring_microservice.dto.DeviceMeasurement;
import org.example.monitoring_microservice.entity.HourlyEnergy;
import org.example.monitoring_microservice.entity.MonitoredDevice;
import org.example.monitoring_microservice.repository.HourlyEnergyRepository;
import org.example.monitoring_microservice.repository.MonitoredDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeviceDataConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceDataConsumerService.class);

    private final ObjectMapper objectMapper;
    private final HourlyEnergyRepository hourlyEnergyRepository;
    private final MonitoredDeviceRepository monitoredDeviceRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ZoneId zoneId;
    private final String overconsumptionQueue;
    private final String instanceId;

    public DeviceDataConsumerService(ObjectMapper objectMapper,
                                     HourlyEnergyRepository hourlyEnergyRepository,
                                     MonitoredDeviceRepository monitoredDeviceRepository,
                                     RabbitTemplate rabbitTemplate,
                                     @Value("${monitoring.timezone}") String timezone,
                                     @Value("${monitoring.overconsumption-queue}") String overconsumptionQueue,
                                     @Value("${monitoring.instance-id}") String instanceId) {
        this.objectMapper = objectMapper;
        this.hourlyEnergyRepository = hourlyEnergyRepository;
        this.monitoredDeviceRepository = monitoredDeviceRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.zoneId = ZoneId.of(timezone);
        this.overconsumptionQueue = overconsumptionQueue;
        this.instanceId = instanceId;
    }

    @RabbitListener(queues = "${monitoring.queue}")
    @Transactional
    public void receiveMeasurement(String json) {
        try {
            DeviceMeasurement measurement =
                    objectMapper.readValue(json, DeviceMeasurement.class);

            LOGGER.info("[Instance {}] Received measurement: {}", instanceId, measurement);

            UUID deviceId = measurement.getDeviceId();

            // Check if device exists in monitored devices - skip if not present
            Optional<MonitoredDevice> deviceOpt = monitoredDeviceRepository.findById(deviceId);
            if (deviceOpt.isEmpty()) {
                LOGGER.warn("[Instance {}] Device {} not found in monitored devices, skipping measurement",
                        instanceId, deviceId);
                return;
            }

            MonitoredDevice monitoredDevice = deviceOpt.get();
            Instant ts = measurement.getTimestamp();

            // Determine hour bucket (truncated to hour in configured timezone)
            ZonedDateTime zdt = ts.atZone(zoneId);
            ZonedDateTime hourStartZdt = zdt.truncatedTo(ChronoUnit.HOURS);
            Instant hourStart = hourStartZdt.toInstant();

            HourlyEnergy hourly = hourlyEnergyRepository
                    .findByDeviceIdAndHourStart(deviceId, hourStart)
                    .orElseGet(() -> {
                        HourlyEnergy he = new HourlyEnergy();
                        he.setDeviceId(deviceId);
                        he.setHourStart(hourStart);
                        he.setTotalEnergy(0.0);
                        return he;
                    });

            double previousEnergy = hourly.getTotalEnergy();
            double newEnergy = round(previousEnergy + measurement.getEnergy(), 4);
            hourly.setTotalEnergy(newEnergy);
            hourlyEnergyRepository.save(hourly);

            LOGGER.info("[Instance {}] Updated hourly total for device {} at {} to {} kWh",
                    instanceId, deviceId, hourStart, newEnergy);

            // Get device-specific max consumption threshold
            double maxConsumption = monitoredDevice.getMaxConsumption();
            String ownerUsername = monitoredDevice.getOwnerUsername();

            // Check for overconsumption - trigger if threshold is exceeded
            if (previousEnergy <= maxConsumption && newEnergy > maxConsumption) {
                sendOverconsumptionNotification(deviceId, hourStart, newEnergy, maxConsumption, ownerUsername);
            }
        } catch (Exception e) {
            LOGGER.error("[Instance {}] Failed to process message: {}", instanceId, json, e);
        }
    }

    private void sendOverconsumptionNotification(UUID deviceId, Instant hourStart, 
                                                  double totalEnergy, double maxConsumption,
                                                  String ownerUsername) {
        try {
            String notification = objectMapper.writeValueAsString(new OverconsumptionNotification(
                    deviceId.toString(),
                    hourStart.toString(),
                    totalEnergy,
                    maxConsumption,
                    ownerUsername,
                    "Device " + deviceId + " exceeded hourly consumption limit. " +
                            "Current: " + totalEnergy + " kWh, Limit: " + maxConsumption + " kWh"
            ));
            
            rabbitTemplate.convertAndSend(overconsumptionQueue, notification);
            LOGGER.warn("[Instance {}] OVERCONSUMPTION ALERT: Device {} (owner: {}) at hour {} - {} kWh (limit: {} kWh)",
                    instanceId, deviceId, ownerUsername, hourStart, totalEnergy, maxConsumption);
        } catch (Exception e) {
            LOGGER.error("[Instance {}] Failed to send overconsumption notification", instanceId, e);
        }
    }

    private double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    // Inner class for overconsumption notification payload
    public static class OverconsumptionNotification {
        private String deviceId;
        private String hourStart;
        private double currentConsumption;
        private double maxAllowed;
        private String ownerUsername;
        private String message;

        public OverconsumptionNotification() {}

        public OverconsumptionNotification(String deviceId, String hourStart, 
                                           double currentConsumption, double maxAllowed,
                                           String ownerUsername, String message) {
            this.deviceId = deviceId;
            this.hourStart = hourStart;
            this.currentConsumption = currentConsumption;
            this.maxAllowed = maxAllowed;
            this.ownerUsername = ownerUsername;
            this.message = message;
        }

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getHourStart() { return hourStart; }
        public void setHourStart(String hourStart) { this.hourStart = hourStart; }
        public double getCurrentConsumption() { return currentConsumption; }
        public void setCurrentConsumption(double currentConsumption) { this.currentConsumption = currentConsumption; }
        public double getMaxAllowed() { return maxAllowed; }
        public void setMaxAllowed(double maxAllowed) { this.maxAllowed = maxAllowed; }
        public String getOwnerUsername() { return ownerUsername; }
        public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
