package org.example.monitoring_microservice.repository;

import org.example.monitoring_microservice.entity.HourlyEnergy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HourlyEnergyRepository extends JpaRepository<HourlyEnergy, UUID> {

    Optional<HourlyEnergy> findByDeviceIdAndHourStart(UUID deviceId, Instant hourStart);

    List<HourlyEnergy> findByDeviceIdAndHourStartBetween(UUID deviceId, Instant start, Instant end);
}
