package com.capitec.aggregator.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Summary for a single data source")
public record SourceSummaryDto(
        @Schema(description = "Source system name", example = "BANK")
        String sourceSystem,

        @Schema(description = "Number of transactions from this source")
        long transactionCount,

        @Schema(description = "Total debit amount from this source in ZAR")
        BigDecimal totalDebits,

        @Schema(description = "Total credit amount from this source in ZAR")
        BigDecimal totalCredits,

        @Schema(description = "Net position for this source in ZAR")
        BigDecimal netPosition,

        @Schema(description = "Percentage of total transactions this source represents")
        BigDecimal percentageOfTotal
) {}
