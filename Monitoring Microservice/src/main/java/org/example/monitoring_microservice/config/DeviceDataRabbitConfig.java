package org.example.monitoring_microservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceDataRabbitConfig {

    @Value("${monitoring.queue}")
    private String queueName;

    @Bean
    public Queue deviceDataQueue() {
        return new Queue(queueName, true);
    }

}
