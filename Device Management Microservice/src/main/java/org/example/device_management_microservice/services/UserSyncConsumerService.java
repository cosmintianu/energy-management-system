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
            LOGGER.info("Received sync event: type={}, event={}, id={}",
                    event.getType(), event.getEvent(), event.getId());

            if ("USER".equalsIgnoreCase(event.getType())
                    && "CREATED".equalsIgnoreCase(event.getEvent())) {
                handleUserCreated(event.getId());
            } else if ("DEVICE".equalsIgnoreCase(event.getType())
                    && "CREATED".equalsIgnoreCase(event.getEvent())) {
                LOGGER.debug("Ignoring DEVICE.CREATED event (published by this service)");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to process sync event: {}", json, e);
        }
    }

    private void handleUserCreated(String username) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User(username);
            userRepository.save(user);
            System.out.println("Synced new user " +  username);
            LOGGER.info("Synced new user: {}", username);
        } else {
            LOGGER.debug("User {} already exists, skipping sync", username);
        }
    }

}
