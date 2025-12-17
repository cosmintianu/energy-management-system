package org.example.load_balancer_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class RabbitConfig {

    @Value("${loadbalancer.input-queue}")
    private String inputQueue;

    @Value("${loadbalancer.output-queues}")
    private String outputQueuesString;

    @Bean
    public Queue inputQueue() {
        return new Queue(inputQueue, true);
    }

    @Bean
    public Queue outputQueue1() {
        return new Queue(getOutputQueues().get(0), true);
    }

    @Bean
    public Queue outputQueue2() {
        return new Queue(getOutputQueues().get(1), true);
    }

    @Bean
    public Queue outputQueue3() {
        return new Queue(getOutputQueues().get(2), true);
    }

    public List<String> getOutputQueues() {
        return Arrays.asList(outputQueuesString.split(","));
    }
}
