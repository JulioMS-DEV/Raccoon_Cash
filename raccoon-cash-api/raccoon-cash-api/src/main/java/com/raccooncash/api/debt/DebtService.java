package com.raccooncash.api.debt;

import com.raccooncash.api.account.Account;
import com.raccooncash.api.account.AccountRepository;
import com.raccooncash.api.exception.BadRequestException;
import com.raccooncash.api.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DebtService {

    private final DebtRepository debtRepository;
    private final DebtPaymentRepository paymentRepository;
    private final AccountRepository accountRepository;

    public DebtService(DebtRepository debtRepository,
                       DebtPaymentRepository paymentRepository,
                       AccountRepository accountRepository) {
        this.debtRepository = debtRepository;
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<DebtResponse> getAllDebts() {
        return debtRepository.findAllByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(DebtResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DebtResponse getDebtById(Long id) {
        return new DebtResponse(findActiveDebt(id));
    }

    @Transactional
    public DebtResponse createDebt(DebtRequest request) {
        validateDebtRequest(request);
        Account account = findActiveAccount(request.getAccountId());

        Debt debt = new Debt();
        debt.setPersonName(request.getPersonName());
        debt.setDescription(request.getDescription());
        debt.setTotalAmount(request.getTotalAmount());
        debt.setPaidAmount(BigDecimal.ZERO);
        debt.setRemainingAmount(request.getTotalAmount());
        debt.setType(request.getType());
        debt.setStatus(DebtStatus.PENDING);
        debt.setDueDate(request.getDueDate());
        debt.setAccount(account);
        debt.setActive(true);

        Debt savedDebt = debtRepository.save(debt);
        return new DebtResponse(savedDebt);
    }

    @Transactional
    public DebtResponse updateDebt(Long id, DebtRequest request) {
        validateDebtRequest(request);
        Debt debt = findActiveDebt(id);
        Account account = findActiveAccount(request.getAccountId());

        BigDecimal paidAmount = debt.getPaidAmount() != null ? debt.getPaidAmount() : BigDecimal.ZERO;
        if (request.getTotalAmount().compareTo(paidAmount) < 0) {
            throw new BadRequestException("El monto total no puede ser menor que lo ya pagado");
        }

        debt.setPersonName(request.getPersonName());
        debt.setDescription(request.getDescription());
        debt.setTotalAmount(request.getTotalAmount());
        debt.setPaidAmount(paidAmount);
        debt.setRemainingAmount(request.getTotalAmount().subtract(paidAmount));
        debt.setType(request.getType());
        debt.setDueDate(request.getDueDate());
        debt.setAccount(account);
        updateStatus(debt);

        Debt updatedDebt = debtRepository.save(debt);
        return new DebtResponse(updatedDebt);
    }

    @Transactional
    public void deleteDebt(Long id) {
        Debt debt = findActiveDebt(id);
        debt.setActive(false);
        debt.setStatus(DebtStatus.CANCELLED);
        debtRepository.save(debt);
    }

    @Transactional
    public DebtPaymentResponse addPayment(Long debtId, DebtPaymentRequest request) {
        validatePaymentRequest(request);
        Debt debt = findActiveDebt(debtId);

        if (request.getAmount().compareTo(debt.getRemainingAmount()) > 0) {
            throw new BadRequestException("El pago no puede ser mayor que el monto pendiente");
        }

        DebtPayment payment = new DebtPayment();
        payment.setDebt(debt);
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        payment.setNotes(request.getNotes());

        debt.setPaidAmount(debt.getPaidAmount().add(request.getAmount()));
        debt.setRemainingAmount(debt.getTotalAmount().subtract(debt.getPaidAmount()));
        updateStatus(debt);
        debtRepository.save(debt);

        DebtPayment savedPayment = paymentRepository.save(payment);
        return new DebtPaymentResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public List<DebtPaymentResponse> getPayments(Long debtId) {
        findActiveDebt(debtId);
        return paymentRepository.findAllByDebtIdOrderByPaymentDateDesc(debtId)
                .stream()
                .map(DebtPaymentResponse::new)
                .collect(Collectors.toList());
    }

    private void updateStatus(Debt debt) {
        if (debt.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            debt.setStatus(DebtStatus.PAID);
            return;
        }

        if (debt.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            debt.setStatus(DebtStatus.PARTIALLY_PAID);
            return;
        }

        debt.setStatus(DebtStatus.PENDING);
    }

    private void validateDebtRequest(DebtRequest request) {
        if (request.getTotalAmount() == null || request.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto total debe ser mayor a cero");
        }
        if (request.getType() == null) {
            throw new BadRequestException("El tipo de deuda es obligatorio");
        }
        if (request.getAccountId() == null) {
            throw new BadRequestException("La cuenta es obligatoria");
        }
    }

    private void validatePaymentRequest(DebtPaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El pago debe ser mayor a cero");
        }
    }

    private Debt findActiveDebt(Long id) {
        return debtRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada"));
    }

    private Account findActiveAccount(Long id) {
        return accountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
    }
}