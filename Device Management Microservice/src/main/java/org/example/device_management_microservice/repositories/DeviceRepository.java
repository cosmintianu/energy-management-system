package org.example.device_management_microservice.repositories;

import org.example.device_management_microservice.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    /**
     * Example: JPA generate query by existing field
     */
    List<Device> findByName(String name);

    /**
     * Example: Custom query
     */
//    @Query(value = "SELECT p " +
//            "FROM Device p " +
//            "WHERE p.name = :name " +
//            "AND p.age >= 60  ")
//    Optional<Device> findSeniorsByName(@Param("name") String name);

}
