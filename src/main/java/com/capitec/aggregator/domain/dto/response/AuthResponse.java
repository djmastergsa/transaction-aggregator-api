package com.capitec.aggregator.domain.dto.response;

import java.util.List;

public record AuthResponse(
        String token,
        String type,
        String username,
        List<String> roles,
        long expiresIn
) {}
