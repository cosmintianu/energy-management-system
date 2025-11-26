package org.example.user_management_microservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static org.example.user_management_microservice.config.UserSyncRabbitConfig.SYNC_EXCHANGE;
import static org.example.user_management_microservice.config.UserSyncRabbitConfig.USER_SYNC_ROUTING_KEY;

@Service
public class UserSyncPublisherService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public UserSyncPublisherService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishUserCreated(String username) {
        SyncEvent event = new SyncEvent("USER", "CREATED", username);
        try {
            String json = objectMapper.writeValueAsString(event);

            System.out.println("User created sync event published " + username);
            rabbitTemplate.convertAndSend(SYNC_EXCHANGE, USER_SYNC_ROUTING_KEY, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publishUserDeleted(String username) {
        SyncEvent event = new SyncEvent("USER", "DELETED", username);
        try {
            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(SYNC_EXCHANGE, USER_SYNC_ROUTING_KEY, json);
            System.out.println("User deleted sync event published " + username);
        } catch (Exception e) {
            System.err.println("Failed to publish USER.DELETED event " + e.toString());
        }
    }

    public static class SyncEvent {
        private String type;
        private String event;
        private String id; // username for users

        public SyncEvent() {}
        public SyncEvent(String type, String event, String id) {
            this.type = type;
            this.event = event;
            this.id = id;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }
}
