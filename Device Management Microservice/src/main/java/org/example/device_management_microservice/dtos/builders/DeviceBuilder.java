package org.example.device_management_microservice.dtos.builders;


import org.example.device_management_microservice.dtos.DeviceDTO;
import org.example.device_management_microservice.dtos.DeviceDetailsDTO;
import org.example.device_management_microservice.entities.Device;
import org.example.device_management_microservice.entities.User;

public class DeviceBuilder {

    private DeviceBuilder() {
    }

    public static DeviceDTO toDeviceDTO(Device device) {
        return new DeviceDTO(device.getId(),
                device.getName(),
                device.getMaxConsumption(),
                device.getOwner().getUsername());
    }

    public static DeviceDetailsDTO toDeviceDetailsDTO(Device device) {
        return new DeviceDetailsDTO(device.getId(),
                device.getName(),
                device.getMaxConsumption(),
                device.getOwner().getUsername());
    }

    public static Device toEntity(DeviceDetailsDTO deviceDetailsDTO,
                                  User owner) {
        return new Device(deviceDetailsDTO.getName(),
                deviceDetailsDTO.getMax_consumption(),
                owner
                );
    }
}
