package org.example.user_management_microservice.dtos.builders;

import org.example.user_management_microservice.dtos.UserDTO;
import org.example.user_management_microservice.dtos.UserDetailsDTO;
import org.example.user_management_microservice.entities.User;

public class UserBuilder {
    public UserBuilder() {
    }
    public static UserDTO toUserDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername());
    }

    public static UserDetailsDTO toUserDetailsDTO(User user) {
        return new UserDetailsDTO(user.getId(), user.getUsername(), user.getAddress(), user.getEmail());
    }

    public static User toEntity(UserDetailsDTO userDetailsDTO) {
        return new User(userDetailsDTO.getUsername(), userDetailsDTO.getEmail(), userDetailsDTO.getAddress());
    }
}
