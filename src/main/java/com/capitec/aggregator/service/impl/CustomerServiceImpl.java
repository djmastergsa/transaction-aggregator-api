package com.capitec.aggregator.service.impl;

import com.capitec.aggregator.domain.dto.request.TransactionFilterRequest;
import com.capitec.aggregator.domain.dto.response.*;
import com.capitec.aggregator.domain.entity.Customer;
import com.capitec.aggregator.domain.entity.Transaction;
import com.capitec.aggregator.domain.enums.TransactionType;
import com.capitec.aggregator.exception.ResourceNotFoundException;
import com.capitec.aggregator.repository.CustomerRepository;
import com.capitec.aggregator.repository.TransactionRepository;
import com.capitec.aggregator.repository.TransactionSpecification;
import com.capitec.aggregator.service.CustomerService;
import com.capitec.aggregator.service.TransactionAggregatorService;
import com.capitec.aggregator.service.TransactionMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionAggregatorService transactionAggregatorService;
    private final TransactionMappingService mappingService;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                                TransactionRepository transactionRepository,
                                TransactionAggregatorService transactionAggregatorService,
                                TransactionMappingService mappingService) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.transactionAggregatorService = transactionAggregatorService;
        this.mappingService = mappingService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> getAllCustomers() {
        log.info("Fetching all customers");
        return customerRepository.findAllOrderByName().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByCustomerId(String customerId) {
        log.info("Fetching customer by customerId: {}", customerId);
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "customerId", customerId));
        return toDto(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TransactionDto> getCustomerTransactions(String customerId, TransactionFilterRequest filter) {
        log.info("Fetching transactions for customer: {}", customerId);

        // Validate customer exists
        if (!customerRepository.existsByCustomerId(customerId)) {
            throw new ResourceNotFoundException("Customer", "customerId", customerId);
        }

        // Force customerId filter for this customer
        filter.setCustomerId(customerId);

        Specification<Transaction> spec = TransactionSpecification.buildFilter(filter);
        Sort sort = buildSort(filter.getSortBy(), filter.getSortDirection());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<Transaction> page = transactionRepository.findAll(spec, pageable);
        Page<TransactionDto> dtoPage = page.map(mappingService::toDto);

        return PagedResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerSummaryDto getCustomerSummary(String customerId) {
        log.info("Computing financial summary for customer: {}", customerId);

        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "customerId", customerId));

        BigDecimal totalIncome = transactionRepository.sumAmountByCustomerAndType(customerId, TransactionType.CREDIT);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        BigDecimal totalExpenses = transactionRepository.sumAmountByCustomerAndType(customerId, TransactionType.DEBIT);
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal netPosition = totalIncome.subtract(totalExpenses);
        long totalTransactions = transactionRepository.countByCustomerId(customerId);

        BigDecimal maxAmount = transactionRepository.findMaxAmountByCustomerId(customerId);
        if (maxAmount == null) maxAmount = BigDecimal.ZERO;

        // Avg monthly spend = total expenses / 6 months of data
        BigDecimal avgMonthlySpend = totalTransactions == 0 ? BigDecimal.ZERO :
                totalExpenses.divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);

        List<CategorySummaryDto> topCategories = transactionAggregatorService.getCategorySummary(customerId)
                .stream().limit(5).collect(Collectors.toList());

        return new CustomerSummaryDto(
                customerId,
                customer.getFirstName() + " " + customer.getLastName(),
                totalTransactions,
                totalIncome.setScale(2, RoundingMode.HALF_UP),
                totalExpenses.setScale(2, RoundingMode.HALF_UP),
                netPosition.setScale(2, RoundingMode.HALF_UP),
                avgMonthlySpend,
                maxAmount.setScale(2, RoundingMode.HALF_UP),
                topCategories
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorySummaryDto> getCustomerCategorySummary(String customerId) {
        log.info("Computing category summary for customer: {}", customerId);
        if (!customerRepository.existsByCustomerId(customerId)) {
            throw new ResourceNotFoundException("Customer", "customerId", customerId);
        }
        return transactionAggregatorService.getCategorySummary(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyTrendDto> getCustomerMonthlyTrends(String customerId) {
        log.info("Computing monthly trends for customer: {}", customerId);
        if (!customerRepository.existsByCustomerId(customerId)) {
            throw new ResourceNotFoundException("Customer", "customerId", customerId);
        }
        return transactionAggregatorService.getMonthlyTrends(customerId);
    }

    // ---- Mapping helpers ----

    private CustomerDto toDto(Customer customer) {
        long txCount = transactionRepository.countByCustomerId(customer.getCustomerId());
        return new CustomerDto(
                customer.getId(),
                customer.getCustomerId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getFirstName() + " " + customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCreatedAt(),
                txCount
        );
    }

    private Sort buildSort(String sortBy, String direction) {
        String field = switch (sortBy != null ? sortBy.toLowerCase() : "") {
            case "amount" -> "amount";
            case "merchant" -> "merchant";
            case "category" -> "category";
            default -> "transactionDate";
        };
        return "ASC".equalsIgnoreCase(direction) ? Sort.by(field).ascending() : Sort.by(field).descending();
    }
}
