package com.dxc.sooraj.coding_kata.services;

import com.dxc.sooraj.coding_kata.entities.Accounts;
import com.dxc.sooraj.coding_kata.entities.BankingTransaction;
import com.dxc.sooraj.coding_kata.entities.LedgerEntry;
import com.dxc.sooraj.coding_kata.enums.EntryType;
import com.dxc.sooraj.coding_kata.enums.TransactionType;
import com.dxc.sooraj.coding_kata.exceptions.AccountNotFoundException;
import com.dxc.sooraj.coding_kata.exceptions.InsufficientFundsException;
import com.dxc.sooraj.coding_kata.repositories.AccountRepository;
import com.dxc.sooraj.coding_kata.repositories.BankingTransactionRepository;
import com.dxc.sooraj.coding_kata.repositories.LedgerEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Handles deposit, withdrawal and transfer.
 *
 * Account uses optimistic locking with version field.
 * If two requests update same account, one request may fail.
 * Failed request will rollback all database changes.
 * Client can retry the failed request.
 */
@Service
@Slf4j
public class TransactionService {

    private final AccountRepository accountRepository;
    private final BankingTransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public TransactionService(AccountRepository accountRepository,
                              BankingTransactionRepository transactionRepository,
                              LedgerEntryRepository ledgerEntryRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public BankingTransaction deposit(UUID accountId, BigDecimal amount) {
        BigDecimal validAmount = MoneyValidator.requirePositive(amount);
        Accounts account = findAccount(accountId);

        BigDecimal newBalance = MoneyValidator.requireNonNegative(
                account.getBalance().add(validAmount), "Balance");
        Instant transactionTime = Instant.now();

        account.setBalance(newBalance);
        BankingTransaction savedTransaction = saveTransaction(
                TransactionType.DEPOSIT, validAmount, null, account, transactionTime);
        saveLedgerEntry(account, savedTransaction, EntryType.CREDIT,
                validAmount, newBalance, transactionTime);

        log.info("Deposit completed. accountId={}, transactionId={}",
                accountId, savedTransaction.getId());
        return savedTransaction;
    }

    @Transactional
    public BankingTransaction withdraw(UUID accountId, BigDecimal amount) {
        BigDecimal validAmount = MoneyValidator.requirePositive(amount);
        Accounts account = findAccount(accountId);
        checkAvailableBalance(account, validAmount);

        BigDecimal newBalance = account.getBalance().subtract(validAmount);
        Instant transactionTime = Instant.now();

        account.setBalance(newBalance);
        BankingTransaction savedTransaction = saveTransaction(
                TransactionType.WITHDRAWAL, validAmount, account, null, transactionTime);
        saveLedgerEntry(account, savedTransaction, EntryType.DEBIT,
                validAmount, newBalance, transactionTime);

        log.info("Withdrawal completed. accountId={}, transactionId={}",
                accountId, savedTransaction.getId());
        return savedTransaction;
    }

    @Transactional
    public BankingTransaction transfer(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount) {
        validateTransferAccounts(sourceAccountId, destinationAccountId);
        BigDecimal validAmount = MoneyValidator.requirePositive(amount);

        Accounts fromAccount = findAccount(sourceAccountId);
        Accounts toAccount = findAccount(destinationAccountId);
        checkAvailableBalance(fromAccount, validAmount);

        BigDecimal newSourceBalance = fromAccount.getBalance().subtract(validAmount);
        BigDecimal newDestinationBalance = MoneyValidator.requireNonNegative(
                toAccount.getBalance().add(validAmount), "Balance");
        Instant transactionTime = Instant.now();

        fromAccount.setBalance(newSourceBalance);
        toAccount.setBalance(newDestinationBalance);

        BankingTransaction savedTransaction = saveTransaction(
                TransactionType.TRANSFER,
                validAmount,
                fromAccount,
                toAccount,
                transactionTime);

        // Transfer needs entry for both accounts.
        saveLedgerEntry(
                fromAccount, savedTransaction, EntryType.DEBIT,
                validAmount, newSourceBalance, transactionTime);
        saveLedgerEntry(
                toAccount, savedTransaction, EntryType.CREDIT,
                validAmount, newDestinationBalance, transactionTime);

        log.info("Transfer completed. sourceAccountId={}, destinationAccountId={}, transactionId={}",
                sourceAccountId, destinationAccountId, savedTransaction.getId());
        return savedTransaction;
    }

    private Accounts findAccount(UUID accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID is required");
        }

        return accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.warn("Account not found for transaction. accountId={}", accountId);
                    return new AccountNotFoundException(accountId);
                });
    }

    private void validateTransferAccounts(UUID sourceAccountId, UUID destinationAccountId) {
        if (sourceAccountId == null || destinationAccountId == null) {
            throw new IllegalArgumentException("Source and destination account IDs are required");
        }

        if (sourceAccountId.equals(destinationAccountId)) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }
    }

    private void checkAvailableBalance(Accounts account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("Transaction rejected because balance is low. accountId={}", account.getId());
            throw new InsufficientFundsException(account.getId(), account.getBalance(), amount);
        }
    }

    private BankingTransaction saveTransaction(
            TransactionType transactionType,
            BigDecimal amount,
            Accounts sourceAccount,
            Accounts destinationAccount,
            Instant transactionTime) {

        BankingTransaction transaction = new BankingTransaction();
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setCreatedAt(transactionTime);

        return transactionRepository.save(transaction);
    }

    private void saveLedgerEntry(
            Accounts account,
            BankingTransaction transaction,
            EntryType entryType,
            BigDecimal amount,
            BigDecimal balanceAfter,
            Instant transactionTime) {

        LedgerEntry ledger = new LedgerEntry();
        ledger.setAccount(account);
        ledger.setTransaction(transaction);
        ledger.setEntryType(entryType);
        ledger.setAmount(amount);
        ledger.setBalanceAfter(balanceAfter);
        ledger.setOccurredAt(transactionTime);

        ledgerEntryRepository.save(ledger);
    }
}
