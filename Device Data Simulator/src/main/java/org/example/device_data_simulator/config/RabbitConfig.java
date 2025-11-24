package org.example.device_data_simulator.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${simulator.queue}")
    private String queueName;

    @Bean
    public Queue deviceDataQueue() {
        return new Queue(queueName, true);
    }

}
