package com.capitec.aggregator.repository;

import com.capitec.aggregator.domain.entity.Transaction;
import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>,
        JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByTransactionRef(String transactionRef);

    boolean existsByTransactionRef(String transactionRef);

    Page<Transaction> findByCustomerCustomerId(String customerId, Pageable pageable);

    List<Transaction> findByCustomerCustomerId(String customerId);

    // --- Aggregation queries ---

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.customer.customerId = :customerId AND t.type = :type")
    BigDecimal sumAmountByCustomerAndType(@Param("customerId") String customerId,
                                          @Param("type") TransactionType type);

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.type = :type GROUP BY t.category")
    List<Object[]> sumAmountByCategoryAndType(@Param("type") TransactionType type);

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t " +
            "WHERE t.customer.customerId = :customerId AND t.type = :type " +
            "GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> sumAmountByCategoryAndTypeForCustomer(@Param("customerId") String customerId,
                                                          @Param("type") TransactionType type);

    @Query("SELECT t.category, COUNT(t), SUM(t.amount) FROM Transaction t GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> findCategorySummary();

    @Query("SELECT t.category, COUNT(t), SUM(t.amount) FROM Transaction t " +
            "WHERE t.customer.customerId = :customerId " +
            "GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> findCategorySummaryForCustomer(@Param("customerId") String customerId);

    @Query("SELECT t.sourceSystem, COUNT(t), SUM(CASE WHEN t.type = 'DEBIT' THEN t.amount ELSE 0 END), " +
            "SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END) " +
            "FROM Transaction t GROUP BY t.sourceSystem")
    List<Object[]> findSourceSummary();

    // Monthly trend queries
    @Query("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), " +
            "SUM(CASE WHEN t.type = 'DEBIT' THEN t.amount ELSE 0 END), " +
            "SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), " +
            "COUNT(t) " +
            "FROM Transaction t " +
            "WHERE t.transactionDate >= :fromDate " +
            "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
            "ORDER BY YEAR(t.transactionDate) ASC, MONTH(t.transactionDate) ASC")
    List<Object[]> findMonthlyTrends(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), " +
            "SUM(CASE WHEN t.type = 'DEBIT' THEN t.amount ELSE 0 END), " +
            "SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), " +
            "COUNT(t) " +
            "FROM Transaction t " +
            "WHERE t.customer.customerId = :customerId AND t.transactionDate >= :fromDate " +
            "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
            "ORDER BY YEAR(t.transactionDate) ASC, MONTH(t.transactionDate) ASC")
    List<Object[]> findMonthlyTrendsForCustomer(@Param("customerId") String customerId,
                                                 @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT MIN(t.transactionDate) FROM Transaction t")
    LocalDateTime findEarliestTransactionDate();

    @Query("SELECT MAX(t.transactionDate) FROM Transaction t")
    LocalDateTime findLatestTransactionDate();

    @Query("SELECT MAX(t.amount) FROM Transaction t WHERE t.customer.customerId = :customerId")
    BigDecimal findMaxAmountByCustomerId(@Param("customerId") String customerId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.customer.customerId = :customerId")
    long countByCustomerId(@Param("customerId") String customerId);

    @Query("SELECT t.type, COUNT(t) FROM Transaction t GROUP BY t.type")
    List<Object[]> countByType();

    @Query("SELECT t.status, COUNT(t) FROM Transaction t GROUP BY t.status")
    List<Object[]> countByStatus();
}
