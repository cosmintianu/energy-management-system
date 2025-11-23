package org.example.monitoring_microservice.service;

import org.example.monitoring_microservice.dto.DeviceMeasurement;
import org.example.monitoring_microservice.entity.HourlyEnergy;
import org.example.monitoring_microservice.repository.HourlyEnergyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class MonitoringService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringService.class);

    private final ObjectMapper objectMapper;
    private final HourlyEnergyRepository repository;
    private final ZoneId zoneId;

    public MonitoringService(ObjectMapper objectMapper,
                             HourlyEnergyRepository repository,
                             @Value("${monitoring.timezone}") String timezone) {
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.zoneId = ZoneId.of(timezone);
    }

    @RabbitListener(queues = "${monitoring.queue}")
    @Transactional
    public void receiveMeasurement(String json) {
        try {
            DeviceMeasurement measurement =
                    objectMapper.readValue(json, DeviceMeasurement.class);

            LOGGER.info("Received measurement: {}", measurement);

            UUID deviceId = measurement.getDeviceId();
            Instant ts = measurement.getTimestamp();

            // Determine hour bucket (truncated to hour in configured timezone)
            ZonedDateTime zdt = ts.atZone(zoneId);
            ZonedDateTime hourStartZdt = zdt.truncatedTo(ChronoUnit.HOURS);
            Instant hourStart = hourStartZdt.toInstant();

            HourlyEnergy hourly = repository
                    .findByDeviceIdAndHourStart(deviceId, hourStart)
                    .orElseGet(() -> {
                        HourlyEnergy he = new HourlyEnergy();
                        he.setDeviceId(deviceId);
                        he.setHourStart(hourStart);
                        he.setTotalEnergy(0.0);
                        return he;
                    });

            hourly.setTotalEnergy(round(hourly.getTotalEnergy() + measurement.getEnergy(),4));
            repository.save(hourly);

            LOGGER.info("Updated hourly total for device {} at {} to {} kWh",
                    deviceId, hourStart, hourly.getTotalEnergy());
        } catch (Exception e) {
            LOGGER.error("Failed to process message: {}", json, e);
            // you can choose to rethrow to trigger retry / DLQ depending on container config
        }
    }

    private double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }
}
