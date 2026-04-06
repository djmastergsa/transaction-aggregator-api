package com.capitec.aggregator.datasource.impl;

import com.capitec.aggregator.datasource.DataSourceAdapter;
import com.capitec.aggregator.datasource.model.RawTransaction;
import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.domain.enums.TransactionStatus;
import com.capitec.aggregator.domain.enums.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the credit card data source.
 * Generates ~30 realistic South African credit card transactions per customer
 * (CUST001-CUST005) with a focus on dining, entertainment, and shopping.
 */
@Component
public class CreditCardDataSourceAdapter implements DataSourceAdapter {

    private static final Logger log = LoggerFactory.getLogger(CreditCardDataSourceAdapter.class);

    private static final String SOURCE = "CREDIT_CARD";
    private static final String[] CUSTOMER_IDS = {"CUST001", "CUST002", "CUST003", "CUST004", "CUST005"};

    @Override
    public String getSourceName() {
        return SOURCE;
    }

    @Override
    public List<RawTransaction> fetchTransactions() {
        log.info("Fetching transactions from Credit Card data source");
        List<RawTransaction> all = new ArrayList<>();
        for (String customerId : CUSTOMER_IDS) {
            all.addAll(generateForCustomer(customerId));
        }
        log.info("Credit Card data source returned {} transactions", all.size());
        return all;
    }

    @Override
    public List<RawTransaction> fetchTransactionsForCustomer(String customerId) {
        return generateForCustomer(customerId);
    }

