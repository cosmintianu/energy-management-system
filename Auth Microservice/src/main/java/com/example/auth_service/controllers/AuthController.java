    package com.example.auth_service.controllers;


    import com.example.auth_service.entities.AuthUser;
    import com.example.auth_service.entities.Role;
    import com.example.auth_service.repositories.AuthUserRepository;
    import com.example.auth_service.services.PasswordService;
    import com.example.auth_service.services.TokenService;
    import jakarta.validation.Valid;
    import org.springframework.http.ResponseEntity;
    import org.springframework.validation.annotation.Validated;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

    import java.net.URI;
    import java.util.Map;

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
    }
