package com.capitec.aggregator.domain.dto.request;

import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.domain.enums.TransactionStatus;
import com.capitec.aggregator.domain.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Request parameters for filtering transactions")
public class TransactionFilterRequest {

    @Schema(description = "Filter by customer ID (e.g. CUST001)", example = "CUST001")
    private String customerId;

    @Schema(description = "Filter by transaction category", example = "GROCERIES")
    private TransactionCategory category;

    @Schema(description = "Filter by transaction type", example = "DEBIT")
    private TransactionType type;

    @Schema(description = "Filter by transaction status", example = "PROCESSED")
    private TransactionStatus status;

    @Schema(description = "Filter by source system", example = "BANK")
    private String sourceSystem;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Start date for date range filter (ISO format)", example = "2024-01-01T00:00:00")
    private LocalDateTime dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "End date for date range filter (ISO format)", example = "2024-12-31T23:59:59")
    private LocalDateTime dateTo;

    @Schema(description = "Minimum transaction amount", example = "100.00")
    private BigDecimal minAmount;

    @Schema(description = "Maximum transaction amount", example = "10000.00")
    private BigDecimal maxAmount;

    @Schema(description = "Sort field (transactionDate, amount, merchant)", example = "transactionDate")
    private String sortBy = "transactionDate";

    @Schema(description = "Sort direction (ASC or DESC)", example = "DESC")
    private String sortDirection = "DESC";

    @Schema(description = "Page number (zero-based)", example = "0")
    private int page = 0;

    @Schema(description = "Page size", example = "20")
    private int size = 20;

    public TransactionFilterRequest() {}

    public TransactionFilterRequest(String customerId, TransactionCategory category, TransactionType type,
                                    TransactionStatus status, String sourceSystem, LocalDateTime dateFrom,
                                    LocalDateTime dateTo, BigDecimal minAmount, BigDecimal maxAmount,
                                    String sortBy, String sortDirection, int page, int size) {
        this.customerId = customerId;
        this.category = category;
        this.type = type;
        this.status = status;
        this.sourceSystem = sourceSystem;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.sortBy = sortBy != null ? sortBy : "transactionDate";
        this.sortDirection = sortDirection != null ? sortDirection : "DESC";
        this.page = page;
        this.size = size;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public TransactionCategory getCategory() { return category; }
    public void setCategory(TransactionCategory category) { this.category = category; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }

    public LocalDateTime getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDateTime dateFrom) { this.dateFrom = dateFrom; }

    public LocalDateTime getDateTo() { return dateTo; }
    public void setDateTo(LocalDateTime dateTo) { this.dateTo = dateTo; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    @Override
    public String toString() {
        return "TransactionFilterRequest{" +
                "customerId='" + customerId + '\'' +
                ", category=" + category +
                ", type=" + type +
                ", status=" + status +
                ", sourceSystem='" + sourceSystem + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", minAmount=" + minAmount +
                ", maxAmount=" + maxAmount +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}