    private List<RawTransaction> generateForCustomer(String customerId) {
        List<RawTransaction> txns = new ArrayList<>();
        LocalDateTime base = LocalDateTime.now().minusMonths(6).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        // Month 1 - Entertainment + Dining
        txns.add(build(customerId, 1, base.plusDays(1).plusHours(19), "ENT", "Netflix Monthly Subscription", "Netflix", new BigDecimal("199.00"), TransactionType.DEBIT, TransactionCategory.ENTERTAINMENT));
        txns.add(build(customerId, 2, base.plusDays(2).plusHours(20), "DIN", "Nando's Chicken Sandton", "Nando's", new BigDecimal("340.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 3, base.plusDays(3).plusHours(12), "SHP", "Takealot Online Purchase", "Takealot", new BigDecimal("1250.00"), TransactionType.DEBIT, TransactionCategory.SHOPPING));
        txns.add(build(customerId, 4, base.plusDays(4).plusHours(21), "DIN", "Ocean Basket Restaurant Menlyn", "Ocean Basket", new BigDecimal("780.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 5, base.plusDays(5).plusHours(10), "ENT", "DStv Premium Monthly Bill", "DStv", new BigDecimal("819.00"), TransactionType.DEBIT, TransactionCategory.ENTERTAINMENT));
        txns.add(build(customerId, 6, base.plusDays(6).plusHours(18), "DIN", "Spur Steak Ranch Centurion", "Spur", new BigDecimal("550.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 7, base.plusDays(7).plusHours(14), "SHP", "Mr Price Fashion Clearance", "Mr Price", new BigDecimal("890.00"), TransactionType.DEBIT, TransactionCategory.SHOPPING));
        txns.add(build(customerId, 8, base.plusDays(9).plusHours(16), "ENT", "Showmax Streaming Subscription", "Showmax", new BigDecimal("99.00"), TransactionType.DEBIT, TransactionCategory.ENTERTAINMENT));
        txns.add(build(customerId, 9, base.plusDays(10).plusHours(20), "DIN", "Wimpy Restaurant Rosebank", "Wimpy", new BigDecimal("210.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 10, base.plusDays(12).plusHours(11), "SHP", "H&M Clothing Purchase Sandton City", "H&M", new BigDecimal("1100.00"), TransactionType.DEBIT, TransactionCategory.SHOPPING));

        // Month 2
        txns.add(build(customerId, 11, base.plusMonths(1).plusDays(1).plusHours(19), "ENT", "Netflix Monthly Subscription", "Netflix", new BigDecimal("199.00"), TransactionType.DEBIT, TransactionCategory.ENTERTAINMENT));
        txns.add(build(customerId, 12, base.plusMonths(1).plusDays(2).plusHours(20), "DIN", "KFC Colonel Streetwires", "KFC", new BigDecimal("185.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 13, base.plusMonths(1).plusDays(3).plusHours(12), "SHP", "Zara Clothing Sandton City", "Zara", new BigDecimal("2100.00"), TransactionType.DEBIT, TransactionCategory.SHOPPING));
        txns.add(build(customerId, 14, base.plusMonths(1).plusDays(5).plusHours(15), "ENT", "Ster-Kinekor Cinema Tickets", "Ster-Kinekor", new BigDecimal("320.00"), TransactionType.DEBIT, TransactionCategory.ENTERTAINMENT));
        txns.add(build(customerId, 15, base.plusMonths(1).plusDays(6).plusHours(18), "DIN", "Steers Burger Melrose Arch", "Steers", new BigDecimal("175.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 16, base.plusMonths(1).plusDays(7).plusHours(10), "ENT", "Spotify Premium Subscription", "Spotify", new BigDecimal("79.99"), TransactionType.DEBIT, TransactionCategory.ENTERTAINMENT));
        txns.add(build(customerId, 17, base.plusMonths(1).plusDays(9).plusHours(12), "SHP", "Edgars Fashion Clearance Sale", "Edgars", new BigDecimal("650.00"), TransactionType.DEBIT, TransactionCategory.SHOPPING));
        txns.add(build(customerId, 18, base.plusMonths(1).plusDays(11).plusHours(20),"DIN", "McDonald's Drive Thru Rivonia", "McDonald's", new BigDecimal("195.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 19, base.plusMonths(1).plusDays(13).plusHours(14),"SHP", "Amazon.co.za Order", "Amazon", new BigDecimal("3200.00"), TransactionType.DEBIT, TransactionCategory.SHOPPING));
        txns.add(build(customerId, 20, base.plusMonths(1).plusDays(15).plusHours(19),"DIN", "Nando's Chicken Hatfield", "Nando's", new BigDecimal("295.00"), TransactionType.DEBIT, TransactionCategory.DINING));

        // Month 3
        txns.add(build(customerId, 21, base.plusMonths(2).plusDays(1).plusHours(19), "ENT", "Netflix Monthly Subscription", "Netflix", new BigDecimal("199.00"), TransactionType.DEBIT, TransactionCategory.ENTERTAINMENT));
        txns.add(build(customerId, 22, base.plusMonths(2).plusDays(3).plusHours(13), "SHP", "Takealot Black Friday Deal", "Takealot", new BigDecimal("4500.00"), TransactionType.DEBIT, TransactionCategory.SHOPPING));
        txns.add(build(customerId, 23, base.plusMonths(2).plusDays(5).plusHours(20), "DIN", "Spur Steak Ranch Eastgate", "Spur", new BigDecimal("610.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 24, base.plusMonths(2).plusDays(7).plusHours(10), "ENT", "Apple Music Subscription", "Apple Music", new BigDecimal("79.99"), TransactionType.DEBIT, TransactionCategory.ENTERTAINMENT));
        txns.add(build(customerId, 25, base.plusMonths(2).plusDays(9).plusHours(18), "DIN", "Mug & Bean Coffee Menlyn", "Mug & Bean", new BigDecimal("145.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 26, base.plusMonths(2).plusDays(12).plusHours(11),"SHP", "Woolworths Fashion Online", "Woolworths Fashion", new BigDecimal("1800.00"), TransactionType.DEBIT, TransactionCategory.SHOPPING));
        txns.add(build(customerId, 27, base.plusMonths(2).plusDays(15).plusHours(20),"ENT", "Ster-Kinekor IMAX Movie", "Ster-Kinekor", new BigDecimal("250.00"), TransactionType.DEBIT, TransactionCategory.ENTERTAINMENT));
        txns.add(build(customerId, 28, base.plusMonths(2).plusDays(18).plusHours(12),"SHP", "Mr Price Sport Online", "Mr Price", new BigDecimal("990.00"), TransactionType.DEBIT, TransactionCategory.SHOPPING));
        txns.add(build(customerId, 29, base.plusMonths(2).plusDays(20).plusHours(19),"DIN", "Ocean Basket Tyger Valley", "Ocean Basket", new BigDecimal("820.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 30, base.plusMonths(2).plusDays(22).plusHours(11),"ENT", "DStv Premium Monthly Bill", "DStv", new BigDecimal("819.00"), TransactionType.DEBIT, TransactionCategory.ENTERTAINMENT));

        return txns;
    }

    private RawTransaction build(String customerId, int seq, LocalDateTime date,
                                  String prefix, String description, String merchant,
                                  BigDecimal amount, TransactionType type, TransactionCategory category) {
        String ref = String.format("CC-%s-%s-%03d", customerId, prefix, seq);
        return new RawTransaction(ref, customerId, amount, type, category, description, merchant, SOURCE, date, "ZAR", TransactionStatus.PROCESSED);
    }
}
