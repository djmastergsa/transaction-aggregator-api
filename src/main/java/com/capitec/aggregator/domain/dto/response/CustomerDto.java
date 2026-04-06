package com.capitec.aggregator.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Customer response object")
public record CustomerDto(
        @Schema(description = "Internal UUID of the customer")
        UUID id,

        @Schema(description = "External customer ID", example = "CUST001")
        String customerId,

        @Schema(description = "First name", example = "Sipho")
        String firstName,

        @Schema(description = "Last name", example = "Dlamini")
        String lastName,

        @Schema(description = "Full name", example = "Sipho Dlamini")
        String fullName,

        @Schema(description = "Email address", example = "sipho.dlamini@email.co.za")
        String email,

        @Schema(description = "Phone number", example = "+27 82 123 4567")
        String phone,

        @Schema(description = "Account creation date")
        LocalDateTime createdAt,

        @Schema(description = "Total number of transactions")
        long totalTransactions
) {}
