package com.raccooncash.api.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<AccountResponse> getAllActiveAccounts() {
        return accountRepository.findAllByActiveTrue()
                .stream()
                .map(AccountResponse::new)
                .collect(Collectors.toList());
    }

    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        return new AccountResponse(account);
    }

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        Account account = new Account();
        account.setName(request.getName());
        account.setType(request.getType());
        account.setInitialBalance(request.getInitialBalance());
        account.setCurrentBalance(request.getInitialBalance());
        account.setCurrency(request.getCurrency());
        account.setColor(request.getColor());
        account.setActive(true);

        Account savedAccount = accountRepository.save(account);
        return new AccountResponse(savedAccount);
    }

    @Transactional
    public AccountResponse updateAccount(Long id, AccountRequest request) {
        Account account = accountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        account.setName(request.getName());
        account.setType(request.getType());
        account.setInitialBalance(request.getInitialBalance());
        account.setCurrency(request.getCurrency());
        account.setColor(request.getColor());

        Account updatedAccount = accountRepository.save(account);
        return new AccountResponse(updatedAccount);
    }

    @Transactional
    public void deleteAccount(Long id) {
        Account account = accountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        account.setActive(false);
        accountRepository.save(account);
    }
}
