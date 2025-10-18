package org.example.user_management_microservice.controllers;

import jakarta.validation.Valid;
import org.example.user_management_microservice.dtos.UserDTO;
import org.example.user_management_microservice.dtos.UserDetailsDTO;
import org.example.user_management_microservice.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserDetailsDTO userDetailsDTO) {
        UUID id = userService.createUser(userDetailsDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build().toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) { return ResponseEntity.ok(userService.findUserById(id));}

    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers() { return ResponseEntity.ok(userService.findAllUsers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        UserDTO updated = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


}
