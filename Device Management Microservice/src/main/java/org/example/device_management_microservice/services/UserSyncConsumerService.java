package org.example.device_management_microservice.services;



import org.example.device_management_microservice.dtos.SyncEvent;
import org.example.device_management_microservice.entities.User;
import org.example.device_management_microservice.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.example.device_management_microservice.config.UserSyncRabbitConfig.USER_SYNC_QUEUE;

@Service
public class UserSyncConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSyncConsumerService.class);

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public UserSyncConsumerService(ObjectMapper objectMapper,
                                   UserRepository userRepository) {
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = USER_SYNC_QUEUE)
    @Transactional
    public void handleSyncEvent(String json) {
        try {
            SyncEvent event = objectMapper.readValue(json, SyncEvent.class);

            if ("USER".equalsIgnoreCase(event.getType())) {
                if ("CREATED".equalsIgnoreCase(event.getEvent())) {
                    handleUserCreated(event.getId());
                } else if ("DELETED".equalsIgnoreCase(event.getEvent())) {
                    handleUserDeleted(event.getId());
                }
            } else if ("DEVICE".equalsIgnoreCase(event.getType())
                    && "CREATED".equalsIgnoreCase(event.getEvent())) {
                LOGGER.debug("Ignoring DEVICE.CREATED event from self");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to process sync event: {}", json, e);
        }
    }

    private void handleUserDeleted(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            userRepository.delete(user);

            System.out.println("User deleted sync event consumed " + username);

        });
    }

    private void handleUserCreated(String username) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User(username);
            userRepository.save(user);

            System.out.println("User created sync event consumed " + username);
        } else {
            LOGGER.debug("User {} already exists, skipping sync", username);
        }
    }

}
