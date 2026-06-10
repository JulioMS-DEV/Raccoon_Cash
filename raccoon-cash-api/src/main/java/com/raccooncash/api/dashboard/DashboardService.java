package com.raccooncash.api.dashboard;

import com.raccooncash.api.account.Account;
import com.raccooncash.api.account.AccountRepository;
import com.raccooncash.api.budget.BudgetRepository;
import com.raccooncash.api.debt.Debt;
import com.raccooncash.api.debt.DebtRepository;
import com.raccooncash.api.debt.DebtStatus;
import com.raccooncash.api.debt.DebtType;
import com.raccooncash.api.transaction.Transaction;
import com.raccooncash.api.transaction.TransactionRepository;
import com.raccooncash.api.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final DebtRepository debtRepository;
    private final BudgetRepository budgetRepository;

    public DashboardService(AccountRepository accountRepository,
                            TransactionRepository transactionRepository,
                            DebtRepository debtRepository,
                            BudgetRepository budgetRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.debtRepository = debtRepository;
        this.budgetRepository = budgetRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        BigDecimal totalBalance = accountRepository.findAllByActiveTrue()
                .stream()
                .map(this::currentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal income = sumTransactions(TransactionType.INCOME, startOfMonth, endOfMonth);
        BigDecimal expense = sumTransactions(TransactionType.EXPENSE, startOfMonth, endOfMonth);

        List<Debt> debts = debtRepository.findAllByActiveTrue();
        BigDecimal debtsIOwe = sumDebts(debts, DebtType.I_OWE);
        BigDecimal debtsOwedToMe = sumDebts(debts, DebtType.OWED_TO_ME);

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotalBalance(totalBalance);
        response.setTotalIncomeThisMonth(income);
        response.setTotalExpenseThisMonth(expense);
        response.setNetCashFlowThisMonth(income.subtract(expense));
        response.setTotalDebtsIOwe(debtsIOwe);
        response.setTotalDebtsOwedToMe(debtsOwedToMe);
        response.setNumberOfAccounts(accountRepository.countByActiveTrue());
        response.setNumberOfActiveBudgets(budgetRepository.countByActiveTrue());
        return response;
    }

    private BigDecimal sumTransactions(TransactionType type, LocalDate from, LocalDate to) {
        return transactionRepository.findWithFilters(null, null, type, from.atStartOfDay(), to.atTime(LocalTime.MAX))
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumDebts(List<Debt> debts, DebtType type) {
        return debts.stream()
                .filter(debt -> debt.getType() == type)
                .filter(debt -> debt.getStatus() != DebtStatus.PAID && debt.getStatus() != DebtStatus.CANCELLED)
                .map(Debt::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal currentBalance(Account account) {
        return account.getCurrentBalance() != null ? account.getCurrentBalance() : BigDecimal.ZERO;
    }
}