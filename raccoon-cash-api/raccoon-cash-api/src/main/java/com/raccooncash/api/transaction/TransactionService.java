package com.raccooncash.api.transaction;

import com.raccooncash.api.account.Account;
import com.raccooncash.api.account.AccountRepository;
import com.raccooncash.api.category.Category;
import com.raccooncash.api.category.CategoryRepository;
import com.raccooncash.api.category.CategoryType;
import com.raccooncash.api.exception.BadRequestException;
import com.raccooncash.api.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(Long accountId,
                                                     Long categoryId,
                                                     TransactionType type,
                                                     LocalDate from,
                                                     LocalDate to) {
        LocalDateTime fromDate = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDate = to != null ? to.atTime(LocalTime.MAX) : null;

        return transactionRepository.findWithFilters(accountId, categoryId, type, fromDate, toDate)
                .stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = findActiveTransaction(id);
        return new TransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Transaction transaction = buildTransactionFromRequest(new Transaction(), request);
        applyTransactionEffect(transaction);

        saveAccountsFor(transaction);
        Transaction savedTransaction = transactionRepository.save(transaction);
        return new TransactionResponse(savedTransaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Transaction transaction = findActiveTransaction(id);

        revertTransactionEffect(transaction);
        saveAccountsFor(transaction);

        buildTransactionFromRequest(transaction, request);
        applyTransactionEffect(transaction);
        saveAccountsFor(transaction);

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return new TransactionResponse(updatedTransaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = findActiveTransaction(id);
        revertTransactionEffect(transaction);
        transaction.setActive(false);

        saveAccountsFor(transaction);
        transactionRepository.save(transaction);
    }

    private Transaction buildTransactionFromRequest(Transaction transaction, TransactionRequest request) {
        validateRequestBasics(request);

        Account account = findActiveAccount(request.getAccountId(), "Cuenta no encontrada");
        Category category = null;
        Account destinationAccount = null;

        if (request.getType() == TransactionType.INCOME || request.getType() == TransactionType.EXPENSE) {
            if (request.getCategoryId() == null) {
                throw new BadRequestException("La categoria es obligatoria para ingresos y gastos");
            }
            category = findActiveCategory(request.getCategoryId());
            validateCategoryType(request.getType(), category);
        }

        if (request.getType() == TransactionType.TRANSFER) {
            Long destinationAccountId = request.getResolvedDestinationAccountId();
            if (destinationAccountId == null) {
                throw new BadRequestException("La cuenta destino es obligatoria para transferencias");
            }
            if (request.getAccountId().equals(destinationAccountId)) {
                throw new BadRequestException("La cuenta origen y destino no pueden ser la misma");
            }
            destinationAccount = findActiveAccount(destinationAccountId, "Cuenta destino no encontrada");
        }

        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate() != null ? request.getDate() : LocalDateTime.now());
        transaction.setType(request.getType());
        transaction.setAccount(account);
        transaction.setToAccount(destinationAccount);
        transaction.setCategory(category);
        transaction.setNotes(request.getNotes());
        transaction.setActive(true);

        return transaction;
    }

    private void validateRequestBasics(TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto debe ser mayor a cero");
        }
        if (request.getType() == null) {
            throw new BadRequestException("El tipo de transaccion es obligatorio");
        }
        if (request.getAccountId() == null) {
            throw new BadRequestException("La cuenta es obligatoria");
        }
    }

    private void validateCategoryType(TransactionType transactionType, Category category) {
        if (transactionType == TransactionType.INCOME && category.getType() != CategoryType.INCOME) {
            throw new BadRequestException("La categoria debe ser de tipo INCOME");
        }
        if (transactionType == TransactionType.EXPENSE && category.getType() != CategoryType.EXPENSE) {
            throw new BadRequestException("La categoria debe ser de tipo EXPENSE");
        }
    }

    private void applyTransactionEffect(Transaction transaction) {
        if (transaction.getType() == TransactionType.INCOME) {
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).add(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TransactionType.EXPENSE) {
            ensureSufficientBalance(transaction.getAccount(), transaction.getAmount());
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).subtract(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TransactionType.TRANSFER) {
            ensureSufficientBalance(transaction.getAccount(), transaction.getAmount());
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).subtract(transaction.getAmount()));
            transaction.getToAccount().setCurrentBalance(currentBalance(transaction.getToAccount()).add(transaction.getAmount()));
        }
    }

    private void revertTransactionEffect(Transaction transaction) {
        if (transaction.getType() == TransactionType.INCOME) {
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).subtract(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TransactionType.EXPENSE) {
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).add(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TransactionType.TRANSFER) {
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).add(transaction.getAmount()));
            if (transaction.getToAccount() != null) {
                transaction.getToAccount().setCurrentBalance(currentBalance(transaction.getToAccount()).subtract(transaction.getAmount()));
            }
        }
    }

    private void ensureSufficientBalance(Account account, BigDecimal amount) {
        if (currentBalance(account).compareTo(amount) < 0) {
            throw new BadRequestException("Saldo insuficiente");
        }
    }

    private BigDecimal currentBalance(Account account) {
        return account.getCurrentBalance() != null ? account.getCurrentBalance() : BigDecimal.ZERO;
    }

    private void saveAccountsFor(Transaction transaction) {
        accountRepository.save(transaction.getAccount());
        if (transaction.getToAccount() != null) {
            accountRepository.save(transaction.getToAccount());
        }
    }

    private Transaction findActiveTransaction(Long id) {
        return transactionRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaccion no encontrada"));
    }

    private Account findActiveAccount(Long id, String message) {
        return accountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(message));
    }

    private Category findActiveCategory(Long id) {
        return categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));
    }
}