package org.example.user_management_microservice.controllers;

import jakarta.validation.Valid;
import org.example.user_management_microservice.dtos.UserDTO;
import org.example.user_management_microservice.dtos.UserDetailsDTO;
import org.example.user_management_microservice.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<UserDetailsDTO> getUser(@PathVariable UUID id,
                                                  Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream().findFirst().map(auth -> auth.getAuthority()).orElse("");

        UserDetailsDTO user = userService.findUserById(id);

        if (role.equals("ROLE_CLIENT") && !user.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.findUserById(id));}

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<List<UserDetailsDTO>> getUsers() { return ResponseEntity.ok(userService.findAllUsers());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<UserDetailsDTO> updateUser(@PathVariable UUID id,
                                                     @Valid @RequestBody UserDetailsDTO userDetailsDTO,
                                                     Authentication authentication) {

        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("");

        UserDetailsDTO existingUser = userService.findUserById(id);

        if (role.equals("ROLE_CLIENT") && !existingUser.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // ID can't be changed
        userDetailsDTO.setUsername(existingUser.getUsername());

        UserDetailsDTO updated = userService.updateUser(id, userDetailsDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


}
