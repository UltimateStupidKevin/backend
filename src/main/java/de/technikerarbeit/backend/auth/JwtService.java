package de.technikerarbeit.backend.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

  private final SecretKey key;
  private final long expiresMinutes;

  // Werte kommen aus application.yaml: app.jwt.secret / app.jwt.expires-minutes
  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expires-minutes}") long expiresMinutes
  ) {
    // Secret muss mind. 32 Zeichen haben
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expiresMinutes = expiresMinutes;
  }

  /** Baut ein JWT mit subject (z.B. userId) + optionalen Claims */
  public String generate(String subject, Map<String, Object> claims) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(subject)
        .claims(claims == null ? Map.of() : claims)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(expiresMinutes * 60)))
        .signWith(key)
        .compact();
  }

  /** Liest das subject (bei uns: userId) aus dem Token */
  public String getSubject(String token) {
    return Jwts.parser()
        .verifyWith(key)     // <- 0.12.x: kein parserBuilder(), sondern parser().verifyWith(...)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }
}
