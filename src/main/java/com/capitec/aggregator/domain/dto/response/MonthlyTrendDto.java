package com.capitec.aggregator.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Monthly spending trend data point")
public record MonthlyTrendDto(
        @Schema(description = "Year", example = "2024")
        int year,

        @Schema(description = "Month number (1-12)", example = "3")
        int month,

        @Schema(description = "Month name", example = "March")
        String monthName,

        @Schema(description = "Year-month label for display", example = "2024-03")
        String period,

        @Schema(description = "Total debit amount for the month in ZAR")
        BigDecimal totalDebits,

        @Schema(description = "Total credit amount for the month in ZAR")
        BigDecimal totalCredits,

        @Schema(description = "Net position for the month (credits - debits) in ZAR")
        BigDecimal netPosition,

        @Schema(description = "Number of transactions in the month")
        long transactionCount,

        @Schema(description = "Average transaction amount for the month")
        BigDecimal averageTransactionAmount
) {}
