package com.capitec.aggregator.controller;

import com.capitec.aggregator.domain.dto.request.TransactionFilterRequest;
import com.capitec.aggregator.domain.dto.response.*;
import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.domain.enums.TransactionStatus;
import com.capitec.aggregator.domain.enums.TransactionType;
import com.capitec.aggregator.service.CustomerService;
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

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Customer management and per-customer analytics endpoints")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @Operation(
            summary = "List all customers",
            description = "Retrieve all registered customers, ordered by last name then first name. " +
                    "Each customer record includes their total transaction count."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customers retrieved successfully")
    })
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        log.info("GET /customers");
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{customerId}")
    @Operation(
            summary = "Get customer by ID",
            description = "Retrieve a single customer by their external customer ID (e.g. CUST001)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = com.capitec.aggregator.exception.ErrorResponse.class)))
    })
    public ResponseEntity<CustomerDto> getCustomer(
            @Parameter(description = "Customer ID", required = true, example = "CUST001")
            @PathVariable String customerId) {
        log.info("GET /customers/{}", customerId);
        return ResponseEntity.ok(customerService.getCustomerByCustomerId(customerId));
    }

    @GetMapping("/{customerId}/transactions")
    @Operation(
            summary = "Get customer transactions",
            description = "Retrieve paginated and filtered transactions for a specific customer. " +
                    "Supports the same filtering and sorting options as the global transaction endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = com.capitec.aggregator.exception.ErrorResponse.class)))
    })
    public ResponseEntity<PagedResponse<TransactionDto>> getCustomerTransactions(
            @Parameter(description = "Customer ID", required = true, example = "CUST001") @PathVariable String customerId,
            @Parameter(description = "Filter by category") @RequestParam(required = false) TransactionCategory category,
            @Parameter(description = "Filter by type") @RequestParam(required = false) TransactionType type,
            @Parameter(description = "Filter by status") @RequestParam(required = false) TransactionStatus status,
            @Parameter(description = "Filter by source system") @RequestParam(required = false) String sourceSystem,
            @Parameter(description = "Date range start (ISO datetime)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @Parameter(description = "Date range end (ISO datetime)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @Parameter(description = "Minimum amount") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Maximum amount") @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "transactionDate") String sortBy,
            @Parameter(description = "Sort direction: ASC or DESC") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, 100);
        TransactionFilterRequest filter = new TransactionFilterRequest(
                null, category, type, status, sourceSystem,
                dateFrom, dateTo, minAmount, maxAmount,
                sortBy, sortDirection, page, size);

        log.info("GET /customers/{}/transactions", customerId);
        return ResponseEntity.ok(customerService.getCustomerTransactions(customerId, filter));
    }

    @GetMapping("/{customerId}/summary")
    @Operation(
            summary = "Get customer financial summary",
            description = "Retrieve a comprehensive financial summary for a customer including total income, " +
                    "total expenses, net position, average monthly spend, highest transaction, and top 5 spending categories."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = com.capitec.aggregator.exception.ErrorResponse.class)))
    })
    public ResponseEntity<CustomerSummaryDto> getCustomerSummary(
            @Parameter(description = "Customer ID", required = true, example = "CUST001")
            @PathVariable String customerId) {
        log.info("GET /customers/{}/summary", customerId);
        return ResponseEntity.ok(customerService.getCustomerSummary(customerId));
    }

    @GetMapping("/{customerId}/categories/summary")
    @Operation(
            summary = "Get customer category breakdown",
            description = "Retrieve category-level spending breakdown for a specific customer, " +
                    "including amounts, transaction counts, and percentage of total spend per category."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category summary retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = com.capitec.aggregator.exception.ErrorResponse.class)))
    })
    public ResponseEntity<List<CategorySummaryDto>> getCustomerCategorySummary(
            @Parameter(description = "Customer ID", required = true, example = "CUST001")
            @PathVariable String customerId) {
        log.info("GET /customers/{}/categories/summary", customerId);
        return ResponseEntity.ok(customerService.getCustomerCategorySummary(customerId));
    }

    @GetMapping("/{customerId}/trends/monthly")
    @Operation(
            summary = "Get customer monthly trends",
            description = "Retrieve monthly spending trends for a specific customer over the last 12 months. " +
                    "Shows total debits, credits, net position, and transaction count per month."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly trends retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = com.capitec.aggregator.exception.ErrorResponse.class)))
    })
    public ResponseEntity<List<MonthlyTrendDto>> getCustomerMonthlyTrends(
            @Parameter(description = "Customer ID", required = true, example = "CUST001")
            @PathVariable String customerId) {
        log.info("GET /customers/{}/trends/monthly", customerId);
        return ResponseEntity.ok(customerService.getCustomerMonthlyTrends(customerId));
    }
}
