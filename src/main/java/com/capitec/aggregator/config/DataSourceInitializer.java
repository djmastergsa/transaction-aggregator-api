package com.capitec.aggregator.config;

import com.capitec.aggregator.domain.entity.Customer;
import com.capitec.aggregator.repository.CustomerRepository;
import com.capitec.aggregator.service.TransactionAggregatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Initializes the database with seed customers and then triggers
 * a full sync from all mock data source adapters on startup.
 *
 * Seed data uses realistic South African names and contact details.
 */
@Component
public class DataSourceInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSourceInitializer.class);

    private final CustomerRepository customerRepository;
    private final TransactionAggregatorService transactionAggregatorService;

    @Value("${app.data-sources.sync-on-startup:true}")
    private boolean syncOnStartup;

    public DataSourceInitializer(CustomerRepository customerRepository,
                                  TransactionAggregatorService transactionAggregatorService) {
        this.customerRepository = customerRepository;
        this.transactionAggregatorService = transactionAggregatorService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("=== DataSourceInitializer starting ===");
        seedCustomers();

        if (syncOnStartup) {
            log.info("Triggering initial data sync from all sources...");
            int count = transactionAggregatorService.syncAllSources();
            log.info("=== DataSourceInitializer complete. {} transactions loaded. ===", count);
        } else {
            log.info("Sync on startup is disabled (app.data-sources.sync-on-startup=false)");
        }
    }

    private void seedCustomers() {
        List<Customer> mockCustomers = buildMockCustomers();
        int created = 0;
        for (Customer customer : mockCustomers) {
            if (!customerRepository.existsByCustomerId(customer.getCustomerId())) {
                customerRepository.save(customer);
                log.info("Created customer: {} - {} {}", customer.getCustomerId(),
                        customer.getFirstName(), customer.getLastName());
                created++;
            } else {
                log.debug("Customer {} already exists, skipping.", customer.getCustomerId());
            }
        }
        log.info("Customer seeding complete. {} new customers created.", created);
    }

    private List<Customer> buildMockCustomers() {
        Customer c1 = new Customer();
        c1.setCustomerId("CUST001");
        c1.setFirstName("Sipho");
        c1.setLastName("Dlamini");
        c1.setEmail("sipho.dlamini@email.co.za");
        c1.setPhone("+27 82 123 4567");

        Customer c2 = new Customer();
        c2.setCustomerId("CUST002");
        c2.setFirstName("Nomvula");
        c2.setLastName("Khumalo");
        c2.setEmail("nomvula.khumalo@gmail.com");
        c2.setPhone("+27 71 234 5678");

        Customer c3 = new Customer();
        c3.setCustomerId("CUST003");
        c3.setFirstName("Thabo");
        c3.setLastName("Molefe");
        c3.setEmail("thabo.molefe@outlook.com");
        c3.setPhone("+27 83 345 6789");

        Customer c4 = new Customer();
        c4.setCustomerId("CUST004");
        c4.setFirstName("Ayanda");
        c4.setLastName("Nkosi");
        c4.setEmail("ayanda.nkosi@webmail.co.za");
        c4.setPhone("+27 76 456 7890");

        Customer c5 = new Customer();
        c5.setCustomerId("CUST005");
        c5.setFirstName("Lerato");
        c5.setLastName("Sithole");
        c5.setEmail("lerato.sithole@yahoo.co.za");
        c5.setPhone("+27 84 567 8901");

        return List.of(c1, c2, c3, c4, c5);
    }
}
