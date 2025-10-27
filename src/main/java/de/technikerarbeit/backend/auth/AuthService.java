package de.technikerarbeit.backend.auth;

import de.technikerarbeit.backend.auth.dto.AuthResponse;
import de.technikerarbeit.backend.auth.dto.LoginRequest;
import de.technikerarbeit.backend.auth.dto.RegisterRequest;
import de.technikerarbeit.backend.user.User;
import de.technikerarbeit.backend.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwt) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Register called for email={}", request.email());

        users.findByEmail(request.email()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email_taken");
        });

        User u = new User();
        u.setUsername(request.username());
        u.setEmail(request.email());
        // Achte darauf: Feldname im Entity! (passwordHash vs password)
        u.setPasswordHash(passwordEncoder.encode(request.password()));

        users.save(u);
        log.info("User persisted: id={}, email={}", u.getId(), u.getEmail());

        String token = jwt.generate(
                String.valueOf(u.getId()),
                Map.of("username", u.getUsername(), "email", u.getEmail())
        );

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login called for email={}", request.email());
        User u = users.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_credentials"));

        if (!passwordEncoder.matches(request.password(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_credentials");
        }

        String token = jwt.generate(
                String.valueOf(u.getId()),
                Map.of("username", u.getUsername(), "email", u.getEmail())
        );
        return new AuthResponse(token);
    }
}
