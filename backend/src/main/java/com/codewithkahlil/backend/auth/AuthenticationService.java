package com.codewithkahlil.backend.auth;

import com.codewithkahlil.backend.model.Role;
import com.codewithkahlil.backend.model.User;
import com.codewithkahlil.backend.repository.UserRepository;
import com.codewithkahlil.backend.service.JwtService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.naming.AuthenticationException;
import java.net.PasswordAuthentication;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repositpry ;
    private final PasswordEncoder passwordEncoder ;
    private final JwtService jwtService ;
    private final AuthenticationManager authenticationManager ;

    public AuthenticationResponse register(RegistreRequest request){
        assert passwordEncoder != null;
        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        repositpry.save(user);
         var jwtToken = jwtService.generateToken(user);

        return  AuthenticationResponse.builder()
                .token(jwtToken)
                .role(user.getRole())
                .build() ;
    }

    public AuthenticationResponse authenticate(AuthenticateRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword())
        );

        var user = repositpry.findUserByEmail(request.getEmail());
        var jwtToken = jwtService.generateToken(user);

        Role role = user.getRole() ;

        return  AuthenticationResponse.builder()
                .token(jwtToken)
                .role(role)
                .build() ;
    }
}
