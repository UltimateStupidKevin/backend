package de.technikerarbeit.backend.user;

import jakarta.persistence.*;

@Entity @Table(name = "users")
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false, unique=true, length=32)
  private String username;

  @Column(nullable=false, unique=true, length=255)
  private String email;

  @Column(name="password_hash", nullable=false, length=255)
  private String passwordHash;

  @Column(name="email_verified", nullable=false)
  private boolean emailVerified = false;

  public Long getId(){ return id; }
  public String getUsername(){ return username; }
  public void setUsername(String username){ this.username = username; }
  public String getEmail(){ return email; }
  public void setEmail(String email){ this.email = email; }
  public String getPasswordHash(){ return passwordHash; }
  public void setPasswordHash(String passwordHash){ this.passwordHash = passwordHash; }
  public boolean isEmailVerified(){ return emailVerified; }
  public void setEmailVerified(boolean emailVerified){ this.emailVerified = emailVerified; }
}
