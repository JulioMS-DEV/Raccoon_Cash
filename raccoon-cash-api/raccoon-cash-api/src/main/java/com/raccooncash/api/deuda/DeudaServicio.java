package com.raccooncash.api.deuda;

import com.raccooncash.api.cuenta.Cuenta;
import com.raccooncash.api.cuenta.CuentaRepositorio;
import com.raccooncash.api.excepcion.SolicitudIncorrectaException;
import com.raccooncash.api.excepcion.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeudaServicio {

    private final DeudaRepositorio debtRepository;
    private final PagoDeudaRepositorio paymentRepository;
    private final CuentaRepositorio accountRepository;

    public DeudaServicio(DeudaRepositorio debtRepository,
                       PagoDeudaRepositorio paymentRepository,
                       CuentaRepositorio accountRepository) {
        this.debtRepository = debtRepository;
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<DeudaRespuesta> getAllDebts() {
        return debtRepository.findAllByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(DeudaRespuesta::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeudaRespuesta getDebtById(Long id) {
        return new DeudaRespuesta(findActiveDebt(id));
    }

    @Transactional
    public DeudaRespuesta createDebt(DeudaSolicitud request) {
        validateDebtRequest(request);
        Cuenta account = findActiveAccount(request.getAccountId());

        Deuda debt = new Deuda();
        debt.setPersonName(request.getPersonName());
        debt.setDescription(request.getDescription());
        debt.setTotalAmount(request.getTotalAmount());
        debt.setPaidAmount(BigDecimal.ZERO);
        debt.setRemainingAmount(request.getTotalAmount());
        debt.setType(request.getType());
        debt.setStatus(EstadoDeuda.PENDING);
        debt.setDueDate(request.getDueDate());
        debt.setAccount(account);
        debt.setActive(true);

        Deuda savedDebt = debtRepository.save(debt);
        return new DeudaRespuesta(savedDebt);
    }

    @Transactional
    public DeudaRespuesta updateDebt(Long id, DeudaSolicitud request) {
        validateDebtRequest(request);
        Deuda debt = findActiveDebt(id);
        Cuenta account = findActiveAccount(request.getAccountId());

        BigDecimal paidAmount = debt.getPaidAmount() != null ? debt.getPaidAmount() : BigDecimal.ZERO;
        if (request.getTotalAmount().compareTo(paidAmount) < 0) {
            throw new SolicitudIncorrectaException("El monto total no puede ser menor que lo ya pagado");
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

        Deuda updatedDebt = debtRepository.save(debt);
        return new DeudaRespuesta(updatedDebt);
    }

    @Transactional
    public void deleteDebt(Long id) {
        Deuda debt = findActiveDebt(id);
        debt.setActive(false);
        debt.setStatus(EstadoDeuda.CANCELLED);
        debtRepository.save(debt);
    }

    @Transactional
    public PagoDeudaRespuesta addPayment(Long debtId, PagoDeudaSolicitud request) {
        validatePaymentRequest(request);
        Deuda debt = findActiveDebt(debtId);

        if (request.getAmount().compareTo(debt.getRemainingAmount()) > 0) {
            throw new SolicitudIncorrectaException("El pago no puede ser mayor que el monto pendiente");
        }

        PagoDeuda payment = new PagoDeuda();
        payment.setDebt(debt);
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        payment.setNotes(request.getNotes());

        debt.setPaidAmount(debt.getPaidAmount().add(request.getAmount()));
        debt.setRemainingAmount(debt.getTotalAmount().subtract(debt.getPaidAmount()));
        updateStatus(debt);
        debtRepository.save(debt);

        PagoDeuda savedPayment = paymentRepository.save(payment);
        return new PagoDeudaRespuesta(savedPayment);
    }

    @Transactional(readOnly = true)
    public List<PagoDeudaRespuesta> getPayments(Long debtId) {
        findActiveDebt(debtId);
        return paymentRepository.findAllByDebtIdOrderByPaymentDateDesc(debtId)
                .stream()
                .map(PagoDeudaRespuesta::new)
                .collect(Collectors.toList());
    }

    private void updateStatus(Deuda debt) {
        if (debt.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            debt.setStatus(EstadoDeuda.PAID);
            return;
        }

        if (debt.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            debt.setStatus(EstadoDeuda.PARTIALLY_PAID);
            return;
        }

        debt.setStatus(EstadoDeuda.PENDING);
    }

    private void validateDebtRequest(DeudaSolicitud request) {
        if (request.getTotalAmount() == null || request.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SolicitudIncorrectaException("El monto total debe ser mayor a cero");
        }
        if (request.getType() == null) {
            throw new SolicitudIncorrectaException("El tipo de deuda es obligatorio");
        }
        if (request.getAccountId() == null) {
            throw new SolicitudIncorrectaException("La cuenta es obligatoria");
        }
    }

    private void validatePaymentRequest(PagoDeudaSolicitud request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SolicitudIncorrectaException("El pago debe ser mayor a cero");
        }
    }

    private Deuda findActiveDebt(Long id) {
        return debtRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Deuda no encontrada"));
    }

    private Cuenta findActiveAccount(Long id) {
        return accountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada"));
    }
}