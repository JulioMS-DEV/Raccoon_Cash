package com.raccooncash.api.panel;

import com.raccooncash.api.cuenta.Cuenta;
import com.raccooncash.api.cuenta.CuentaRepositorio;
import com.raccooncash.api.presupuesto.PresupuestoRepositorio;
import com.raccooncash.api.deuda.Deuda;
import com.raccooncash.api.deuda.DeudaRepositorio;
import com.raccooncash.api.deuda.EstadoDeuda;
import com.raccooncash.api.deuda.TipoDeuda;
import com.raccooncash.api.transaccion.Transaccion;
import com.raccooncash.api.transaccion.TransaccionRepositorio;
import com.raccooncash.api.transaccion.TipoTransaccion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class PanelServicio {

    private final CuentaRepositorio accountRepository;
    private final TransaccionRepositorio transactionRepository;
    private final DeudaRepositorio debtRepository;
    private final PresupuestoRepositorio budgetRepository;

    public PanelServicio(CuentaRepositorio accountRepository,
                            TransaccionRepositorio transactionRepository,
                            DeudaRepositorio debtRepository,
                            PresupuestoRepositorio budgetRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.debtRepository = debtRepository;
        this.budgetRepository = budgetRepository;
    }

    @Transactional(readOnly = true)
    public ResumenPanelRespuesta getSummary(Long usuarioId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        BigDecimal totalBalance = accountRepository.findAllByUsuarioIdAndActiveTrue(usuarioId)
                .stream()
                .map(this::currentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal income = sumTransactions(usuarioId, TipoTransaccion.INCOME, startOfMonth, endOfMonth);
        BigDecimal expense = sumTransactions(usuarioId, TipoTransaccion.EXPENSE, startOfMonth, endOfMonth);

        List<Deuda> debts = debtRepository.findAllByUsuarioIdAndActiveTrue(usuarioId);
        BigDecimal debtsIOwe = sumDebts(debts, TipoDeuda.I_OWE);
        BigDecimal debtsOwedToMe = sumDebts(debts, TipoDeuda.OWED_TO_ME);

        ResumenPanelRespuesta response = new ResumenPanelRespuesta();
        response.setTotalBalance(totalBalance);
        response.setTotalIncomeThisMonth(income);
        response.setTotalExpenseThisMonth(expense);
        response.setNetCashFlowThisMonth(income.subtract(expense));
        response.setTotalDebtsIOwe(debtsIOwe);
        response.setTotalDebtsOwedToMe(debtsOwedToMe);
        response.setNumberOfAccounts(accountRepository.countByUsuarioIdAndActiveTrue(usuarioId));
        response.setNumberOfActiveBudgets(budgetRepository.countByUsuarioIdAndActiveTrue(usuarioId));
        return response;
    }

    private BigDecimal sumTransactions(Long usuarioId, TipoTransaccion type, LocalDate from, LocalDate to) {
        return transactionRepository.findWithFilters(usuarioId, null, null, type, from.atStartOfDay(), to.atTime(LocalTime.MAX))
                .stream()
                .map(Transaccion::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumDebts(List<Deuda> debts, TipoDeuda type) {
        return debts.stream()
                .filter(debt -> debt.getType() == type)
                .filter(debt -> debt.getStatus() != EstadoDeuda.PAID && debt.getStatus() != EstadoDeuda.CANCELLED)
                .map(Deuda::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal currentBalance(Cuenta account) {
        return account.getCurrentBalance() != null ? account.getCurrentBalance() : BigDecimal.ZERO;
    }
}
