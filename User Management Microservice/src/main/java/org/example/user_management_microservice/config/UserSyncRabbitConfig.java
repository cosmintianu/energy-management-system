package org.example.user_management_microservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class UserSyncRabbitConfig {
    public static final String SYNC_EXCHANGE = "sync.events.exchange";
    public static final String USER_SYNC_QUEUE = "sync.user.queue";
    public static final String USER_SYNC_ROUTING_KEY = "users";

    @Bean
    public DirectExchange syncExchange() {
        return new DirectExchange(SYNC_EXCHANGE, true, false);
    }

    @Bean
    public Queue syncQueue() {
        return new Queue(USER_SYNC_QUEUE, true);
    }

    @Bean
    public Binding syncBinding(Queue syncQueue, DirectExchange syncExchange) {
        return BindingBuilder
                .bind(syncQueue)
                .to(syncExchange)
                .with(USER_SYNC_ROUTING_KEY);
    }
}
