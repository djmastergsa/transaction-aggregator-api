package com.capitec.aggregator.repository;

import com.capitec.aggregator.domain.dto.request.TransactionFilterRequest;
import com.capitec.aggregator.domain.entity.Customer;
import com.capitec.aggregator.domain.entity.Transaction;
import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.domain.enums.TransactionStatus;
import com.capitec.aggregator.domain.enums.TransactionType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    private TransactionSpecification() {
        // Utility class - no instantiation
    }

    public static Specification<Transaction> buildFilter(TransactionFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join with customer for customerId filter
            Join<Transaction, Customer> customerJoin = root.join("customer", JoinType.INNER);

            if (filter.getCustomerId() != null && !filter.getCustomerId().isBlank()) {
                predicates.add(cb.equal(customerJoin.get("customerId"), filter.getCustomerId()));
            }

            if (filter.getCategory() != null) {
                predicates.add(cb.equal(root.get("category"), filter.getCategory()));
            }

            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getSourceSystem() != null && !filter.getSourceSystem().isBlank()) {
                predicates.add(cb.equal(root.get("sourceSystem"), filter.getSourceSystem()));
            }

            if (filter.getDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), filter.getDateFrom()));
            }

            if (filter.getDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), filter.getDateTo()));
            }

            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }

            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Transaction> hasCustomerId(String customerId) {
        return (root, query, cb) -> {
            if (customerId == null || customerId.isBlank()) {
                return cb.conjunction();
            }
            Join<Transaction, Customer> customerJoin = root.join("customer", JoinType.INNER);
            return cb.equal(customerJoin.get("customerId"), customerId);
        };
    }

    public static Specification<Transaction> hasCategory(TransactionCategory category) {
        return (root, query, cb) -> {
            if (category == null) return cb.conjunction();
            return cb.equal(root.get("category"), category);
        };
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, cb) -> {
            if (type == null) return cb.conjunction();
            return cb.equal(root.get("type"), type);
        };
    }

    public static Specification<Transaction> hasStatus(TransactionStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Transaction> hasSourceSystem(String sourceSystem) {
        return (root, query, cb) -> {
            if (sourceSystem == null || sourceSystem.isBlank()) return cb.conjunction();
            return cb.equal(root.get("sourceSystem"), sourceSystem);
        };
    }

    public static Specification<Transaction> dateAfter(LocalDateTime from) {
        return (root, query, cb) -> {
            if (from == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("transactionDate"), from);
        };
    }

    public static Specification<Transaction> dateBefore(LocalDateTime to) {
        return (root, query, cb) -> {
            if (to == null) return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("transactionDate"), to);
        };
    }

    public static Specification<Transaction> amountGreaterThanOrEqual(BigDecimal min) {
        return (root, query, cb) -> {
            if (min == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("amount"), min);
        };
    }

    public static Specification<Transaction> amountLessThanOrEqual(BigDecimal max) {
        return (root, query, cb) -> {
            if (max == null) return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("amount"), max);
        };
    }
}
