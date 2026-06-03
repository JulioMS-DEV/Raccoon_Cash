package com.raccooncash.api.report;

import com.raccooncash.api.exception.BadRequestException;
import com.raccooncash.api.transaction.Transaction;
import com.raccooncash.api.transaction.TransactionRepository;
import com.raccooncash.api.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(int year, int month) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("El mes debe estar entre 1 y 12");
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();

        BigDecimal income = sumByType(TransactionType.INCOME, from, to);
        BigDecimal expense = sumByType(TransactionType.EXPENSE, from, to);

        return new MonthlyReportResponse(year, month, income, expense);
    }

    @Transactional(readOnly = true)
    public List<CategoryReportResponse> getByCategory(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        List<Transaction> expenses = transactionRepository.findWithFilters(
                null,
                null,
                TransactionType.EXPENSE,
                startOfDay(from),
                endOfDay(to));

        Map<Long, CategoryReportResponse> reportMap = new LinkedHashMap<>();
        for (Transaction transaction : expenses) {
            Long categoryId = transaction.getCategory() != null ? transaction.getCategory().getId() : null;
            String categoryName = transaction.getCategory() != null ? transaction.getCategory().getName() : "Sin categoria";
            CategoryReportResponse report = reportMap.computeIfAbsent(categoryId,
                    id -> new CategoryReportResponse(categoryId, categoryName));
            report.addExpense(transaction.getAmount());
        }

        return new ArrayList<>(reportMap.values());
    }

    @Transactional(readOnly = true)
    public List<AccountReportResponse> getByAccount(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        List<Transaction> transactions = transactionRepository.findWithFilters(
                null,
                null,
                null,
                startOfDay(from),
                endOfDay(to));

        Map<Long, AccountReportResponse> reportMap = new LinkedHashMap<>();
        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                AccountReportResponse report = accountReport(reportMap, transaction.getAccount().getId(), transaction.getAccount().getName());
                report.addIncome(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                AccountReportResponse report = accountReport(reportMap, transaction.getAccount().getId(), transaction.getAccount().getName());
                report.addExpense(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.TRANSFER) {
                AccountReportResponse source = accountReport(reportMap, transaction.getAccount().getId(), transaction.getAccount().getName());
                source.addTransferOut(transaction.getAmount());

                if (transaction.getToAccount() != null) {
                    AccountReportResponse destination = accountReport(reportMap, transaction.getToAccount().getId(), transaction.getToAccount().getName());
                    destination.addTransferIn(transaction.getAmount());
                }
            }
        }

        return new ArrayList<>(reportMap.values());
    }

    @Transactional(readOnly = true)
    public CashFlowReportResponse getCashFlow(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        BigDecimal income = sumByType(TransactionType.INCOME, from, to);
        BigDecimal expense = sumByType(TransactionType.EXPENSE, from, to);
        return new CashFlowReportResponse(from, to, income, expense);
    }

    private AccountReportResponse accountReport(Map<Long, AccountReportResponse> reportMap, Long accountId, String accountName) {
        return reportMap.computeIfAbsent(accountId, id -> new AccountReportResponse(accountId, accountName));
    }

    private BigDecimal sumByType(TransactionType type, LocalDate from, LocalDate to) {
        return transactionRepository.findWithFilters(null, null, type, startOfDay(from), endOfDay(to))
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BadRequestException("Las fechas from y to son obligatorias");
        }
        if (to.isBefore(from)) {
            throw new BadRequestException("La fecha final no puede ser anterior a la fecha inicial");
        }
    }

    private LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }
}