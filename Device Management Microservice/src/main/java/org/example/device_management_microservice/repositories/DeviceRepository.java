package org.example.device_management_microservice.repositories;

import org.example.device_management_microservice.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    List<Device> findByName(String name);

    /**
     * Query to get all devices of a user
     */
//    @Query(value = "SELECT d " +
//            "FROM Device d " +
//            "WHERE d.user_id = :id ")
//    Optional<Device> findDevicesByUserId(@Param("user_id") UUID userId);

}
