package org.example.device_management_microservice.controllers;

import org.example.device_management_microservice.services.UserSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/devices/sync/users")
public class UserSyncController {

    private final UserSyncService userSyncService;

    public UserSyncController(UserSyncService userSyncService) {
        this.userSyncService = userSyncService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<Void> syncUser(@RequestBody Map<String, String> userData) {
        String username = userData.get("username");

        userSyncService.syncUser(username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userSyncService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }
}
