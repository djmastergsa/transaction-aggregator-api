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
 * Adapter for the bank data source.
 * Generates ~50 realistic South African bank transactions per customer
 * for customers CUST001-CUST005 using deterministic seed data.
 */
@Component
public class BankDataSourceAdapter implements DataSourceAdapter {

    private static final Logger log = LoggerFactory.getLogger(BankDataSourceAdapter.class);

    private static final String SOURCE = "BANK";
    private static final String[] CUSTOMER_IDS = {"CUST001", "CUST002", "CUST003", "CUST004", "CUST005"};

    @Override
    public String getSourceName() {
        return SOURCE;
    }

    @Override
    public List<RawTransaction> fetchTransactions() {
        log.info("Fetching transactions from Bank data source");
        List<RawTransaction> all = new ArrayList<>();
        for (String customerId : CUSTOMER_IDS) {
            all.addAll(generateForCustomer(customerId));
        }
        log.info("Bank data source returned {} transactions", all.size());
        return all;
    }

    @Override
    public List<RawTransaction> fetchTransactionsForCustomer(String customerId) {
        return generateForCustomer(customerId);
    }

    private List<RawTransaction> generateForCustomer(String customerId) {
        List<RawTransaction> txns = new ArrayList<>();
        LocalDateTime base = LocalDateTime.now().minusMonths(6).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        int seq = customerSeq(customerId);

        // Month 1 - Salary + expenses
        txns.add(build(customerId, 1, base.plusDays(0).plusHours(8),  "SAL", "Monthly Salary - " + getEmployer(seq), "EMPLOYER PAYROLL", new BigDecimal(getSalary(seq)), TransactionType.CREDIT, TransactionCategory.SALARY));
        txns.add(build(customerId, 2, base.plusDays(1).plusHours(10), "GRO", "Woolworths Food Market", "Woolworths", new BigDecimal("1250.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 3, base.plusDays(2).plusHours(9),  "UTL", "Eskom Prepaid Electricity", "Eskom", new BigDecimal("500.00"), TransactionType.DEBIT, TransactionCategory.UTILITIES));
        txns.add(build(customerId, 4, base.plusDays(3).plusHours(11), "UTL", "Vodacom Airtime", "Vodacom", new BigDecimal("200.00"), TransactionType.DEBIT, TransactionCategory.UTILITIES));
        txns.add(build(customerId, 5, base.plusDays(4).plusHours(13), "GRO", "Checkers Hypermarket Purchase", "Checkers", new BigDecimal("890.50"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 6, base.plusDays(5).plusHours(15), "TRF", "EFT Transfer to Savings", "FNB Savings", new BigDecimal("3000.00"), TransactionType.DEBIT, TransactionCategory.TRANSFER));
        txns.add(build(customerId, 7, base.plusDays(7).plusHours(8),  "GRO", "Pick n Pay Supermarket", "Pick n Pay", new BigDecimal("620.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 8, base.plusDays(8).plusHours(12), "UTL", "City of Tshwane Water Bill", "City Power", new BigDecimal("340.00"), TransactionType.DEBIT, TransactionCategory.UTILITIES));
        txns.add(build(customerId, 9, base.plusDays(10).plusHours(9), "TRP", "Gautrain Monthly Card Top-up", "Gautrain", new BigDecimal("750.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));
        txns.add(build(customerId, 10, base.plusDays(12).plusHours(7), "GRO", "SPAR Supermarket", "SPAR", new BigDecimal("450.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));

        // Month 1 continued
        txns.add(build(customerId, 11, base.plusDays(14).plusHours(11), "UTL", "MTN Data Bundle Purchase", "MTN", new BigDecimal("149.00"), TransactionType.DEBIT, TransactionCategory.UTILITIES));
        txns.add(build(customerId, 12, base.plusDays(15).plusHours(14), "MED", "Clicks Pharmacy Purchase", "Clicks Pharmacy", new BigDecimal("320.00"), TransactionType.DEBIT, TransactionCategory.HEALTHCARE));
        txns.add(build(customerId, 13, base.plusDays(16).plusHours(10), "GRO", "Food Lovers Market", "Food Lovers Market", new BigDecimal("280.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 14, base.plusDays(18).plusHours(16), "TRP", "Shell Garage Fuel", "Shell", new BigDecimal("850.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));
        txns.add(build(customerId, 15, base.plusDays(20).plusHours(9),  "TRF", "Payment to Credit Card", "Nedbank CC", new BigDecimal("2000.00"), TransactionType.DEBIT, TransactionCategory.TRANSFER));
        txns.add(build(customerId, 16, base.plusDays(22).plusHours(8),  "GRO", "Woolworths Supermarket", "Woolworths", new BigDecimal("1100.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 17, base.plusDays(24).plusHours(12), "UTL", "Rain Fibre Internet Monthly", "Rain", new BigDecimal("699.00"), TransactionType.DEBIT, TransactionCategory.UTILITIES));
        txns.add(build(customerId, 18, base.plusDays(25).plusHours(15), "TRP", "Engen Petrol Station", "Engen", new BigDecimal("950.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));
        txns.add(build(customerId, 19, base.plusDays(26).plusHours(10), "GRO", "Pick n Pay Express", "Pick n Pay", new BigDecimal("280.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 20, base.plusDays(27).plusHours(9),  "TRF", "EFT Received from Client", "Client Payment", new BigDecimal("5000.00"), TransactionType.CREDIT, TransactionCategory.TRANSFER));

        // Month 2
        txns.add(build(customerId, 21, base.plusMonths(1).plusDays(0).plusHours(8), "SAL", "Monthly Salary - " + getEmployer(seq), "EMPLOYER PAYROLL", new BigDecimal(getSalary(seq)), TransactionType.CREDIT, TransactionCategory.SALARY));
        txns.add(build(customerId, 22, base.plusMonths(1).plusDays(1).plusHours(10), "GRO", "Checkers Supermarket", "Checkers", new BigDecimal("760.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 23, base.plusMonths(1).plusDays(2).plusHours(9),  "UTL", "Eskom Prepaid Electricity", "Eskom", new BigDecimal("500.00"), TransactionType.DEBIT, TransactionCategory.UTILITIES));
        txns.add(build(customerId, 24, base.plusMonths(1).plusDays(3).plusHours(14), "TRP", "BP Petrol Station Fuel", "BP", new BigDecimal("900.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));
        txns.add(build(customerId, 25, base.plusMonths(1).plusDays(5).plusHours(11), "GRO", "Woolworths Food Kloof Street", "Woolworths", new BigDecimal("1380.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 26, base.plusMonths(1).plusDays(7).plusHours(16), "TRF", "EFT Transfer to Home Loan", "ABSA Home Loan", new BigDecimal("8500.00"), TransactionType.DEBIT, TransactionCategory.TRANSFER));
        txns.add(build(customerId, 27, base.plusMonths(1).plusDays(9).plusHours(8),  "UTL", "City Power Electricity Token", "City Power", new BigDecimal("300.00"), TransactionType.DEBIT, TransactionCategory.UTILITIES));
        txns.add(build(customerId, 28, base.plusMonths(1).plusDays(11).plusHours(10),"GRO", "SPAR Convenience Store", "SPAR", new BigDecimal("410.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 29, base.plusMonths(1).plusDays(13).plusHours(15),"MED", "Dischem Pharmacy", "Dischem", new BigDecimal("245.00"), TransactionType.DEBIT, TransactionCategory.HEALTHCARE));
        txns.add(build(customerId, 30, base.plusMonths(1).plusDays(15).plusHours(9), "GRO", "FreshStop at Caltex", "FreshStop", new BigDecimal("180.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));

        // Month 3
        txns.add(build(customerId, 31, base.plusMonths(2).plusDays(0).plusHours(8), "SAL", "Monthly Salary - " + getEmployer(seq), "EMPLOYER PAYROLL", new BigDecimal(getSalary(seq)), TransactionType.CREDIT, TransactionCategory.SALARY));
        txns.add(build(customerId, 32, base.plusMonths(2).plusDays(1).plusHours(10),"GRO", "Pick n Pay Hypermarket", "Pick n Pay", new BigDecimal("1450.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 33, base.plusMonths(2).plusDays(3).plusHours(9), "UTL", "Telkom ADSL Line Rental", "Telkom", new BigDecimal("499.00"), TransactionType.DEBIT, TransactionCategory.UTILITIES));
        txns.add(build(customerId, 34, base.plusMonths(2).plusDays(5).plusHours(14),"TRP", "Shell Garage Fuel Payment", "Shell", new BigDecimal("820.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));
        txns.add(build(customerId, 35, base.plusMonths(2).plusDays(7).plusHours(11),"GRO", "Woolworths Supermarket", "Woolworths", new BigDecimal("1050.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 36, base.plusMonths(2).plusDays(9).plusHours(16),"TRF", "EFT Transfer to Vehicle Finance", "Standard Bank", new BigDecimal("4200.00"), TransactionType.DEBIT, TransactionCategory.TRANSFER));
        txns.add(build(customerId, 37, base.plusMonths(2).plusDays(12).plusHours(10),"GRO","Checkers Sixty60 Delivery", "Checkers", new BigDecimal("390.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 38, base.plusMonths(2).plusDays(15).plusHours(8), "UTL","MTN Contract Bill", "MTN", new BigDecimal("699.00"), TransactionType.DEBIT, TransactionCategory.UTILITIES));
        txns.add(build(customerId, 39, base.plusMonths(2).plusDays(18).plusHours(15),"MED","Mediclinic Medical Aid Contribution", "Mediclinic", new BigDecimal("1800.00"), TransactionType.DEBIT, TransactionCategory.HEALTHCARE));
        txns.add(build(customerId, 40, base.plusMonths(2).plusDays(20).plusHours(9), "TRP","Caltex Garage Fuel", "Caltex", new BigDecimal("780.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));

        // Month 4
        txns.add(build(customerId, 41, base.plusMonths(3).plusDays(0).plusHours(8), "SAL", "Monthly Salary - " + getEmployer(seq), "EMPLOYER PAYROLL", new BigDecimal(getSalary(seq)), TransactionType.CREDIT, TransactionCategory.SALARY));
        txns.add(build(customerId, 42, base.plusMonths(3).plusDays(2).plusHours(10),"GRO","Woolworths Food Market", "Woolworths", new BigDecimal("1100.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 43, base.plusMonths(3).plusDays(4).plusHours(9), "UTL","Eskom Prepaid Token", "Eskom", new BigDecimal("500.00"), TransactionType.DEBIT, TransactionCategory.UTILITIES));
        txns.add(build(customerId, 44, base.plusMonths(3).plusDays(6).plusHours(15),"TRP","BP Petrol Station", "BP", new BigDecimal("870.00"), TransactionType.DEBIT, TransactionCategory.TRANSPORT));
        txns.add(build(customerId, 45, base.plusMonths(3).plusDays(8).plusHours(11),"GRO","Pick n Pay Supermarket", "Pick n Pay", new BigDecimal("540.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 46, base.plusMonths(3).plusDays(10).plusHours(14),"TRF","Monthly Home Loan EFT", "ABSA Home Loan", new BigDecimal("8500.00"), TransactionType.DEBIT, TransactionCategory.TRANSFER));
        txns.add(build(customerId, 47, base.plusMonths(3).plusDays(14).plusHours(10),"GRO","Checkers Supermarket", "Checkers", new BigDecimal("720.00"), TransactionType.DEBIT, TransactionCategory.GROCERIES));
        txns.add(build(customerId, 48, base.plusMonths(3).plusDays(17).plusHours(9), "UTL","Rain Fibre Internet", "Rain", new BigDecimal("699.00"), TransactionType.DEBIT, TransactionCategory.UTILITIES));
        txns.add(build(customerId, 49, base.plusMonths(3).plusDays(20).plusHours(16),"MED","Clicks Pharmacy Prescriptions", "Clicks Pharmacy", new BigDecimal("560.00"), TransactionType.DEBIT, TransactionCategory.HEALTHCARE));
        txns.add(build(customerId, 50, base.plusMonths(3).plusDays(22).plusHours(8), "TRF","EFT Received Rental Income", "Tenant Payment", new BigDecimal("6500.00"), TransactionType.CREDIT, TransactionCategory.TRANSFER));

        return txns;
    }

    private RawTransaction build(String customerId, int seq, LocalDateTime date,
                                  String prefix, String description, String merchant,
                                  BigDecimal amount, TransactionType type, TransactionCategory category) {
        String ref = String.format("BANK-%s-%s-%03d", customerId, prefix, seq);
        return new RawTransaction(ref, customerId, amount, type, category, description, merchant, SOURCE, date, "ZAR", TransactionStatus.PROCESSED);
    }

    private int customerSeq(String customerId) {
        return Integer.parseInt(customerId.replace("CUST", ""));
    }

    private String getEmployer(int seq) {
        return switch (seq) {
            case 1 -> "Capitec Bank";
            case 2 -> "Standard Bank";
            case 3 -> "FNB";
            case 4 -> "Absa Group";
            default -> "Nedbank";
        };
    }

    private String getSalary(int seq) {
        return switch (seq) {
            case 1 -> "45000";
            case 2 -> "38000";
            case 3 -> "52000";
            case 4 -> "29000";
            default -> "61000";
        };
    }
}
