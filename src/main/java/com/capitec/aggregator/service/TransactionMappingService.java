package com.capitec.aggregator.service;

import com.capitec.aggregator.domain.dto.response.TransactionDto;
import com.capitec.aggregator.domain.entity.Transaction;

/**
 * Service for mapping Transaction entities to DTOs.
 * Separated from the aggregation service to avoid circular dependencies
 * and to give a clean conversion interface.
 */
public interface TransactionMappingService {

    TransactionDto toDto(Transaction transaction);
}
