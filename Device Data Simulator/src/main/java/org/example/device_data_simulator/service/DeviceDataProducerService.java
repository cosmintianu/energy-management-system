package org.example.device_data_simulator.service;


import org.example.device_data_simulator.model.DeviceMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;



@Service
public class DeviceDataProducerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceDataProducerService.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${simulator.queue}")
    private String queueName;

    @Value("${simulator.device-ids}")
    private String deviceIdsString;

    @Value("${simulator.readings-count}")
    private int readingsCount;

    @Value("${simulator.interval-minutes}")
    private int intervalMinutes;

    @Value("${simulator.timezone}")
    private String timezone;

    public DeviceDataProducerService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void runOnce() {
        List<UUID> deviceIds = Arrays.stream(deviceIdsString.split(","))
                .map(String::trim)
                .map(UUID::fromString)
                .collect(Collectors.toList());

        Instant now = Instant.now();
        ZoneId zoneId = ZoneId.of(timezone);

        for (UUID deviceId : deviceIds) {
            LOGGER.info("Generating data for device: {}", deviceId);
            for (int i = readingsCount - 1; i >= 0; i--) {
                Instant endTime = now.minusSeconds((long) i * intervalMinutes * 60L);
                double energy = generateEnergyForInterval(endTime, zoneId);

                DeviceMeasurement measurement =
                        new DeviceMeasurement(deviceId, endTime, energy);

                try {
                    String json = objectMapper.writeValueAsString(measurement);
                    rabbitTemplate.convertAndSend(queueName, json);
                    LOGGER.info("Sent measurement JSON: {}", json);
                } catch (Exception e) {
                    LOGGER.error("Failed to serialize measurement", e);
                }
            }
        }
    }

    private double generateEnergyForInterval(Instant end, ZoneId zone) {
        ZonedDateTime zdt = end.atZone(zone);
        int hour = zdt.getHour();

        double base;
        if (hour < 6) {
            base = 0.08;
        } else if (hour < 9) {
            base = 0.22;
        } else if (hour < 17) {
            base = 0.16;
        } else if (hour < 23) {
            base = 0.40;
        } else {
            base = 0.12;
        }

        double noise = ThreadLocalRandom.current().nextDouble(-0.03, 0.03);
        double energy = Math.max(0.01, base + noise);
        return round(energy, 4);
    }

    private double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }
}
