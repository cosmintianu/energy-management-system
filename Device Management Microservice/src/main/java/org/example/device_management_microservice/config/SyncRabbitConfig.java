package org.example.device_management_microservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SyncRabbitConfig {
    public static final String SYNC_EXCHANGE = "sync.events.exchange";
    public static final String SYNC_QUEUE = "sync.events.queue";
    public static final String SYNC_ROUTING_KEY = "sync.events";

    @Bean
    public TopicExchange syncExchange() {
        return new TopicExchange(SYNC_EXCHANGE, true, false);
    }

    @Bean
    public Queue syncQueue() {
        return new Queue(SYNC_QUEUE, true);
    }

    @Bean
    public Binding syncBinding(Queue syncQueue, TopicExchange syncExchange) {
        return BindingBuilder
                .bind(syncQueue)
                .to(syncExchange)
                .with(SYNC_ROUTING_KEY);
    }
}