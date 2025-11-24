package org.example.device_management_microservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.device_management_microservice.dtos.DeviceDetailsDTO;
import org.example.device_management_microservice.dtos.builders.DeviceBuilder;
import org.example.device_management_microservice.entities.Device;
import org.example.device_management_microservice.entities.User;
import org.example.device_management_microservice.handlers.exceptions.model.ResourceNotFoundException;
import org.example.device_management_microservice.repositories.DeviceRepository;
import org.example.device_management_microservice.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final DeviceSyncPublisher deviceSyncPublisher;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository,
                         UserRepository userRepository,
                         DeviceSyncPublisher deviceSyncPublisher) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.deviceSyncPublisher = deviceSyncPublisher;
    }

    @Transactional
    public UUID createDevice(DeviceDetailsDTO deviceDetailsDTO) {
        Optional<User> ownerOptional = userRepository.findByUsername(deviceDetailsDTO.getOwnerUsername());

        if (ownerOptional.isEmpty()) {
            LOGGER.error("User with username {} not found in db.", deviceDetailsDTO.getOwnerUsername());
            throw new ResourceNotFoundException("User with username: " + deviceDetailsDTO.getOwnerUsername());
        }

        User owner = ownerOptional.get();

        Device device = DeviceBuilder.toEntity(deviceDetailsDTO, owner);
        device = deviceRepository.save(device);
        LOGGER.debug("Device with id {} was inserted in db", device.getId());

        // Publish sync event
        deviceSyncPublisher.publishDeviceCreated(device.getId().toString());

        return device.getId();
    }

    public DeviceDetailsDTO findDeviceById(UUID id) {
        Optional<Device> optionalDevice = deviceRepository.findById(id);
        if (optionalDevice.isEmpty()) {
            LOGGER.error("Device with id {} not found in db.", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName()
                    + " with id: " + id);
        }

        return DeviceBuilder.toDeviceDetailsDTO(optionalDevice.get());
    }

    public List<DeviceDetailsDTO> findAllDevices() {
        List<Device> devices = deviceRepository.findAll();

        if(devices.isEmpty()) {
            LOGGER.error("There are no devices in the db.");
            throw new ResourceNotFoundException("No devices found in db.");
        }

        return devices.stream()
                .map(DeviceBuilder::toDeviceDetailsDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeviceDetailsDTO updateDevice(UUID id, DeviceDetailsDTO deviceDetailsDTO) {
        Optional<Device> optionalDevice = deviceRepository.findById(id);
        if (optionalDevice.isEmpty()) {
            LOGGER.error("Device with id {} not found in db.", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }

        Device existingDevice = optionalDevice.get();

        if (!existingDevice.getOwner().getUsername().equals(deviceDetailsDTO.getOwnerUsername())) {
            Optional<User> newOwnerOpt = userRepository.findByUsername(deviceDetailsDTO.getOwnerUsername());

            if (newOwnerOpt.isEmpty()) {
                LOGGER.error("User with username {} not found in db.", deviceDetailsDTO.getOwnerUsername());
                throw new ResourceNotFoundException("User with username: " + deviceDetailsDTO.getOwnerUsername());
            }

            existingDevice.setOwner(newOwnerOpt.get());
        }

        existingDevice.setName(deviceDetailsDTO.getName());
        existingDevice.setMaxConsumption(deviceDetailsDTO.getMax_consumption());

        existingDevice = deviceRepository.save(existingDevice);
        LOGGER.debug("Device with id {} was updated in db", id);

        return DeviceBuilder.toDeviceDetailsDTO(existingDevice);
    }

    @Transactional
    public void deleteDevice(UUID id) {
        Optional<Device> optionalDevice = deviceRepository.findById(id);
        if (optionalDevice.isEmpty()) {
            LOGGER.error("Device with id {} not found in db.", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }

        deviceRepository.delete(optionalDevice.get());
        LOGGER.debug("Device with id {} was deleted in db", id);
    }

    public List<DeviceDetailsDTO> findDevicesByOwner(String ownerUsername) {
        Optional<User> ownerOpt = userRepository.findByUsername(ownerUsername);

        if (ownerOpt.isEmpty()) {
            LOGGER.warn("User with username {} not found in db. Returning empty list.", ownerUsername);
            return List.of();
        }

        List<Device> devices = deviceRepository.findByOwnerUsername(ownerOpt.get().getUsername());

        return devices.stream()
                .map(DeviceBuilder::toDeviceDetailsDTO)
                .collect(Collectors.toList());
    }
}
