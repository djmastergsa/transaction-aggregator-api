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
 * Adapter for the mobile payment data source.
 * Generates ~20 realistic South African mobile payment transactions per customer
 * (CUST001-CUST005) with a focus on small transfers, transport, and dining.
 */
@Component
public class MobilePaymentDataSourceAdapter implements DataSourceAdapter {

    private static final Logger log = LoggerFactory.getLogger(MobilePaymentDataSourceAdapter.class);

    private static final String SOURCE = "MOBILE_PAYMENT";
    private static final String[] CUSTOMER_IDS = {"CUST001", "CUST002", "CUST003", "CUST004", "CUST005"};

    @Override
    public String getSourceName() {
        return SOURCE;
    }

    @Override
    public List<RawTransaction> fetchTransactions() {
        log.info("Fetching transactions from Mobile Payment data source");
        List<RawTransaction> all = new ArrayList<>();
        for (String customerId : CUSTOMER_IDS) {
            all.addAll(generateForCustomer(customerId));
        }
        log.info("Mobile Payment data source returned {} transactions", all.size());
        return all;
    }

    @Override
    public List<RawTransaction> fetchTransactionsForCustomer(String customerId) {
        return generateForCustomer(customerId);
    }

    private List<RawTransaction> generateForCustomer(String customerId) {
        List<RawTransaction> txns = new ArrayList<>();
        LocalDateTime base = LocalDateTime.now().minusMonths(6).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        // Month 1 - Transport + small transfers + dining
        txns.add(build(customerId, 1, base.plusDays(1).plusHours(8),  "TRP", "Uber Trip - Sandton to Rosebank", "Uber", new BigDecimal("89.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));
        txns.add(build(customerId, 2, base.plusDays(1).plusHours(20), "DIN", "Uber Eats - Nando's Delivery", "Uber Eats", new BigDecimal("245.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 3, base.plusDays(3).plusHours(9),  "TRF", "SnapScan Payment to Friend", "SnapScan", new BigDecimal("350.00"), TransactionType.DEBIT, TransactionCategory.TRANSFER));
        txns.add(build(customerId, 4, base.plusDays(4).plusHours(18), "TRP", "Bolt Taxi Home from Work", "Bolt", new BigDecimal("75.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));
        txns.add(build(customerId, 5, base.plusDays(6).plusHours(12), "DIN", "Uber Eats - KFC Order", "Uber Eats", new BigDecimal("185.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 6, base.plusDays(8).plusHours(7),  "TRP", "Uber Trip - Home to Office", "Uber", new BigDecimal("110.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));
        txns.add(build(customerId, 7, base.plusDays(10).plusHours(15),"TRF", "PayShap Send Money - Split Bill", "PayShap", new BigDecimal("200.00"), TransactionType.DEBIT, TransactionCategory.TRANSFER));
        txns.add(build(customerId, 8, base.plusDays(12).plusHours(19),"DIN", "Mr D Food - Spur Delivery", "Mr D Food", new BigDecimal("310.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 9, base.plusDays(14).plusHours(8), "TRP", "Bolt Taxi to Airport", "Bolt", new BigDecimal("220.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));
        txns.add(build(customerId, 10, base.plusDays(16).plusHours(13),"TRF","Received from Roommate Split", "SnapScan", new BigDecimal("800.00"), TransactionType.CREDIT, TransactionCategory.TRANSFER));

        // Month 2
        txns.add(build(customerId, 11, base.plusMonths(1).plusDays(2).plusHours(8),  "TRP", "Uber Trip - Weekend Outing", "Uber", new BigDecimal("145.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));
        txns.add(build(customerId, 12, base.plusMonths(1).plusDays(3).plusHours(19), "DIN", "Uber Eats - McDonald's Order", "Uber Eats", new BigDecimal("165.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 13, base.plusMonths(1).plusDays(5).plusHours(10), "TRF", "SnapScan - Split Dinner Bill", "SnapScan", new BigDecimal("425.00"), TransactionType.DEBIT, TransactionCategory.TRANSFER));
        txns.add(build(customerId, 14, base.plusMonths(1).plusDays(7).plusHours(17), "TRP", "Bolt Trip - Shopping Centre", "Bolt", new BigDecimal("95.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));
        txns.add(build(customerId, 15, base.plusMonths(1).plusDays(10).plusHours(21),"DIN", "Mr D Food - Pizza Delivery", "Mr D Food", new BigDecimal("280.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 16, base.plusMonths(1).plusDays(12).plusHours(8), "TRP", "Uber Trip - Early Morning Commute", "Uber", new BigDecimal("120.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));
        txns.add(build(customerId, 17, base.plusMonths(1).plusDays(15).plusHours(14),"TRF","PayShap Payment for Goods", "PayShap", new BigDecimal("550.00"), TransactionType.DEBIT, TransactionCategory.TRANSFER));
        txns.add(build(customerId, 18, base.plusMonths(1).plusDays(18).plusHours(18),"DIN","Uber Eats - Steers Burger Delivery", "Uber Eats", new BigDecimal("195.00"), TransactionType.DEBIT, TransactionCategory.DINING));
        txns.add(build(customerId, 19, base.plusMonths(1).plusDays(20).plusHours(9), "TRP","Bolt Airport Shuttle", "Bolt", new BigDecimal("280.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));
        txns.add(build(customerId, 20, base.plusMonths(1).plusDays(22).plusHours(11),"TRF","Received Payment from Client", "PayShap", new BigDecimal("1200.00"), TransactionType.CREDIT, TransactionCategory.TRANSFER));

        return txns;
    }

    private RawTransaction build(String customerId, int seq, LocalDateTime date,
                                  String prefix, String description, String merchant,
                                  BigDecimal amount, TransactionType type, TransactionCategory category) {
        String ref = String.format("MPAY-%s-%s-%03d", customerId, prefix, seq);
        return new RawTransaction(ref, customerId, amount, type, category, description, merchant, SOURCE, date, "ZAR", TransactionStatus.PROCESSED);
    }
}
