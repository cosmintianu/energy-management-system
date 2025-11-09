package org.example.device_management_microservice.services;



import org.example.device_management_microservice.dtos.DeviceDTO;
import org.example.device_management_microservice.dtos.DeviceDetailsDTO;
import org.example.device_management_microservice.dtos.builders.DeviceBuilder;
import org.example.device_management_microservice.entities.Device;
import org.example.device_management_microservice.handlers.exceptions.model.ResourceNotFoundException;
import org.example.device_management_microservice.repositories.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Autowired
    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public UUID createDevice(DeviceDetailsDTO deviceDetailsDTO) {
        Device device = DeviceBuilder.toEntity(deviceDetailsDTO);
        device =  deviceRepository.save(device);
        LOGGER.debug("Device with id {} was inserted in db", device.getId());
        return device.getId();
    }

    public DeviceDetailsDTO findDeviceById(UUID id) {
        Optional<Device> optionalDevice = deviceRepository.findById(id);
        if (optionalDevice.isEmpty()) {
            LOGGER.error("Device with id {} not found in db.", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }

        return DeviceBuilder.toDeviceDetailsDTO(optionalDevice.get());
    }

    public List<DeviceDTO> findAllDevices() {
        List<Device> devices = deviceRepository.findAll();

        if(devices.isEmpty()) {
            LOGGER.error("There are no devices in the db.");
            throw new ResourceNotFoundException("No devices found in db.");
        }

        return devices.stream().map(DeviceBuilder::toDeviceDTO).collect(Collectors.toList());
    }

    public DeviceDetailsDTO updateDevice(UUID id, DeviceDetailsDTO deviceDetailsDTO) {
        Optional<Device> optionalDevice = deviceRepository.findById(id);
        if (optionalDevice.isEmpty()) {
            LOGGER.error("Device with id {} not found in db.", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }

        Device existingDevice = optionalDevice.get();
        existingDevice.setName(deviceDetailsDTO.getName());
        existingDevice.setOwnerUsername(deviceDetailsDTO.getOwnerUsername());
        existingDevice.setMax_consumption(deviceDetailsDTO.getMax_consumption());

        existingDevice =  deviceRepository.save(existingDevice);
        LOGGER.debug("Device with id {} was updated in db", id);
        return DeviceBuilder.toDeviceDetailsDTO(existingDevice);
    }

    public void deleteDevice(UUID id) {
        Optional<Device> optionalDevice = deviceRepository.findById(id);
        if (optionalDevice.isEmpty()) {
            LOGGER.error("Device with id {} not found in db.", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }

        deviceRepository.delete(optionalDevice.get());
        LOGGER.debug("Device with id {} was deleted in db", id);
    }

    public List<DeviceDTO> findDevicesByOwner(String ownerUsername) {
        List<Device> devices = deviceRepository.findAll();deviceRepository.findByOwnerUsername(ownerUsername);

        return devices.stream().map(DeviceBuilder::toDeviceDTO).collect(Collectors.toList());
    }
}
