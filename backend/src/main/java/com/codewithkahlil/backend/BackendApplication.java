package com.codewithkahlil.backend;

import com.codewithkahlil.backend.auth.AuthenticationService;
import com.codewithkahlil.backend.auth.RegistreRequest;
import com.codewithkahlil.backend.model.Role;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
