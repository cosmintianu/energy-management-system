package org.example.monitoring_microservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceDataRabbitConfig {

    @Value("${monitoring.queue}")
    private String queueName;

    @Value("${monitoring.overconsumption-queue}")
    private String overconsumptionQueueName;

    @Bean
    public Queue deviceDataQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue overconsumptionQueue() {
        return new Queue(overconsumptionQueueName, true);
    }
}
