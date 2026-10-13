package com.dxc.sooraj.coding_kata.services;

import com.dxc.sooraj.coding_kata.entities.Accounts;
import com.dxc.sooraj.coding_kata.entities.LedgerEntry;
import com.dxc.sooraj.coding_kata.exceptions.AccountNotFoundException;
import com.dxc.sooraj.coding_kata.repositories.AccountRepository;
import com.dxc.sooraj.coding_kata.repositories.LedgerEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;

    private final LedgerEntryRepository ledgerEntryRepository;

    public AccountService(AccountRepository accountRepository,
                          LedgerEntryRepository ledgerEntryRepository) {
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public Accounts createAccount(BigDecimal initialBalance) {
        // Balance cannot be negative when account is created.
        BigDecimal openingBalance = MoneyValidator.requireNonNegative(initialBalance, "Initial balance");

        Accounts account = new Accounts();
        account.setBalance(openingBalance);
        account.setCreatedAt(Instant.now());

        Accounts savedAccount = accountRepository.save(account);
        log.info("New account created. accountId={}", savedAccount.getId());

        return savedAccount;
    }

    public Accounts getAccount(UUID accountId) {
        validateAccountId(accountId);

        return accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.warn("Account not found. accountId={}", accountId);
                    return new AccountNotFoundException(accountId);
                });
    }

    public BigDecimal getBalance(UUID accountId) {
        Accounts account = getAccount(accountId);
        log.debug("Balance checked. accountId={}", accountId);
        return account.getBalance();
    }

    public List<LedgerEntry> getTransactionHistory(UUID accountId) {
        // Check account first, otherwise empty list can be confusing.
        getAccount(accountId);
        List<LedgerEntry> history = ledgerEntryRepository
                .findByAccount_IdOrderByOccurredAtDesc(accountId);

        log.debug("Transaction history checked. accountId={}, entryCount={}",
                accountId, history.size());
        return history;
    }

    private void validateAccountId(UUID accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID is required");
        }
    }
}
