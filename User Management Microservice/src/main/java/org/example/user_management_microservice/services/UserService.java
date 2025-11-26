package org.example.user_management_microservice.services;

import org.example.user_management_microservice.dtos.UserDetailsDTO;
import org.example.user_management_microservice.dtos.builders.UserBuilder;
import org.example.user_management_microservice.entities.User;
import org.example.user_management_microservice.handlers.exceptions.model.ResourceNotFoundException;
import org.example.user_management_microservice.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserSyncPublisherService userSyncPublisher;

    @Autowired
    public UserService(UserRepository userRepository, UserSyncPublisherService userSyncPublisher) {
        this.userRepository = userRepository;
        this.userSyncPublisher = userSyncPublisher;
    }

    public UUID createUser(UserDetailsDTO userDetailsDTO) {
        User user = UserBuilder.toEntity(userDetailsDTO);
        user = userRepository.save(user);
        LOGGER.debug("User with id {} was inserted in db", user.getId());

        userSyncPublisher.publishUserCreated(user.getUsername());

        return user.getId();
    }

    public UserDetailsDTO findUserById(UUID id) {
        Optional<User> userOptional = userRepository.findById(id);
        if(!userOptional.isPresent()) {
            LOGGER.error("User with id {} not found in db.", id);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }

        return UserBuilder.toUserDetailsDTO(userOptional.get());
    }

    public List<UserDetailsDTO> findAllUsers() {
        List<User> users = userRepository.findAll();

        if(users.isEmpty()) {
            LOGGER.error("There are no users in the db.");
            throw new ResourceNotFoundException("No users found in db.");
        }

        return  users.stream()
                .map(UserBuilder::toUserDetailsDTO)
                .collect(Collectors.toList());
    }

    public UserDetailsDTO updateUser(UUID id, UserDetailsDTO userDetailsDTO) {
        Optional<User> userOptional = userRepository.findById(id);
        if(!userOptional.isPresent()) {
            LOGGER.error("User with id {} not found in db.", id);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }

        User existingUser = userOptional.get();

        existingUser.setUsername(userDetailsDTO.getUsername());
        existingUser.setEmail(userDetailsDTO.getEmail());
        existingUser.setAddress(userDetailsDTO.getAddress());

        existingUser = userRepository.save(existingUser);
        LOGGER.debug("User with id {} was updated in db", id);
        return UserBuilder.toUserDetailsDTO(existingUser);
    }

    public void deleteUser(UUID id) {
        Optional<User> userOptional = userRepository.findById(id);
        if(userOptional.isEmpty()) {
            LOGGER.error("User with id {} not found in db.", id);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }

        userRepository.delete(userOptional.get());
        LOGGER.debug("User with id {} was deleted in db", id);

        userSyncPublisher.publishUserDeleted(userOptional.get().getUsername());
    }

}
