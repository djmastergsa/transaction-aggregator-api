package com.capitec.aggregator.datasource.model;

import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.domain.enums.TransactionStatus;
import com.capitec.aggregator.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RawTransaction(
        String transactionRef,
        String customerId,
        BigDecimal amount,
        TransactionType type,
        TransactionCategory category,
        String description,
        String merchant,
        String sourceSystem,
        LocalDateTime transactionDate,
        String currency,
        TransactionStatus status
) {}
