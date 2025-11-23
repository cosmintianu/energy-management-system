package org.example.device_data_simulator;

import org.example.device_data_simulator.service.SimulatorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeviceDataSimulatorApplication implements CommandLineRunner {

    private final SimulatorService simulatorService;

    public DeviceDataSimulatorApplication(SimulatorService simulatorService) {
        this.simulatorService = simulatorService;
    }

    public static void main(String[] args) {
        SpringApplication.run(DeviceDataSimulatorApplication.class, args);
    }

    @Override
    public void run(String... args) {
        simulatorService.runOnce();
        // After sending all messages, terminate the application
        System.exit(0);
    }
}
