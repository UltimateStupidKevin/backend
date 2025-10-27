package de.technikerarbeit.backend;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MeController {

  @GetMapping("/me")
  public Object me(Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) {
      return Map.of("authenticated", false);
    }
    // In JwtAuthFilter setzt als Principal das Subject = userId (String)
    return Map.of(
        "authenticated", true,
        "principal", String.valueOf(auth.getPrincipal())
    );
  }
}
