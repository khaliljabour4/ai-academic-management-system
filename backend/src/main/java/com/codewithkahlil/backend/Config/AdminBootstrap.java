package com.codewithkahlil.backend.Config;

import com.codewithkahlil.backend.model.Administrator;
import com.codewithkahlil.backend.model.Role;
import com.codewithkahlil.backend.model.User;
import com.codewithkahlil.backend.repository.AdministratorRepository;
import com.codewithkahlil.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Creates the very first administrator on startup, because /auth/register is now restricted to admins.
 * Runs only when ADMIN_EMAIL and ADMIN_PASSWORD are set and that account does not exist yet.
 */
@Configuration
public class AdminBootstrap {

    @Bean
    CommandLineRunner createFirstAdmin(
            UserRepository users,
            AdministratorRepository admins,
            PasswordEncoder encoder,
            @Value("${bootstrap.admin.email:}") String email,
            @Value("${bootstrap.admin.password:}") String password) {
        return args -> {
            if (email.isBlank() || password.isBlank()) return;
            if (users.findUserByEmail(email) != null) return;

            User user = users.save(User.builder()
                    .email(email)
                    .password(encoder.encode(password))
                    .role(Role.Administrator)
                    .build());
            admins.save(Administrator.builder().name("Admin").prenom("System").user(user).build());
            System.out.println("First administrator created: " + email);
        };
    }
}
