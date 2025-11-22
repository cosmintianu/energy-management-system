package org.example.device_management_microservice.services;

import org.example.device_management_microservice.entities.User;
import org.example.device_management_microservice.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserSyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserSyncService.class);
    private final UserRepository userRepository;

    public UserSyncService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void syncUser(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            LOGGER.info("User already exists in Device DB: {}", username);
            return;
        }

        User user = new User();
        user.setUsername(username);

        userRepository.save(user);
        LOGGER.info("User synced to Device DB: username={}", username);
    }

    @Transactional
    public void deleteUser(String username) {
        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    userRepository.delete(user);
                    LOGGER.info("User deleted from Device DB: {}", username);
                });
    }
}
