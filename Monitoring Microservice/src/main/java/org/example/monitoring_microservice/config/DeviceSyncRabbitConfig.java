package org.example.monitoring_microservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceSyncRabbitConfig {

    public static final String SYNC_EXCHANGE = "sync.events.exchange";
    public static final String DEVICE_SYNC_QUEUE = "sync.device.queue";
    public static final String DEVICE_SYNC_ROUTING_KEY = "devices";

    @Bean
    public DirectExchange syncExchange() {
        return new DirectExchange(SYNC_EXCHANGE, true, false);
    }

    @Bean
    public Queue syncQueue() {
        return new Queue(DEVICE_SYNC_QUEUE, true);
    }

    @Bean
    public Binding syncBinding(Queue syncQueue, DirectExchange syncExchange) {
        return BindingBuilder
                .bind(syncQueue)
                .to(syncExchange)
                .with(DEVICE_SYNC_ROUTING_KEY);
    }
}
