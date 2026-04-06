package com.capitec.aggregator.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Overall aggregation summary for transactions")
public record AggregationSummaryDto(
        @Schema(description = "Total number of transactions")
        long totalTransactions,

        @Schema(description = "Total debit amount in ZAR")
        BigDecimal totalDebits,

        @Schema(description = "Total credit amount in ZAR")
        BigDecimal totalCredits,

        @Schema(description = "Net position (credits minus debits) in ZAR")
        BigDecimal netPosition,

        @Schema(description = "Average transaction amount in ZAR")
        BigDecimal averageTransactionAmount,

        @Schema(description = "Transaction count grouped by category")
        Map<String, Long> transactionsByCategory,

        @Schema(description = "Total amount grouped by category")
        Map<String, BigDecimal> amountByCategory,

        @Schema(description = "Transaction count grouped by source system")
        Map<String, Long> transactionsBySource,

        @Schema(description = "Total amount grouped by source system")
        Map<String, BigDecimal> amountBySource,

        @Schema(description = "Transaction count grouped by type")
        Map<String, Long> transactionsByType,

        @Schema(description = "Transaction count grouped by status")
        Map<String, Long> transactionsByStatus,

        @Schema(description = "Earliest transaction date in the result set")
        LocalDateTime dateRangeFrom,

        @Schema(description = "Latest transaction date in the result set")
        LocalDateTime dateRangeTo,

        @Schema(description = "Timestamp when this summary was generated")
        LocalDateTime generatedAt
) {}
