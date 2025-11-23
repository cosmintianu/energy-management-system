package org.example.device_data_simulator.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${simulator.exchange}")
    private String exchangeName;

    @Value("${simulator.queue}")
    private String queueName;

    @Value("${simulator.routing-key}")
    private String routingKey;

    @Bean
    public TopicExchange deviceDataExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue deviceDataQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding deviceDataBinding(Queue deviceDataQueue, TopicExchange deviceDataExchange) {
        return BindingBuilder
                .bind(deviceDataQueue)
                .to(deviceDataExchange)
                .with(routingKey);
    }
}
