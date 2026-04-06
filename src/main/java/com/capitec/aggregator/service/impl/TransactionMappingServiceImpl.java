package com.capitec.aggregator.service.impl;

import com.capitec.aggregator.domain.dto.response.TransactionDto;
import com.capitec.aggregator.domain.entity.Transaction;
import com.capitec.aggregator.service.TransactionMappingService;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;

@Service
public class TransactionMappingServiceImpl implements TransactionMappingService {

    @Override
    public TransactionDto toDto(Transaction t) {
        return new TransactionDto(
                t.getId(),
                t.getTransactionRef(),
                t.getCustomer().getCustomerId(),
                t.getCustomer().getFirstName() + " " + t.getCustomer().getLastName(),
                t.getAmount().setScale(2, RoundingMode.HALF_UP),
                t.getCurrency(),
                t.getType(),
                t.getCategory(),
                t.getDescription(),
                t.getMerchant(),
                t.getSourceSystem(),
                t.getTransactionDate(),
                t.getProcessedAt(),
                t.getStatus()
        );
    }
}
