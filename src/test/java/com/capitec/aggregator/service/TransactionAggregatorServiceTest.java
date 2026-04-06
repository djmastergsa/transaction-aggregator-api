package com.capitec.aggregator.service;

import com.capitec.aggregator.datasource.DataSourceAdapter;
import com.capitec.aggregator.datasource.model.RawTransaction;
import com.capitec.aggregator.domain.dto.request.TransactionFilterRequest;
import com.capitec.aggregator.domain.dto.response.*;
import com.capitec.aggregator.domain.entity.Customer;
import com.capitec.aggregator.domain.entity.Transaction;
import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.domain.enums.TransactionStatus;
import com.capitec.aggregator.domain.enums.TransactionType;
import com.capitec.aggregator.exception.ResourceNotFoundException;
import com.capitec.aggregator.repository.CustomerRepository;
import com.capitec.aggregator.repository.TransactionRepository;
import com.capitec.aggregator.service.impl.TransactionAggregatorServiceImpl;
import com.capitec.aggregator.service.impl.TransactionMappingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionAggregatorService Tests")
class TransactionAggregatorServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private DataSourceAdapter bankAdapter;
    @Mock
    private DataSourceAdapter creditCardAdapter;

    private TransactionAggregatorServiceImpl aggregatorService;

    private Customer testCustomer;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        aggregatorService = new TransactionAggregatorServiceImpl(
                List.of(bankAdapter, creditCardAdapter),
                transactionRepository,
                customerRepository,
                new TransactionMappingServiceImpl()
        );

        testCustomer = new Customer();
        testCustomer.setId(UUID.randomUUID());
        testCustomer.setCustomerId("CUST001");
        testCustomer.setFirstName("Sipho");
        testCustomer.setLastName("Dlamini");
        testCustomer.setEmail("sipho@test.co.za");

        testTransaction = new Transaction();
        testTransaction.setId(UUID.randomUUID());
        testTransaction.setTransactionRef("BANK-CUST001-SAL-001");
        testTransaction.setCustomer(testCustomer);
        testTransaction.setAmount(new BigDecimal("45000.00"));
        testTransaction.setType(TransactionType.CREDIT);
        testTransaction.setCategory(TransactionCategory.SALARY);
        testTransaction.setDescription("Monthly Salary");
        testTransaction.setMerchant("Capitec Bank");
        testTransaction.setSourceSystem("BANK");
        testTransaction.setTransactionDate(LocalDateTime.now().minusDays(5));
        testTransaction.setCurrency("ZAR");
        testTransaction.setStatus(TransactionStatus.PROCESSED);
    }

    // ---- SYNC TESTS ----

    @Nested
    @DisplayName("syncAllSources")
    class SyncAllSourcesTests {

        @Test
        @DisplayName("should call all adapters and return total persisted count")
        void sync_should_call_all_adapters() {
            RawTransaction raw = buildRawTransaction("BANK-CUST001-001");
            when(bankAdapter.fetchTransactions()).thenReturn(List.of(raw));
            when(bankAdapter.getSourceName()).thenReturn("BANK");
            when(creditCardAdapter.fetchTransactions()).thenReturn(List.of());
            when(creditCardAdapter.getSourceName()).thenReturn("CREDIT_CARD");

            when(transactionRepository.existsByTransactionRef("BANK-CUST001-001")).thenReturn(false);
            when(customerRepository.findByCustomerId("CUST001")).thenReturn(Optional.of(testCustomer));
            when(transactionRepository.saveAllAndFlush(anyList())).thenReturn(List.of());

            int count = aggregatorService.syncAllSources();

            assertThat(count).isEqualTo(1);
            verify(bankAdapter).fetchTransactions();
            verify(creditCardAdapter).fetchTransactions();
        }

        @Test
        @DisplayName("should skip duplicate transaction refs")
        void sync_should_skip_duplicates() {
            RawTransaction raw = buildRawTransaction("BANK-CUST001-001");
            when(bankAdapter.fetchTransactions()).thenReturn(List.of(raw));
            when(bankAdapter.getSourceName()).thenReturn("BANK");
            when(creditCardAdapter.fetchTransactions()).thenReturn(List.of());
            when(creditCardAdapter.getSourceName()).thenReturn("CREDIT_CARD");

            // Already exists
            when(transactionRepository.existsByTransactionRef("BANK-CUST001-001")).thenReturn(true);

            int count = aggregatorService.syncAllSources();

            assertThat(count).isEqualTo(0);
            verify(transactionRepository, never()).saveAllAndFlush(anyList());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when customer not found during sync")
        void sync_should_throw_when_customer_not_found() {
            RawTransaction raw = new RawTransaction(
                    "BANK-CUST999-001", "CUST999", new BigDecimal("100.00"),
                    TransactionType.DEBIT, TransactionCategory.GROCERIES,
                    "Test transaction", "Test Merchant", "BANK",
                    LocalDateTime.now(), "ZAR", TransactionStatus.PROCESSED);

            when(bankAdapter.fetchTransactions()).thenReturn(List.of(raw));
            when(bankAdapter.getSourceName()).thenReturn("BANK");
            when(transactionRepository.existsByTransactionRef("BANK-CUST999-001")).thenReturn(false);
            when(customerRepository.findByCustomerId("CUST999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> aggregatorService.syncAllSources())
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("CUST999");
        }

        @Test
        @DisplayName("should persist multiple transactions from multiple adapters")
        void sync_should_persist_all_new_transactions() {
            RawTransaction bank1 = buildRawTransaction("BANK-001");
            RawTransaction bank2 = buildRawTransaction("BANK-002");
            RawTransaction cc1 = buildRawTransaction("CC-001");

            when(bankAdapter.fetchTransactions()).thenReturn(List.of(bank1, bank2));
            when(bankAdapter.getSourceName()).thenReturn("BANK");
            when(creditCardAdapter.fetchTransactions()).thenReturn(List.of(cc1));
            when(creditCardAdapter.getSourceName()).thenReturn("CREDIT_CARD");

            when(transactionRepository.existsByTransactionRef(anyString())).thenReturn(false);
            when(customerRepository.findByCustomerId("CUST001")).thenReturn(Optional.of(testCustomer));
            when(transactionRepository.saveAllAndFlush(anyList())).thenReturn(List.of());

            int count = aggregatorService.syncAllSources();

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("should handle empty result from adapter gracefully")
        void sync_with_empty_adapters_should_return_zero() {
            when(bankAdapter.fetchTransactions()).thenReturn(List.of());
            when(bankAdapter.getSourceName()).thenReturn("BANK");
            when(creditCardAdapter.fetchTransactions()).thenReturn(List.of());
            when(creditCardAdapter.getSourceName()).thenReturn("CREDIT_CARD");

            int count = aggregatorService.syncAllSources();

            assertThat(count).isEqualTo(0);
            verify(transactionRepository, never()).saveAllAndFlush(anyList());
        }
    }

    // ---- GET TRANSACTION TESTS ----

    @Nested
    @DisplayName("getTransactionById")
    class GetTransactionByIdTests {

        @Test
        @DisplayName("should return transaction dto when found")
        void should_return_dto_when_transaction_exists() {
            UUID id = testTransaction.getId();
            when(transactionRepository.findById(id)).thenReturn(Optional.of(testTransaction));

            TransactionDto dto = aggregatorService.getTransactionById(id.toString());

            assertThat(dto).isNotNull();
            assertThat(dto.transactionRef()).isEqualTo("BANK-CUST001-SAL-001");
            assertThat(dto.customerId()).isEqualTo("CUST001");
            assertThat(dto.amount()).isEqualByComparingTo(new BigDecimal("45000.00"));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void should_throw_when_transaction_not_found() {
            UUID id = UUID.randomUUID();
            when(transactionRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> aggregatorService.getTransactionById(id.toString()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for invalid UUID")
        void should_throw_for_invalid_uuid() {
            assertThatThrownBy(() -> aggregatorService.getTransactionById("not-a-uuid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid UUID format");
        }
    }

    @Nested
    @DisplayName("getTransactionByRef")
    class GetTransactionByRefTests {

        @Test
        @DisplayName("should return transaction dto when ref found")
        void should_return_dto_when_ref_exists() {
            when(transactionRepository.findByTransactionRef("BANK-CUST001-SAL-001"))
                    .thenReturn(Optional.of(testTransaction));

            TransactionDto dto = aggregatorService.getTransactionByRef("BANK-CUST001-SAL-001");

            assertThat(dto).isNotNull();
            assertThat(dto.transactionRef()).isEqualTo("BANK-CUST001-SAL-001");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when ref not found")
        void should_throw_when_ref_not_found() {
            when(transactionRepository.findByTransactionRef("UNKNOWN-REF")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> aggregatorService.getTransactionByRef("UNKNOWN-REF"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("UNKNOWN-REF");
        }
    }

    // ---- AGGREGATION TESTS ----

    @Nested
    @DisplayName("getAggregationSummary")
    class AggregationSummaryTests {

        @Test
        @DisplayName("should return empty summary when no transactions found")
        @SuppressWarnings("unchecked")
        void should_return_empty_summary_when_no_data() {
            when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of());

            TransactionFilterRequest filter = new TransactionFilterRequest();
            AggregationSummaryDto summary = aggregatorService.getAggregationSummary(filter);

            assertThat(summary.totalTransactions()).isZero();
            assertThat(summary.totalDebits()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.totalCredits()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.netPosition()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should compute correct totals from transaction list")
        @SuppressWarnings("unchecked")
        void should_compute_correct_totals() {
            Transaction credit = buildTransaction(TransactionType.CREDIT, new BigDecimal("50000.00"), TransactionCategory.SALARY);
            Transaction debit1 = buildTransaction(TransactionType.DEBIT, new BigDecimal("1000.00"), TransactionCategory.GROCERIES);
            Transaction debit2 = buildTransaction(TransactionType.DEBIT, new BigDecimal("500.00"), TransactionCategory.UTILITIES);

            when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(credit, debit1, debit2));

            TransactionFilterRequest filter = new TransactionFilterRequest();
            AggregationSummaryDto summary = aggregatorService.getAggregationSummary(filter);

            assertThat(summary.totalTransactions()).isEqualTo(3);
            assertThat(summary.totalCredits()).isEqualByComparingTo(new BigDecimal("50000.00"));
            assertThat(summary.totalDebits()).isEqualByComparingTo(new BigDecimal("1500.00"));
            assertThat(summary.netPosition()).isEqualByComparingTo(new BigDecimal("48500.00"));
        }

        @Test
        @DisplayName("should group transactions by category correctly")
        @SuppressWarnings("unchecked")
        void should_group_by_category() {
            Transaction t1 = buildTransaction(TransactionType.DEBIT, new BigDecimal("500.00"), TransactionCategory.GROCERIES);
            Transaction t2 = buildTransaction(TransactionType.DEBIT, new BigDecimal("300.00"), TransactionCategory.GROCERIES);
            Transaction t3 = buildTransaction(TransactionType.DEBIT, new BigDecimal("200.00"), TransactionCategory.UTILITIES);

            when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(t1, t2, t3));

            TransactionFilterRequest filter = new TransactionFilterRequest();
            AggregationSummaryDto summary = aggregatorService.getAggregationSummary(filter);

            assertThat(summary.transactionsByCategory()).containsEntry("GROCERIES", 2L);
            assertThat(summary.transactionsByCategory()).containsEntry("UTILITIES", 1L);
        }
    }

    // ---- HELPER METHODS ----

    private RawTransaction buildRawTransaction(String ref) {
        return new RawTransaction(
                ref, "CUST001", new BigDecimal("100.00"),
                TransactionType.DEBIT, TransactionCategory.GROCERIES,
                "Test transaction", "Test Merchant", "BANK",
                LocalDateTime.now(), "ZAR", TransactionStatus.PROCESSED);
    }

    private Transaction buildTransaction(TransactionType type, BigDecimal amount, TransactionCategory category) {
        Transaction t = new Transaction();
        t.setId(UUID.randomUUID());
        t.setTransactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8));
        t.setCustomer(testCustomer);
        t.setAmount(amount);
        t.setType(type);
        t.setCategory(category);
        t.setDescription("Test");
        t.setMerchant("Test Merchant");
        t.setSourceSystem("BANK");
        t.setTransactionDate(LocalDateTime.now());
        t.setCurrency("ZAR");
        t.setStatus(TransactionStatus.PROCESSED);
        return t;
    }
}
