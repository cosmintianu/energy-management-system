package org.example.device_data_simulator;

import org.example.device_data_simulator.service.DeviceDataProducerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeviceDataSimulatorApplication implements CommandLineRunner {

    private final DeviceDataProducerService deviceDataProducerService;

    public DeviceDataSimulatorApplication(DeviceDataProducerService deviceDataProducerService) {
        this.deviceDataProducerService = deviceDataProducerService;
    }

    public static void main(String[] args) {
        SpringApplication.run(DeviceDataSimulatorApplication.class, args);
    }

    @Override
    public void run(String... args) {
        deviceDataProducerService.runOnce();
        // After sending all messages, terminate the application
        System.exit(0);
    }
}
