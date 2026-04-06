package com.capitec.aggregator.domain.dto.response;

import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.domain.enums.TransactionStatus;
import com.capitec.aggregator.domain.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Transaction response object")
public record TransactionDto(
        @Schema(description = "Internal UUID of the transaction")
        UUID id,

        @Schema(description = "Unique transaction reference", example = "BANK-CUST001-001")
        String transactionRef,

        @Schema(description = "Customer ID", example = "CUST001")
        String customerId,

        @Schema(description = "Customer full name", example = "Sipho Dlamini")
        String customerName,

        @Schema(description = "Transaction amount in ZAR", example = "1500.00")
        BigDecimal amount,

        @Schema(description = "Currency code", example = "ZAR")
        String currency,

        @Schema(description = "Transaction type (DEBIT or CREDIT)", example = "DEBIT")
        TransactionType type,

        @Schema(description = "Transaction category", example = "GROCERIES")
        TransactionCategory category,

        @Schema(description = "Transaction description", example = "Purchase at Woolworths")
        String description,

        @Schema(description = "Merchant name", example = "Woolworths")
        String merchant,

        @Schema(description = "Source system", example = "BANK")
        String sourceSystem,

        @Schema(description = "Date and time of the transaction")
        LocalDateTime transactionDate,

        @Schema(description = "Date and time the record was processed")
        LocalDateTime processedAt,

        @Schema(description = "Transaction status", example = "PROCESSED")
        TransactionStatus status
) {}
