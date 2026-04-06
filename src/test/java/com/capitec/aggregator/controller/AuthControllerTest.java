package com.capitec.aggregator.controller;

import com.capitec.aggregator.config.SecurityConfig;
import com.capitec.aggregator.domain.dto.request.LoginRequest;
import com.capitec.aggregator.exception.GlobalExceptionHandler;
import com.capitec.aggregator.security.JwtAuthenticationFilter;
import com.capitec.aggregator.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtTokenProvider.class, JwtAuthenticationFilter.class})
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginTests {

        @Test
        @DisplayName("should return 200 with JWT token for valid admin credentials")
        void should_return_token_for_admin() throws Exception {
            LoginRequest request = new LoginRequest("admin", "admin123");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", not(emptyString())))
                    .andExpect(jsonPath("$.type", is("Bearer")))
                    .andExpect(jsonPath("$.username", is("admin")))
                    .andExpect(jsonPath("$.roles", hasItem("ROLE_ADMIN")))
                    .andExpect(jsonPath("$.expiresIn", is(86400000)));
        }

        @Test
        @DisplayName("should return 200 with JWT token for valid user credentials")
        void should_return_token_for_user() throws Exception {
            LoginRequest request = new LoginRequest("user", "user123");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", not(emptyString())))
                    .andExpect(jsonPath("$.username", is("user")))
                    .andExpect(jsonPath("$.roles", hasItem("ROLE_USER")))
                    .andExpect(jsonPath("$.roles", not(hasItem("ROLE_ADMIN"))));
        }

        @Test
        @DisplayName("should return 401 for wrong password")
        void should_return_401_for_wrong_password() throws Exception {
            LoginRequest request = new LoginRequest("admin", "wrongpassword");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 401 for unknown user")
        void should_return_401_for_unknown_user() throws Exception {
            LoginRequest request = new LoginRequest("hacker", "password");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 400 when username is blank")
        void should_return_400_when_username_blank() throws Exception {
            LoginRequest request = new LoginRequest("", "password");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when password is blank")
        void should_return_400_when_password_blank() throws Exception {
            LoginRequest request = new LoginRequest("admin", "");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
