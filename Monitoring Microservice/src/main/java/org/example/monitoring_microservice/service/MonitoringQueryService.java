package org.example.monitoring_microservice.service;


import org.example.monitoring_microservice.dto.HourlyEnergyDTO;
import org.example.monitoring_microservice.entity.HourlyEnergy;
import org.example.monitoring_microservice.repository.HourlyEnergyRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonitoringQueryService {

    private final HourlyEnergyRepository hourlyEnergyRepository;

    public MonitoringQueryService(HourlyEnergyRepository hourlyEnergyRepository) {
        this.hourlyEnergyRepository = hourlyEnergyRepository;
    }

    public List<HourlyEnergyDTO> getHourlyEnergyForDay(UUID deviceId, LocalDate date) {
        ZoneId zoneId = ZoneId.of("UTC"); // or your monitoring.timezone
        ZonedDateTime startOfDay = date.atStartOfDay(zoneId);
        ZonedDateTime endOfDay = startOfDay.plusDays(1);

        Instant start = startOfDay.toInstant();
        Instant end = endOfDay.toInstant();

        List<HourlyEnergy> records = hourlyEnergyRepository
                .findByDeviceIdAndHourStartBetween(deviceId, start, end);

        // Map to hour -> total
        Map<Integer, Double> hourMap = records.stream()
                .collect(Collectors.toMap(
                        he -> ZonedDateTime.ofInstant(he.getHourStart(), zoneId).getHour(),
                        HourlyEnergy::getTotalEnergy,
                        (a, b) -> a + b // merge if duplicate (shouldn't happen)
                ));

        // Build array for hours 0-23, fill missing with 0.0
        List<HourlyEnergyDTO> result = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            result.add(new HourlyEnergyDTO(hour, hourMap.getOrDefault(hour, 0.0)));
        }

        return result;
    }
}
