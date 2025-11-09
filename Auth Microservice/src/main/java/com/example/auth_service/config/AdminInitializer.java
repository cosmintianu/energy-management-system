package com.example.auth_service.config;

import com.example.auth_service.entities.AuthUser;
import com.example.auth_service.entities.Role;
import com.example.auth_service.repositories.AuthUserRepository;
import com.example.auth_service.services.PasswordService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final AuthUserRepository users;
    private final PasswordService passwords;

    public AdminInitializer(AuthUserRepository users, PasswordService passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Only create admin if no admin exists
        if (!users.existsByUsername("admin")) {
            AuthUser admin = new AuthUser();
            admin.setUsername("admin");
            admin.setPassword(passwords.hash("admin123"));  // Change this password!
            admin.setRole(Role.ADMIN);
            users.save(admin);

            System.out.println("======================================");
            System.out.println("Default admin user created!");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
            System.out.println("PLEASE CHANGE THIS PASSWORD!");
            System.out.println("======================================");
        }
    }
}
