package com.capitec.aggregator.service.impl;

import com.capitec.aggregator.config.CacheConfig;
import com.capitec.aggregator.datasource.DataSourceAdapter;
import com.capitec.aggregator.datasource.model.RawTransaction;
import com.capitec.aggregator.domain.dto.request.TransactionFilterRequest;
import com.capitec.aggregator.domain.dto.response.*;
import com.capitec.aggregator.domain.entity.Customer;
import com.capitec.aggregator.domain.entity.Transaction;
import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.domain.enums.TransactionType;
import com.capitec.aggregator.exception.ResourceNotFoundException;
import com.capitec.aggregator.exception.SyncInProgressException;
import com.capitec.aggregator.repository.CustomerRepository;
import com.capitec.aggregator.repository.TransactionRepository;
import com.capitec.aggregator.repository.TransactionSpecification;
import com.capitec.aggregator.service.TransactionAggregatorService;
import com.capitec.aggregator.service.TransactionMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class TransactionAggregatorServiceImpl implements TransactionAggregatorService {

    private static final Logger log = LoggerFactory.getLogger(TransactionAggregatorServiceImpl.class);

    private final List<DataSourceAdapter> dataSourceAdapters;
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final TransactionMappingService mappingService;

    // Prevents overlapping sync calls (e.g., manual POST /sync while startup sync is still running)
    private final AtomicBoolean syncInProgress = new AtomicBoolean(false);

    public TransactionAggregatorServiceImpl(List<DataSourceAdapter> dataSourceAdapters,
                                            TransactionRepository transactionRepository,
                                            CustomerRepository customerRepository,
                                            TransactionMappingService mappingService) {
        this.dataSourceAdapters = dataSourceAdapters;
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.mappingService = mappingService;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {
            CacheConfig.AGGREGATION_SUMMARY,
            CacheConfig.CATEGORY_SUMMARY,
            CacheConfig.MONTHLY_TRENDS,
            CacheConfig.SOURCE_SUMMARY
    }, allEntries = true)
    public int syncAllSources() {
        if (!syncInProgress.compareAndSet(false, true)) {
            throw new SyncInProgressException(
                    "A sync operation is already in progress. Please wait for it to complete.");
        }
        try {
            log.info("Starting sync from {} data source adapters", dataSourceAdapters.size());
            int totalPersisted = 0;
            List<String> adapterErrors = new ArrayList<>();

            for (DataSourceAdapter adapter : dataSourceAdapters) {
                log.info("Fetching from adapter: {}", adapter.getSourceName());

                // Isolate fetch failures: a broken/unavailable source should not abort the whole sync.
                // Data-integrity exceptions (e.g. unknown customer ref) are intentionally NOT caught here —
                // they indicate a real problem that must surface to the caller.
                List<RawTransaction> rawTransactions;
                try {
                    rawTransactions = adapter.fetchTransactions();
                } catch (Exception e) {
                    log.error("Adapter '{}' failed to fetch transactions: {}",
                            adapter.getSourceName(), e.getMessage(), e);
                    adapterErrors.add(adapter.getSourceName());
                    continue;
                }

                int persisted = persistRawTransactions(rawTransactions, adapter.getSourceName());
                totalPersisted += persisted;
                log.info("Adapter '{}' - fetched {}, newly persisted {}",
                        adapter.getSourceName(), rawTransactions.size(), persisted);
            }

            if (!adapterErrors.isEmpty()) {
                log.warn("Sync completed with errors from adapters: {}. {} transactions persisted.",
                        adapterErrors, totalPersisted);
            } else {
                log.info("Sync complete. Total newly persisted transactions: {}", totalPersisted);
            }

            return totalPersisted;
        } finally {
            syncInProgress.set(false);
        }
    }

    private int persistRawTransactions(List<RawTransaction> rawTransactions, String sourceName) {
        List<Transaction> toSave = new ArrayList<>();

        for (RawTransaction raw : rawTransactions) {
            if (transactionRepository.existsByTransactionRef(raw.transactionRef())) {
                log.debug("Skipping duplicate transaction ref: {}", raw.transactionRef());
                continue;
            }

            Customer customer = customerRepository.findByCustomerId(raw.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Customer", "customerId", raw.customerId()));

            Transaction transaction = new Transaction(
                    null,
                    raw.transactionRef(),
                    customer,
                    raw.amount(),
                    raw.type(),
                    raw.category(),
                    raw.description(),
                    raw.merchant(),
                    sourceName,
                    raw.transactionDate(),
                    null,
                    raw.currency() != null ? raw.currency() : "ZAR",
                    raw.status()
            );

            toSave.add(transaction);
        }

        if (!toSave.isEmpty()) {
            transactionRepository.saveAllAndFlush(toSave);
        }

        return toSave.size();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TransactionDto> getTransactions(TransactionFilterRequest filter) {
        Specification<Transaction> spec = TransactionSpecification.buildFilter(filter);
        Sort sort = buildSort(filter.getSortBy(), filter.getSortDirection());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<Transaction> page = transactionRepository.findAll(spec, pageable);
        Page<TransactionDto> dtoPage = page.map(mappingService::toDto);

        return PagedResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + id);
        }
        Transaction transaction = transactionRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        return mappingService.toDto(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDto getTransactionByRef(String transactionRef) {
        Transaction transaction = transactionRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "transactionRef", transactionRef));
        return mappingService.toDto(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.AGGREGATION_SUMMARY, key = "#filter.toString()")
    public AggregationSummaryDto getAggregationSummary(TransactionFilterRequest filter) {
        Specification<Transaction> spec = TransactionSpecification.buildFilter(filter);
        List<Transaction> transactions = transactionRepository.findAll(spec);

        if (transactions.isEmpty()) {
            return buildEmptySummary();
        }

        BigDecimal totalDebits = transactions.stream()
                .filter(t -> t.getType() == TransactionType.DEBIT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = transactions.stream()
                .filter(t -> t.getType() == TransactionType.CREDIT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netPosition = totalCredits.subtract(totalDebits);

        BigDecimal totalAmount = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgAmount = transactions.isEmpty() ? BigDecimal.ZERO :
                totalAmount.divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);

        Map<String, Long> byCategory = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory().name(), Collectors.counting()));

        Map<String, BigDecimal> amountByCategory = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory().name(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

        Map<String, Long> bySource = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getSourceSystem, Collectors.counting()));

        Map<String, BigDecimal> amountBySource = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getSourceSystem,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

        Map<String, Long> byType = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getType().name(), Collectors.counting()));

        Map<String, Long> byStatus = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));

        LocalDateTime minDate = transactions.stream()
                .map(Transaction::getTransactionDate)
                .min(LocalDateTime::compareTo).orElse(null);

        LocalDateTime maxDate = transactions.stream()
                .map(Transaction::getTransactionDate)
                .max(LocalDateTime::compareTo).orElse(null);

        return new AggregationSummaryDto(
                transactions.size(),
                totalDebits.setScale(2, RoundingMode.HALF_UP),
                totalCredits.setScale(2, RoundingMode.HALF_UP),
                netPosition.setScale(2, RoundingMode.HALF_UP),
                avgAmount,
                byCategory,
                amountByCategory,
                bySource,
                amountBySource,
                byType,
                byStatus,
                minDate,
                maxDate,
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CATEGORY_SUMMARY, key = "#customerId != null ? #customerId : 'ALL'")
    public List<CategorySummaryDto> getCategorySummary(String customerId) {
        List<Object[]> rawData;
        if (customerId != null && !customerId.isBlank()) {
            rawData = transactionRepository.findCategorySummaryForCustomer(customerId);
        } else {
            rawData = transactionRepository.findCategorySummary();
        }

        // Compute total spend for percentage calculation
        BigDecimal grandTotal = rawData.stream()
                .map(row -> (BigDecimal) row[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rawData.stream()
                .map(row -> buildCategorySummary(row, grandTotal))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.MONTHLY_TRENDS, key = "#customerId != null ? #customerId : 'ALL'")
    public List<MonthlyTrendDto> getMonthlyTrends(String customerId) {
        LocalDateTime fromDate = LocalDateTime.now().minusMonths(12).withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);

        List<Object[]> rawData;
        if (customerId != null && !customerId.isBlank()) {
            rawData = transactionRepository.findMonthlyTrendsForCustomer(customerId, fromDate);
        } else {
            rawData = transactionRepository.findMonthlyTrends(fromDate);
        }

        return rawData.stream()
                .map(this::buildMonthlyTrend)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.SOURCE_SUMMARY, key = "'global'")
    public List<SourceSummaryDto> getSourceSummary() {
        List<Object[]> rawData = transactionRepository.findSourceSummary();
        long totalCount = rawData.stream().mapToLong(row -> ((Number) row[1]).longValue()).sum();

        return rawData.stream()
                .map(row -> buildSourceSummary(row, totalCount))
                .collect(Collectors.toList());
    }

    // ---- Mapping helpers ----

    private CategorySummaryDto buildCategorySummary(Object[] row, BigDecimal grandTotal) {
        TransactionCategory category = (TransactionCategory) row[0];
        long count = ((Number) row[1]).longValue();
        BigDecimal total = (BigDecimal) row[2];

        BigDecimal percentage = grandTotal.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                total.multiply(BigDecimal.valueOf(100))
                        .divide(grandTotal, 2, RoundingMode.HALF_UP);

        BigDecimal avg = count == 0 ? BigDecimal.ZERO :
                total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        return new CategorySummaryDto(
                category.name(),
                count,
                total.setScale(2, RoundingMode.HALF_UP),
                total.setScale(2, RoundingMode.HALF_UP),
                BigDecimal.ZERO,
                percentage,
                avg
        );
    }

    private MonthlyTrendDto buildMonthlyTrend(Object[] row) {
        int year = ((Number) row[0]).intValue();
        int month = ((Number) row[1]).intValue();
        BigDecimal debits = toDecimal(row[2]);
        BigDecimal credits = toDecimal(row[3]);
        long count = ((Number) row[4]).longValue();

        BigDecimal net = credits.subtract(debits);
        BigDecimal avg = count == 0 ? BigDecimal.ZERO :
                (debits.add(credits)).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        String monthName = Month.of(month).name().charAt(0)
                + Month.of(month).name().substring(1).toLowerCase();

        return new MonthlyTrendDto(
                year,
                month,
                monthName,
                String.format("%d-%02d", year, month),
                debits.setScale(2, RoundingMode.HALF_UP),
                credits.setScale(2, RoundingMode.HALF_UP),
                net.setScale(2, RoundingMode.HALF_UP),
                count,
                avg
        );
    }

    private SourceSummaryDto buildSourceSummary(Object[] row, long totalCount) {
        String source = (String) row[0];
        long count = ((Number) row[1]).longValue();
        BigDecimal debits = toDecimal(row[2]);
        BigDecimal credits = toDecimal(row[3]);
        BigDecimal net = credits.subtract(debits);

        BigDecimal percentage = totalCount == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(count * 100.0 / totalCount).setScale(2, RoundingMode.HALF_UP);

        return new SourceSummaryDto(
                source,
                count,
                debits.setScale(2, RoundingMode.HALF_UP),
                credits.setScale(2, RoundingMode.HALF_UP),
                net.setScale(2, RoundingMode.HALF_UP),
                percentage
        );
    }

    private AggregationSummaryDto buildEmptySummary() {
        return new AggregationSummaryDto(
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                null,
                null,
                LocalDateTime.now()
        );
    }

    private Sort buildSort(String sortBy, String direction) {
        String field = switch (sortBy != null ? sortBy.toLowerCase() : "") {
            case "amount" -> "amount";
            case "merchant" -> "merchant";
            case "category" -> "category";
            case "status" -> "status";
            default -> "transactionDate";
        };
        return "ASC".equalsIgnoreCase(direction) ? Sort.by(field).ascending() : Sort.by(field).descending();
    }

    private BigDecimal toDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        return new BigDecimal(value.toString());
    }
}
