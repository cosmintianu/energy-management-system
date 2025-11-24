package org.example.monitoring_microservice.controller;


import org.example.monitoring_microservice.dto.HourlyEnergyDTO;
import org.example.monitoring_microservice.service.MonitoringQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/monitoring")
public class MonitoringController {

    private final MonitoringQueryService queryService;

    public MonitoringController(MonitoringQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/device/{deviceId}/daily")
    public ResponseEntity<List<HourlyEnergyDTO>> getDailyConsumption(
            @PathVariable UUID deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<HourlyEnergyDTO> data = queryService.getHourlyEnergyForDay(deviceId, date);
        return ResponseEntity.ok(data);
    }
}
