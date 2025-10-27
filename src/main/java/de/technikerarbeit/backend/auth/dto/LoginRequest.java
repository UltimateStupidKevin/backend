package de.technikerarbeit.backend.auth.dto;

public record LoginRequest(
        String email,
        String password
) { }
