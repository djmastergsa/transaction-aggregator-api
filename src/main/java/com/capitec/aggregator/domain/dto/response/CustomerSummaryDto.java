package com.capitec.aggregator.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Financial summary for a specific customer")
public record CustomerSummaryDto(
        @Schema(description = "Customer ID", example = "CUST001")
        String customerId,

        @Schema(description = "Customer full name", example = "Sipho Dlamini")
        String customerName,

        @Schema(description = "Total number of transactions")
        long totalTransactions,

        @Schema(description = "Total income (credits) in ZAR")
        BigDecimal totalIncome,

        @Schema(description = "Total expenses (debits) in ZAR")
        BigDecimal totalExpenses,

        @Schema(description = "Net position (income - expenses) in ZAR")
        BigDecimal netPosition,

        @Schema(description = "Average monthly spend in ZAR")
        BigDecimal averageMonthlySpend,

        @Schema(description = "Highest single transaction amount in ZAR")
        BigDecimal highestTransaction,

        @Schema(description = "Top spending categories")
        List<CategorySummaryDto> topCategories
) {}
