package com.raccooncash.api.transaccion;

import com.raccooncash.api.cuenta.Cuenta;
import com.raccooncash.api.cuenta.CuentaRepositorio;
import com.raccooncash.api.categoria.Categoria;
import com.raccooncash.api.categoria.CategoriaRepositorio;
import com.raccooncash.api.categoria.TipoCategoria;
import com.raccooncash.api.deuda.Deuda;
import com.raccooncash.api.deuda.EstadoDeuda;
import com.raccooncash.api.deuda.TipoDeuda;
import com.raccooncash.api.excepcion.SolicitudIncorrectaException;
import com.raccooncash.api.excepcion.RecursoNoEncontradoException;
import com.raccooncash.api.presupuesto.Presupuesto;
import com.raccooncash.api.presupuesto.PresupuestoRepositorio;
import com.raccooncash.api.savinggoal.SavingGoal;
import com.raccooncash.api.savinggoal.SavingGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransaccionServicio {

    private final TransaccionRepositorio transactionRepository;
    private final CuentaRepositorio accountRepository;
    private final CategoriaRepositorio categoryRepository;
    private final SavingGoalRepository savingGoalRepository;
    private final PresupuestoRepositorio budgetRepository;

    public TransaccionServicio(TransaccionRepositorio transactionRepository,
                               CuentaRepositorio accountRepository,
                               CategoriaRepositorio categoryRepository,
                               SavingGoalRepository savingGoalRepository,
                               PresupuestoRepositorio budgetRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.savingGoalRepository = savingGoalRepository;
        this.budgetRepository = budgetRepository;
    }

    @Transactional(readOnly = true)
    public List<TransaccionRespuesta> getTransactions(Long accountId,
                                                     Long categoryId,
                                                     TipoTransaccion type,
                                                     LocalDate from,
                                                     LocalDate to) {
        LocalDateTime fromDate = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDate = to != null ? to.atTime(LocalTime.MAX) : null;

        return transactionRepository.findWithFilters(accountId, categoryId, type, fromDate, toDate)
                .stream()
                .map(TransaccionRespuesta::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransaccionRespuesta getTransactionById(Long id) {
        Transaccion transaction = findActiveTransaction(id);
        return new TransaccionRespuesta(transaction);
    }

    @Transactional
    public TransaccionRespuesta createTransaction(TransaccionSolicitud request) {
        Transaccion transaction = buildTransactionFromRequest(new Transaccion(), request);
        applyTransactionEffect(transaction);

        saveAccountsFor(transaction);
        Transaccion savedTransaction = transactionRepository.save(transaction);
        return new TransaccionRespuesta(savedTransaction);
    }

    @Transactional
    public TransaccionRespuesta updateTransaction(Long id, TransaccionSolicitud request) {
        Transaccion transaction = findActiveTransaction(id);
        boolean initialDebtTransaction = isInitialDebtTransaction(transaction);
        ensureTransactionCanBeManagedDirectly(transaction);
        if (initialDebtTransaction) {
            validateInitialDebtTransactionUpdate(transaction, request);
        }

        revertTransactionEffect(transaction);
        saveAccountsFor(transaction);

        buildTransactionFromRequest(transaction, request);
        applyTransactionEffect(transaction);
        saveAccountsFor(transaction);
        if (initialDebtTransaction) {
            syncDebtFromInitialTransaction(transaction);
        }

        Transaccion updatedTransaction = transactionRepository.save(transaction);
        return new TransaccionRespuesta(updatedTransaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaccion transaction = findActiveTransaction(id);
        if (isInitialDebtTransaction(transaction)) {
            ensureInitialDebtTransactionCanBeDeleted(transaction);
            revertTransactionEffect(transaction);
            cancelDebt(transaction.getDebt());
            transaction.setActive(false);

            saveAccountsFor(transaction);
            transactionRepository.save(transaction);
            return;
        }

        ensureTransactionCanBeManagedDirectly(transaction);
        revertTransactionEffect(transaction);
        transaction.setActive(false);

        saveAccountsFor(transaction);
        transactionRepository.save(transaction);
    }

    private void ensureTransactionCanBeManagedDirectly(Transaccion transaction) {
        if (Boolean.TRUE.equals(transaction.getGeneratedByDebtPayment())) {
            throw new SolicitudIncorrectaException("Las transacciones generadas por pagos de deuda se gestionan desde el modulo de deudas");
        }
    }

    private boolean isInitialDebtTransaction(Transaccion transaction) {
        return transaction.getDebt() != null && !Boolean.TRUE.equals(transaction.getGeneratedByDebtPayment());
    }

    private void validateInitialDebtTransactionUpdate(Transaccion transaction, TransaccionSolicitud request) {
        validateRequestBasics(request);
        if (request.getType() == TipoTransaccion.TRANSFER) {
            throw new SolicitudIncorrectaException("La transaccion inicial de una deuda no puede convertirse en transferencia");
        }
        if (request.getSavingGoalId() != null) {
            throw new SolicitudIncorrectaException("La transaccion inicial de una deuda no puede asociarse a una meta de ahorro");
        }

        Deuda debt = transaction.getDebt();
        BigDecimal paidAmount = money(debt.getPaidAmount());
        BigDecimal newAmount = money(request.getAmount());
        if (newAmount.compareTo(paidAmount) < 0) {
            throw new SolicitudIncorrectaException("El monto de la deuda no puede ser menor que lo ya pagado");
        }
        if (debt.getType() != debtTypeForInitialTransaction(request.getType())
                && paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            throw new SolicitudIncorrectaException("No se puede cambiar el tipo de deuda porque ya tiene pagos registrados");
        }
    }

    private void ensureInitialDebtTransactionCanBeDeleted(Transaccion transaction) {
        if (money(transaction.getDebt().getPaidAmount()).compareTo(BigDecimal.ZERO) > 0) {
            throw new SolicitudIncorrectaException("No se puede eliminar la transaccion inicial porque la deuda ya tiene pagos registrados");
        }
    }

    private void syncDebtFromInitialTransaction(Transaccion transaction) {
        Deuda debt = transaction.getDebt();
        BigDecimal totalAmount = money(transaction.getAmount());
        BigDecimal paidAmount = money(debt.getPaidAmount());

        debt.setAccount(transaction.getAccount());
        debt.setTotalAmount(totalAmount);
        debt.setPaidAmount(paidAmount);
        debt.setRemainingAmount(money(totalAmount.subtract(paidAmount)));
        debt.setType(debtTypeForInitialTransaction(transaction.getType()));
        updateDebtStatus(debt);
    }

    private void cancelDebt(Deuda debt) {
        debt.setActive(false);
        debt.setStatus(EstadoDeuda.CANCELLED);
        debt.setReminderEnabled(false);
        debt.setReminderAt(null);
    }

    private TipoDeuda debtTypeForInitialTransaction(TipoTransaccion transactionType) {
        if (transactionType == TipoTransaccion.INCOME) {
            return TipoDeuda.I_OWE;
        }
        if (transactionType == TipoTransaccion.EXPENSE) {
            return TipoDeuda.OWED_TO_ME;
        }
        throw new SolicitudIncorrectaException("La transaccion inicial de una deuda debe ser ingreso o gasto");
    }

    private void updateDebtStatus(Deuda debt) {
        if (money(debt.getRemainingAmount()).compareTo(BigDecimal.ZERO) == 0) {
            debt.setStatus(EstadoDeuda.PAID);
            return;
        }

        if (money(debt.getPaidAmount()).compareTo(BigDecimal.ZERO) > 0) {
            debt.setStatus(EstadoDeuda.PARTIALLY_PAID);
            return;
        }

        debt.setStatus(EstadoDeuda.PENDING);
    }

    private Transaccion buildTransactionFromRequest(Transaccion transaction, TransaccionSolicitud request) {
        validateRequestBasics(request);

        Cuenta account = findActiveAccount(request.getAccountId(), "Cuenta no encontrada");
        Categoria category = null;
        Cuenta destinationAccount = null;
        SavingGoal savingGoal = null;
        Presupuesto budget = null;

        if (request.getBudgetId() != null) {
            budget = budgetRepository.findByIdAndActiveTrue(request.getBudgetId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Presupuesto no encontrado"));
        }

        if (request.getSavingGoalId() != null) {
            savingGoal = savingGoalRepository.findById(request.getSavingGoalId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("SavingGoal no encontrada"));
        } else if (request.getType() == TipoTransaccion.INCOME || request.getType() == TipoTransaccion.EXPENSE) {
            if (request.getCategoryId() == null) {
                throw new SolicitudIncorrectaException("La categoria es obligatoria para ingresos y gastos");
            }
            category = findActiveCategory(request.getCategoryId());
            validateCategoryType(request.getType(), category);
        }

        if (request.getType() == TipoTransaccion.TRANSFER && savingGoal == null) {
            Long destinationAccountId = request.getResolvedDestinationAccountId();
            if (destinationAccountId == null) {
                throw new SolicitudIncorrectaException("La cuenta destino es obligatoria para transferencias");
            }
            if (request.getAccountId().equals(destinationAccountId)) {
                throw new SolicitudIncorrectaException("La cuenta origen y destino no pueden ser la misma");
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
        transaction.setSavingGoal(savingGoal);
        transaction.setBudget(budget);
        transaction.setNotes(request.getNotes());
        transaction.setActive(true);

        return transaction;
    }

    private void validateRequestBasics(TransaccionSolicitud request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SolicitudIncorrectaException("El monto debe ser mayor a cero");
        }
        if (request.getType() == null) {
            throw new SolicitudIncorrectaException("El tipo de transaccion es obligatorio");
        }
        if (request.getAccountId() == null) {
            throw new SolicitudIncorrectaException("La cuenta es obligatoria");
        }
    }

    private void validateCategoryType(TipoTransaccion transactionType, Categoria category) {
        if (transactionType == TipoTransaccion.INCOME && category.getType() != TipoCategoria.INCOME) {
            throw new SolicitudIncorrectaException("La categoria debe ser de tipo INCOME");
        }
        if (transactionType == TipoTransaccion.EXPENSE && category.getType() != TipoCategoria.EXPENSE) {
            throw new SolicitudIncorrectaException("La categoria debe ser de tipo EXPENSE");
        }
    }

    private void applyTransactionEffect(Transaccion transaction) {
        if (transaction.getSavingGoal() != null) {
            ensureSufficientBalance(transaction.getAccount(), transaction.getAmount());
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).subtract(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TipoTransaccion.INCOME) {
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).add(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TipoTransaccion.EXPENSE) {
            ensureSufficientBalance(transaction.getAccount(), transaction.getAmount());
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).subtract(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TipoTransaccion.TRANSFER) {
            ensureSufficientBalance(transaction.getAccount(), transaction.getAmount());
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).subtract(transaction.getAmount()));
            transaction.getToAccount().setCurrentBalance(currentBalance(transaction.getToAccount()).add(transaction.getAmount()));
        }
    }

    private void revertTransactionEffect(Transaccion transaction) {
        if (transaction.getSavingGoal() != null) {
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).add(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TipoTransaccion.INCOME) {
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).subtract(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TipoTransaccion.EXPENSE) {
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).add(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TipoTransaccion.TRANSFER) {
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).add(transaction.getAmount()));
            if (transaction.getToAccount() != null) {
                transaction.getToAccount().setCurrentBalance(currentBalance(transaction.getToAccount()).subtract(transaction.getAmount()));
            }
        }
    }

    private void ensureSufficientBalance(Cuenta account, BigDecimal amount) {
        if (currentBalance(account).compareTo(amount) < 0) {
            throw new SolicitudIncorrectaException("Saldo insuficiente en la cuenta " + account.getName());
        }
    }

    private BigDecimal currentBalance(Cuenta account) {
        return account.getCurrentBalance() != null ? account.getCurrentBalance() : BigDecimal.ZERO;
    }

    private BigDecimal money(BigDecimal amount) {
        BigDecimal safeAmount = amount != null ? amount : BigDecimal.ZERO;
        return safeAmount.setScale(2, RoundingMode.HALF_UP);
    }

    private void saveAccountsFor(Transaccion transaction) {
        accountRepository.save(transaction.getAccount());
        if (transaction.getToAccount() != null) {
            accountRepository.save(transaction.getToAccount());
        }
    }

    private Transaccion findActiveTransaction(Long id) {
        return transactionRepository.findActiveById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Transaccion no encontrada"));
    }

    private Cuenta findActiveAccount(Long id, String message) {
        return accountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(message));
    }

    private Categoria findActiveCategory(Long id) {
        return categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<TransaccionRespuesta> getTransactionsBySavingGoalId(Long savingGoalId) {
        SavingGoal savingGoal = savingGoalRepository.findById(savingGoalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("SavingGoal no encontrada"));

        return transactionRepository.findBySavingGoalAndActiveTrue(savingGoal).stream()
                .map(TransaccionRespuesta::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransaccionRespuesta> getTransactionsByBudgetId(Long budgetId) {
        Presupuesto budget = budgetRepository.findByIdAndActiveTrue(budgetId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Presupuesto no encontrado"));

        return transactionRepository.findByBudgetAndActive(budget).stream()
                .map(TransaccionRespuesta::new)
                .collect(Collectors.toList());
    }
}
