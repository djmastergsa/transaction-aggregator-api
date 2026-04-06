package com.capitec.aggregator.controller;

import com.capitec.aggregator.domain.dto.request.TransactionFilterRequest;
import com.capitec.aggregator.domain.dto.response.*;
import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.domain.enums.TransactionStatus;
import com.capitec.aggregator.domain.enums.TransactionType;
import com.capitec.aggregator.service.TransactionAggregatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Transaction management, aggregation, and analytics endpoints")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionAggregatorService aggregatorService;

    public TransactionController(TransactionAggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    @GetMapping
    @Operation(
            summary = "List all transactions",
            description = "Retrieve paginated transactions with optional filtering by customer, category, type, status, date range, amount range, and source system. Supports sorting by date or amount."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters",
                    content = @Content(schema = @Schema(implementation = com.capitec.aggregator.exception.ErrorResponse.class)))
    })
    public ResponseEntity<PagedResponse<TransactionDto>> getTransactions(
            @Parameter(description = "Filter by customer ID") @RequestParam(required = false) String customerId,
            @Parameter(description = "Filter by category") @RequestParam(required = false) TransactionCategory category,
            @Parameter(description = "Filter by type") @RequestParam(required = false) TransactionType type,
            @Parameter(description = "Filter by status") @RequestParam(required = false) TransactionStatus status,
            @Parameter(description = "Filter by source system (BANK, CREDIT_CARD, MOBILE_PAYMENT)") @RequestParam(required = false) String sourceSystem,
            @Parameter(description = "Date range start (ISO datetime)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @Parameter(description = "Date range end (ISO datetime)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @Parameter(description = "Minimum amount") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Maximum amount") @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "Sort field: transactionDate, amount, merchant, category") @RequestParam(defaultValue = "transactionDate") String sortBy,
            @Parameter(description = "Sort direction: ASC or DESC") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, 100);
        TransactionFilterRequest filter = new TransactionFilterRequest(
                customerId, category, type, status, sourceSystem,
                dateFrom, dateTo, minAmount, maxAmount,
                sortBy, sortDirection, page, size);

        log.info("GET /transactions - filter: {}", filter);
        return ResponseEntity.ok(aggregatorService.getTransactions(filter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieve a single transaction by its internal UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = com.capitec.aggregator.exception.ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format",
                    content = @Content(schema = @Schema(implementation = com.capitec.aggregator.exception.ErrorResponse.class)))
    })
    public ResponseEntity<TransactionDto> getTransactionById(
            @Parameter(description = "Transaction UUID", required = true) @PathVariable String id) {
        log.info("GET /transactions/{}", id);
        return ResponseEntity.ok(aggregatorService.getTransactionById(id));
    }

    @GetMapping("/ref/{transactionRef}")
    @Operation(summary = "Get transaction by reference", description = "Retrieve a single transaction by its unique business reference (e.g. BANK-CUST001-SAL-001)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = com.capitec.aggregator.exception.ErrorResponse.class)))
    })
    public ResponseEntity<TransactionDto> getTransactionByRef(
            @Parameter(description = "Transaction reference", required = true, example = "BANK-CUST001-SAL-001")
            @PathVariable String transactionRef) {
        log.info("GET /transactions/ref/{}", transactionRef);
        return ResponseEntity.ok(aggregatorService.getTransactionByRef(transactionRef));
    }

    @PostMapping("/sync")
    @Operation(
            summary = "Sync from all data sources",
            description = "Triggers a full sync from all mock data source adapters (BANK, CREDIT_CARD, MOBILE_PAYMENT). " +
                    "Existing transaction refs are skipped to avoid duplication. Returns count of newly persisted transactions."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sync completed successfully"),
            @ApiResponse(responseCode = "409", description = "Sync already in progress",
                    content = @Content(schema = @Schema(implementation = com.capitec.aggregator.exception.ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Sync failed",
                    content = @Content(schema = @Schema(implementation = com.capitec.aggregator.exception.ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> syncAllSources() {
        log.info("POST /transactions/sync - triggering data sync");
        int count = aggregatorService.syncAllSources();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Sync completed successfully",
                "newTransactionsPersisted", count,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/aggregate")
    @Operation(
            summary = "Get aggregation summary",
            description = "Compute an overall aggregation summary including total debits, credits, net position, " +
                    "transaction counts by category/source/type, and date range. Supports the same filters as GET /transactions."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aggregation summary computed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters",
                    content = @Content(schema = @Schema(implementation = com.capitec.aggregator.exception.ErrorResponse.class)))
    })
    public ResponseEntity<AggregationSummaryDto> getAggregationSummary(
            @Parameter(description = "Filter by customer ID") @RequestParam(required = false) String customerId,
            @Parameter(description = "Filter by category") @RequestParam(required = false) TransactionCategory category,
            @Parameter(description = "Filter by type") @RequestParam(required = false) TransactionType type,
            @Parameter(description = "Filter by status") @RequestParam(required = false) TransactionStatus status,
            @Parameter(description = "Filter by source system") @RequestParam(required = false) String sourceSystem,
            @Parameter(description = "Date range start") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @Parameter(description = "Date range end") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @Parameter(description = "Minimum amount") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Maximum amount") @RequestParam(required = false) BigDecimal maxAmount) {

        TransactionFilterRequest filter = new TransactionFilterRequest(
                customerId, category, type, status, sourceSystem,
                dateFrom, dateTo, minAmount, maxAmount,
                "transactionDate", "DESC", 0, Integer.MAX_VALUE);

        log.info("GET /transactions/aggregate");
        return ResponseEntity.ok(aggregatorService.getAggregationSummary(filter));
    }

    @GetMapping("/categories/summary")
    @Operation(
            summary = "Category breakdown",
            description = "Retrieve a summary of transactions grouped by category, including total amounts, " +
                    "transaction counts, and percentage of total spend per category."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category summary retrieved successfully")
    })
    public ResponseEntity<List<CategorySummaryDto>> getCategorySummary(
            @Parameter(description = "Filter by customer ID (omit for all customers)") @RequestParam(required = false) String customerId) {
        log.info("GET /transactions/categories/summary");
        return ResponseEntity.ok(aggregatorService.getCategorySummary(customerId));
    }

    @GetMapping("/trends/monthly")
    @Operation(
            summary = "Monthly spending trends",
            description = "Retrieve monthly aggregated spending data for the last 12 months, showing total debits, credits, " +
                    "net position, and transaction count per month."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly trends retrieved successfully")
    })
    public ResponseEntity<List<MonthlyTrendDto>> getMonthlyTrends(
            @Parameter(description = "Filter by customer ID (omit for all customers)") @RequestParam(required = false) String customerId) {
        log.info("GET /transactions/trends/monthly");
        return ResponseEntity.ok(aggregatorService.getMonthlyTrends(customerId));
    }

    @GetMapping("/sources/summary")
    @Operation(
            summary = "Source system summary",
            description = "Retrieve a summary broken down by data source (BANK, CREDIT_CARD, MOBILE_PAYMENT), " +
                    "showing transaction counts, totals, and percentage share."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Source summary retrieved successfully")
    })
    public ResponseEntity<List<SourceSummaryDto>> getSourceSummary() {
        log.info("GET /transactions/sources/summary");
        return ResponseEntity.ok(aggregatorService.getSourceSummary());
    }
}
