package com.capitec.aggregator.domain.entity;

import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.domain.enums.TransactionStatus;
import com.capitec.aggregator.domain.enums.TransactionType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_ref", columnList = "transactionRef", unique = true),
        @Index(name = "idx_transaction_customer", columnList = "customer_id"),
        @Index(name = "idx_transaction_date", columnList = "transactionDate"),
        @Index(name = "idx_transaction_category", columnList = "category"),
        @Index(name = "idx_transaction_type", columnList = "type"),
        @Index(name = "idx_transaction_source", columnList = "sourceSystem")
})
public class Transaction {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "transaction_ref", unique = true, nullable = false, length = 50)
    private String transactionRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private TransactionCategory category;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "merchant", length = 200)
    private String merchant;

    @Column(name = "source_system", nullable = false, length = 30)
    private String sourceSystem;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @CreationTimestamp
    @Column(name = "processed_at", updatable = false)
    private LocalDateTime processedAt;

    @Column(name = "currency", length = 3)
    private String currency = "ZAR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private TransactionStatus status = TransactionStatus.PROCESSED;

    public Transaction() {}

    public Transaction(UUID id, String transactionRef, Customer customer, BigDecimal amount,
                       TransactionType type, TransactionCategory category, String description,
                       String merchant, String sourceSystem, LocalDateTime transactionDate,
                       LocalDateTime processedAt, String currency, TransactionStatus status) {
        this.id = id;
        this.transactionRef = transactionRef;
        this.customer = customer;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.description = description;
        this.merchant = merchant;
        this.sourceSystem = sourceSystem;
        this.transactionDate = transactionDate;
        this.processedAt = processedAt;
        this.currency = currency != null ? currency : "ZAR";
        this.status = status != null ? status : TransactionStatus.PROCESSED;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public TransactionCategory getCategory() { return category; }
    public void setCategory(TransactionCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }

    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionRef, that.transactionRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionRef);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", transactionRef='" + transactionRef + '\'' +
                ", amount=" + amount +
                ", type=" + type +
                ", category=" + category +
                ", sourceSystem='" + sourceSystem + '\'' +
                ", transactionDate=" + transactionDate +
                '}';
    }
}
