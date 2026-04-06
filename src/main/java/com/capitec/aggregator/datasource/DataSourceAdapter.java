package com.capitec.aggregator.datasource;

import com.capitec.aggregator.datasource.model.RawTransaction;

import java.util.List;

/**
 * Adapter interface for data sources providing transaction data.
 * Implementations of this interface adapt different external financial
 * data sources (bank, credit card, mobile payment) into a unified format.
 *
 * Part of the Adapter design pattern implementation.
 */
public interface DataSourceAdapter {

    /**
     * Returns the unique identifier for this data source (e.g., "BANK", "CREDIT_CARD").
     */
    String getSourceName();

    /**
     * Fetches all raw transactions from this data source.
     * Implementations return deterministic mock data.
     */
    List<RawTransaction> fetchTransactions();

    /**
     * Fetches raw transactions for a specific customer from this data source.
     */
    List<RawTransaction> fetchTransactionsForCustomer(String customerId);
}
