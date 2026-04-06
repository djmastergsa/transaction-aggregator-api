package com.capitec.aggregator.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Summary for a single transaction category")
public record CategorySummaryDto(
        @Schema(description = "Category name", example = "GROCERIES")
        String category,

        @Schema(description = "Number of transactions in this category")
        long transactionCount,

        @Schema(description = "Total amount for this category in ZAR")
        BigDecimal totalAmount,

        @Schema(description = "Total debit amount for this category in ZAR")
        BigDecimal totalDebits,

        @Schema(description = "Total credit amount for this category in ZAR")
        BigDecimal totalCredits,

        @Schema(description = "Percentage of total spend this category represents")
        BigDecimal percentageOfTotal,

        @Schema(description = "Average transaction amount for this category")
        BigDecimal averageAmount
) {}
