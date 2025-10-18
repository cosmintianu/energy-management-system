package org.example.device_management_microservice.dtos.builders;


import org.example.device_management_microservice.dtos.DeviceDTO;
import org.example.device_management_microservice.dtos.DeviceDetailsDTO;
import org.example.device_management_microservice.entities.Device;

public class DeviceBuilder {

    private DeviceBuilder() {
    }

    public static DeviceDTO toDeviceDTO(Device device) {
        return new DeviceDTO(device.getId(), device.getName(), device.getMax_consumption());
    }

    public static DeviceDetailsDTO toDeviceDetailsDTO(Device device) {
        return new DeviceDetailsDTO(device.getId(), device.getName(), device.getMax_consumption());
    }

    public static Device toEntity(DeviceDetailsDTO personDetailsDTO) {
        return new Device(personDetailsDTO.getName(),
                personDetailsDTO.getMax_consumption());
    }
}
