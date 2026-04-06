package com.capitec.aggregator.service;

import com.capitec.aggregator.domain.dto.request.TransactionFilterRequest;
import com.capitec.aggregator.domain.dto.response.*;

import java.util.List;

/**
 * Primary service for transaction aggregation operations.
 * Orchestrates data loading from all adapters, persists transactions,
 * and provides aggregation analytics.
 */
public interface TransactionAggregatorService {

    /**
     * Synchronise transactions from all registered data source adapters.
     * Skips already-existing transaction refs to avoid duplicates.
     *
     * @return count of newly persisted transactions
     */
    int syncAllSources();

    /**
     * Retrieve paginated and filtered transactions.
     */
    PagedResponse<TransactionDto> getTransactions(TransactionFilterRequest filter);

    /**
     * Get a single transaction by its internal UUID.
     */
    TransactionDto getTransactionById(String id);

    /**
     * Get a single transaction by its business reference.
     */
    TransactionDto getTransactionByRef(String transactionRef);

    /**
     * Compute an overall aggregation summary across all transactions
     * (or filtered by the provided request).
     */
    AggregationSummaryDto getAggregationSummary(TransactionFilterRequest filter);

    /**
     * Get a breakdown by category with totals and percentages.
     */
    List<CategorySummaryDto> getCategorySummary(String customerId);

    /**
     * Get monthly spending trends for the last 12 months.
     */
    List<MonthlyTrendDto> getMonthlyTrends(String customerId);

    /**
     * Get per-source-system summary.
     */
    List<SourceSummaryDto> getSourceSummary();
}
