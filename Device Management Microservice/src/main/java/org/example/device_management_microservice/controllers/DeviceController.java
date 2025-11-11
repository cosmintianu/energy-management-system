package org.example.device_management_microservice.controllers;

import jakarta.validation.Valid;
import org.example.device_management_microservice.dtos.DeviceDTO;
import org.example.device_management_microservice.dtos.DeviceDetailsDTO;
import org.example.device_management_microservice.services.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
@Validated
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<Void> createDevice(
            @Valid @RequestBody DeviceDetailsDTO deviceDetailsDTO,
            Authentication authentication) {

        // Set owner to current user for CLIENT, allow override for ADMIN
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("");

        // CLIENT can only create devices for themselves
        if (role.equals("ROLE_CLIENT")) {
            deviceDetailsDTO.setOwnerUsername(username);
        }

        UUID id = deviceService.createDevice(deviceDetailsDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<DeviceDetailsDTO> getDevice(
            @PathVariable UUID id,
            Authentication authentication) {

        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("");

        DeviceDetailsDTO device = deviceService.findDeviceById(id);

        // CLIENT can only view their own devices
        if (role.equals("ROLE_CLIENT") && !device.getOwnerUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(device);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<List<DeviceDetailsDTO>> getDevices(Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("");

        List<DeviceDetailsDTO> devices;

        if (role.equals("ROLE_ADMIN")) {
            // Admin sees all devices
            devices = deviceService.findAllDevices();
        } else {
            // Client sees only their devices
            devices = deviceService.findDevicesByOwner(username);
        }

        return ResponseEntity.ok(devices);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<DeviceDetailsDTO> updateDevice(
            @PathVariable UUID id,
            @Valid @RequestBody DeviceDetailsDTO deviceDetailsDTO,
            Authentication authentication) {

        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("");

        DeviceDetailsDTO existingDevice = deviceService.findDeviceById(id);

        // CLIENT can only update their own devices
        if (role.equals("ROLE_CLIENT") && !existingDevice.getOwnerUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // CLIENT cannot change device owner
        if (role.equals("ROLE_CLIENT")) {
            deviceDetailsDTO.setOwnerUsername(existingDevice.getOwnerUsername());
        }

        DeviceDetailsDTO updated = deviceService.updateDevice(id, deviceDetailsDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<Void> deleteDevice(
            @PathVariable UUID id,
            Authentication authentication) {

        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("");

        DeviceDetailsDTO device = deviceService.findDeviceById(id);

        // CLIENT can only delete their own devices
        if (role.equals("ROLE_CLIENT") && !device.getOwnerUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}
