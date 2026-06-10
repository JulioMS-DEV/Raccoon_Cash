package com.raccooncash.api.config;

import com.raccooncash.api.account.AccountRepository;
import com.raccooncash.api.budget.BudgetCategoryLimitRepository;
import com.raccooncash.api.budget.BudgetRepository;
import com.raccooncash.api.category.CategoryRepository;
import com.raccooncash.api.debt.DebtPaymentRepository;
import com.raccooncash.api.debt.DebtRepository;
import com.raccooncash.api.transaction.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class StartupDataCleaner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupDataCleaner.class);

    private final boolean resetOnStartup;
    private final DebtPaymentRepository debtPaymentRepository;
    private final DebtRepository debtRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetCategoryLimitRepository budgetCategoryLimitRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;

    public StartupDataCleaner(
            @Value("${raccoon-cash.data.reset-on-startup:false}") boolean resetOnStartup,
            DebtPaymentRepository debtPaymentRepository,
            DebtRepository debtRepository,
            TransactionRepository transactionRepository,
            BudgetCategoryLimitRepository budgetCategoryLimitRepository,
            BudgetRepository budgetRepository,
            CategoryRepository categoryRepository,
            AccountRepository accountRepository) {
        this.resetOnStartup = resetOnStartup;
        this.debtPaymentRepository = debtPaymentRepository;
        this.debtRepository = debtRepository;
        this.transactionRepository = transactionRepository;
        this.budgetCategoryLimitRepository = budgetCategoryLimitRepository;
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!resetOnStartup) {
            return;
        }

        LOGGER.warn("raccoon-cash.data.reset-on-startup=true: deleting all persisted financial data.");

        debtPaymentRepository.deleteAllInBatch();
        debtRepository.deleteAllInBatch();
        transactionRepository.deleteAllInBatch();
        budgetCategoryLimitRepository.deleteAllInBatch();
        budgetRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
    }
}
