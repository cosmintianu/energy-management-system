    package com.example.auth_service.controllers;


    import com.example.auth_service.entities.AuthUser;
    import com.example.auth_service.entities.Role;
    import com.example.auth_service.repositories.AuthUserRepository;
    import com.example.auth_service.services.PasswordService;
    import com.example.auth_service.services.TokenService;
    import jakarta.validation.Valid;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.validation.annotation.Validated;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

    import java.net.URI;
    import java.util.List;
    import java.util.Map;
    import java.util.stream.Collectors;

    @RestController
    @RequestMapping("/auth")
    @Validated
    public class AuthController {

        private final AuthUserRepository users;
        private final PasswordService passwords;
        private final TokenService tokens;

        public AuthController(AuthUserRepository users, PasswordService passwords, TokenService tokens) {
            this.users = users;
            this.passwords = passwords;
            this.tokens = tokens;
        }

        @PostMapping("/register")
        public ResponseEntity<Map<String, String>> register(@Valid @RequestBody AuthUser authUser) {
            if (users.existsByUsername(authUser.getUsername())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
            }

            authUser.setPassword(passwords.hash(authUser.getPassword()));
            authUser.setRole(Role.CLIENT);

            AuthUser saved = users.save(authUser);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest().path("/users/{username}")
                    .buildAndExpand(saved.getUsername())
                    .toUri();

            return ResponseEntity.created(location)
                    .body(Map.of("status", "created",
                            "user", saved.getUsername(),
                            "role", saved.getRole().name()));
        }

        @PostMapping("/login")
        public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AuthUser authUser) {
            AuthUser u = users.findByUsername(authUser.getUsername())
                    .orElseThrow(() -> new RuntimeException("Invalid username or password"));

            if (!passwords.matches(authUser.getPassword(), u.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
            }

            String token = tokens.generate(u);
            return ResponseEntity.ok(Map.of("token", token,
                                            "user", u.getUsername(),
                                            "role", u.getRole().name()));
        }

        // Get all auth users (for admin to see roles)
        @GetMapping("/users")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<List<Map<String, String>>> getAllAuthUsers() {
            List<Map<String, String>> userList = users.findAll().stream()
                    .map(user -> Map.of(
                            "username", user.getUsername(),
                            "role", user.getRole().name()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userList);
        }

        // Update user role
        @PutMapping("/users/{username}/role")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Map<String, String>> updateUserRole(
                @PathVariable String username,
                @RequestBody Map<String, String> roleUpdate) {

            AuthUser user = users.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String newRole = roleUpdate.get("role");
            if (newRole == null || (!newRole.equals("ADMIN") && !newRole.equals("CLIENT"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
            }

            user.setRole(Role.valueOf(newRole));
            users.save(user);

            return ResponseEntity.ok(Map.of(
                    "status", "updated",
                    "username", user.getUsername(),
                    "role", user.getRole().name()
            ));
        }

        // Delete user from auth
        @DeleteMapping("/users/{username}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> deleteAuthUser(@PathVariable String username) {
            AuthUser user = users.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            users.delete(user);
            return ResponseEntity.noContent().build();
        }

    }
