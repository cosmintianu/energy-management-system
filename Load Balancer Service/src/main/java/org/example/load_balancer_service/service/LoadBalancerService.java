package org.example.load_balancer_service.service;

import org.example.load_balancer_service.dto.DeviceMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Load Balancer Service that distributes device measurements to monitoring replicas
 * using a hash-based algorithm (consistent hashing based on deviceId).
 * 
 * This ensures that all measurements from the same device go to the same replica,
 * which is important for maintaining consistent state per device.
 */
@Service
public class LoadBalancerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancerService.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final List<String> outputQueues;
    private final int replicaCount;

    public LoadBalancerService(RabbitTemplate rabbitTemplate, 
                               ObjectMapper objectMapper,
                               @Value("${loadbalancer.output-queues}") String outputQueuesString,
                               @Value("${loadbalancer.replica-count}") int replicaCount) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.outputQueues = Arrays.asList(outputQueuesString.split(","));
        this.replicaCount = replicaCount;
    }

    @RabbitListener(queues = "${loadbalancer.input-queue}")
    public void receiveAndDistribute(String json) {
        try {
            DeviceMeasurement measurement = objectMapper.readValue(json, DeviceMeasurement.class);
            
            // Hash-based routing: use deviceId hash to consistently route to same replica
            String targetQueue = selectQueue(measurement.getDeviceId());
            
            rabbitTemplate.convertAndSend(targetQueue, json);
            
            LOGGER.info("Routed measurement for device {} to queue {}", 
                    measurement.getDeviceId(), targetQueue);
            
        } catch (Exception e) {
            LOGGER.error("Failed to process and route message: {}", json, e);
        }
    }


    private String selectQueue(UUID deviceId) {
        // Use absolute value of hash and modulo to get consistent queue assignment
        int hash = Math.abs(deviceId.hashCode());
        int index = hash % replicaCount;
        return outputQueues.get(index);
    }
}
