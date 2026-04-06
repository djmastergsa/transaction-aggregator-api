package com.capitec.aggregator.service.impl;

import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.service.TransactionCategorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Rule-based categorization engine using keyword matching.
 *
 * Implements the Strategy pattern: each category has its own set of keywords
 * and the matching logic is encapsulated here. The categories are checked in
 * order of priority, and the first match wins.
 */
@Service
public class TransactionCategorizationServiceImpl implements TransactionCategorizationService {

    private static final Logger log = LoggerFactory.getLogger(TransactionCategorizationServiceImpl.class);

    // Ordered list of category keyword rules (priority: first match wins)
    private static final Map<TransactionCategory, List<String>> CATEGORY_KEYWORDS = Map.ofEntries(
            Map.entry(TransactionCategory.SALARY, List.of(
                    "salary", "payroll", "remuneration", "wage", "stipend", "pay slip"
            )),
            Map.entry(TransactionCategory.GROCERIES, List.of(
                    "woolworths food", "pick n pay", "checkers", "spar", "food lovers",
                    "freshstop", "fruit & veg", "shoprite", "boxer superstore", "sixty60"
            )),
            Map.entry(TransactionCategory.UTILITIES, List.of(
                    "eskom", "city power", "telkom", "vodacom", "mtn", "rain broadband", "rain mobile", "fibre",
                    "electricity", "water bill", "municipality", "prepaid electricity",
                    "airtime", "data bundle", "adsl", "internet monthly", "contract bill"
            )),
            Map.entry(TransactionCategory.ENTERTAINMENT, List.of(
                    "netflix", "showmax", "dstv", "spotify", "apple music", "cinemas",
                    "ster-kinekor", "sterkinekor", "nu metro", "movies", "gaming",
                    "playstation", "xbox", "steam"
            )),
            Map.entry(TransactionCategory.TRANSPORT, List.of(
                    "uber", "bolt", "shell", "engen", "bp", "caltex", "garage", "fuel",
                    "gautrain", "petrol", "gas station", "toll gate", "e-toll", "taxi"
            )),
            Map.entry(TransactionCategory.DINING, List.of(
                    "restaurant", "kfc", "mcdonalds", "mcdonald", "steers", "wimpy",
                    "nando", "spur", "ocean basket", "coffee", "cafe", "uber eats",
                    "mr d food", "mug & bean", "mugg & bean", "debonairs", "pizza",
                    "fishaways", "roman's", "bootlegger", "vida e caffe", "starbucks",
                    "burger king", "chesa nyama"
            )),
            Map.entry(TransactionCategory.HEALTHCARE, List.of(
                    "clicks pharmacy", "dischem", "dis-chem", "hospital", "mediclinic",
                    "netcare", "doctor", "medical aid", "dentist", "pharmacy", "clinic",
                    "optometrist", "life healthcare"
            )),
            Map.entry(TransactionCategory.SHOPPING, List.of(
                    "takealot", "amazon", "mr price", "woolworths fashion", "h&m", "zara",
                    "edgars", "foschini", "truworths", "ackermans", "pep stores",
                    "game store", "incredible connection", "hifi corp", "makro",
                    "builders warehouse", "leroy merlin"
            )),
            Map.entry(TransactionCategory.TRANSFER, List.of(
                    "transfer", "eft", "payment to", "sent to", "payshap", "snapscan",
                    "zapper", "received from", "home loan", "vehicle finance", "bond payment",
                    "savings account", "investment"
            ))
    );

    // Priority order for checking categories (DINING before TRANSPORT so "Uber Eats" → DINING, not TRANSPORT)
    private static final List<TransactionCategory> PRIORITY_ORDER = List.of(
            TransactionCategory.SALARY,
            TransactionCategory.GROCERIES,
            TransactionCategory.UTILITIES,
            TransactionCategory.ENTERTAINMENT,
            TransactionCategory.DINING,
            TransactionCategory.TRANSPORT,
            TransactionCategory.HEALTHCARE,
            TransactionCategory.SHOPPING,
            TransactionCategory.TRANSFER
    );

    @Override
    public TransactionCategory categorize(String description, String merchant) {
        String combined = buildSearchText(description, merchant);

        if (combined.isEmpty()) {
            log.debug("Empty description and merchant - defaulting to OTHER");
            return TransactionCategory.OTHER;
        }

        for (TransactionCategory category : PRIORITY_ORDER) {
            List<String> keywords = CATEGORY_KEYWORDS.get(category);
            if (keywords != null && matchesAny(combined, keywords)) {
                log.debug("Categorized '{}' as {} based on keyword match", combined, category);
                return category;
            }
        }

        log.debug("No keyword match found for '{}' - defaulting to OTHER", combined);
        return TransactionCategory.OTHER;
    }

    private String buildSearchText(String description, String merchant) {
        StringBuilder sb = new StringBuilder();
        if (description != null && !description.isBlank()) {
            sb.append(description.toLowerCase().trim());
        }
        if (merchant != null && !merchant.isBlank()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(merchant.toLowerCase().trim());
        }
        return sb.toString();
    }

    private boolean matchesAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(keyword -> text.contains(keyword.toLowerCase()));
    }
}
