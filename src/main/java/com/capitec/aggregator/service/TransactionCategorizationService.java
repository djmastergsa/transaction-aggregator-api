package com.capitec.aggregator.service;

import com.capitec.aggregator.domain.enums.TransactionCategory;

/**
 * Service for categorizing transactions based on description and merchant name.
 * Implements the Strategy pattern - the categorization rules are encapsulated
 * behind this interface, allowing different categorization strategies to be
 * swapped in without changing callers.
 */
public interface TransactionCategorizationService {

    /**
     * Categorize a transaction based on its description and merchant name.
     *
     * @param description the transaction description
     * @param merchant    the merchant name
     * @return the determined TransactionCategory
     */
    TransactionCategory categorize(String description, String merchant);
}
