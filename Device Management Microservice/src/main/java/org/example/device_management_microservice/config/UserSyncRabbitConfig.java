package org.example.device_management_microservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserSyncRabbitConfig {
    public static final String SYNC_EXCHANGE = "sync.events.exchange";
    public static final String USER_SYNC_QUEUE = "sync.user.queue";
    public static final String USER_SYNC_ROUTING_KEY = "users";

    @Bean
    public DirectExchange syncExchangeUser() {
        return new DirectExchange(SYNC_EXCHANGE, true, false);
    }

    @Bean
    public Queue syncQueueUser() {
        return new Queue(USER_SYNC_QUEUE, true);
    }

    @Bean
    public Binding syncBindingUser(Queue syncQueueUser, DirectExchange syncExchangeUser) {
        return BindingBuilder
                .bind(syncQueueUser)
                .to(syncExchangeUser)
                .with(USER_SYNC_ROUTING_KEY);
    }
}