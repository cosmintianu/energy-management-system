package org.example.device_management_microservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceSyncRabbitConfig {
    public static final String SYNC_EXCHANGE = "sync.events.exchange";
    public static final String DEVICE_SYNC_QUEUE = "sync.device.queue";
    public static final String DEVICE_SYNC_ROUTING_KEY = "devices";

    @Bean
    public DirectExchange syncExchangeDevice() {
        return new DirectExchange(SYNC_EXCHANGE, true, false);
    }

    @Bean
    public Queue syncQueueDevice() {
        return new Queue(DEVICE_SYNC_QUEUE, true);
    }

    @Bean
    public Binding syncBindingDevice(Queue syncQueueDevice, DirectExchange syncExchangeDevice) {
        return BindingBuilder
                .bind(syncQueueDevice)
                .to(syncExchangeDevice)
                .with(DEVICE_SYNC_ROUTING_KEY);
    }
}