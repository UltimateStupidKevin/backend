package de.technikerarbeit.backend.auth;

import de.technikerarbeit.backend.auth.dto.AuthResponse;
import de.technikerarbeit.backend.auth.dto.LoginRequest;
import de.technikerarbeit.backend.auth.dto.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AuthResponse register(@RequestBody RegisterRequest request) {
        log.info("AuthController.register IN email={}, username={}", request.email(), request.username());
        return auth.register(request);
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AuthResponse login(@RequestBody LoginRequest request) {
        log.info("AuthController.login IN email={}", request.email());
        return auth.login(request);
    }
}
