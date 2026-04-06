package com.capitec.aggregator.controller;

import com.capitec.aggregator.domain.dto.request.LoginRequest;
import com.capitec.aggregator.domain.dto.response.AuthResponse;
import com.capitec.aggregator.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Obtain a JWT bearer token")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticate with username and password to receive a JWT bearer token. " +
                    "Include the token in the `Authorization: Bearer <token>` header on subsequent requests.\n\n" +
                    "**Test credentials:**\n" +
                    "- `admin` / `admin123` — full access including sync\n" +
                    "- `user` / `user123` — read-only access"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful — JWT returned"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String token = tokenProvider.generateToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(new AuthResponse(token, "Bearer", request.username(), roles, jwtExpiration));
    }
}
