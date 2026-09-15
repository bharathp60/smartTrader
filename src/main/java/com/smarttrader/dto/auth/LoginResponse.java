package com.smarttrader.dto.auth;

import java.time.Instant;
import java.util.List;

public record LoginResponse(
    String token,
    String username,
    List<String> roles,
    Instant expiresAt
) {}
