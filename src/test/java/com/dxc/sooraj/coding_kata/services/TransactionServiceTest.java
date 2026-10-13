package com.dxc.sooraj.coding_kata.services;

import com.dxc.sooraj.coding_kata.entities.Accounts;
import com.dxc.sooraj.coding_kata.entities.BankingTransaction;
import com.dxc.sooraj.coding_kata.entities.LedgerEntry;
import com.dxc.sooraj.coding_kata.enums.EntryType;
import com.dxc.sooraj.coding_kata.enums.TransactionType;
import com.dxc.sooraj.coding_kata.exceptions.InsufficientFundsException;
import com.dxc.sooraj.coding_kata.repositories.AccountRepository;
import com.dxc.sooraj.coding_kata.repositories.BankingTransactionRepository;
import com.dxc.sooraj.coding_kata.repositories.LedgerEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BankingTransactionRepository transactionRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                accountRepository, transactionRepository, ledgerEntryRepository);
    }

    @Test
    void depositMoney() {
        Accounts account = account("100.00");
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        returnSavedTransaction();

        BankingTransaction transaction = transactionService.deposit(
                account.getId(), new BigDecimal("50.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("150.00");
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(transaction.getDestinationAccount()).isSameAs(account);

        ArgumentCaptor<LedgerEntry> entryCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerEntryRepository).save(entryCaptor.capture());
        LedgerEntry entry = entryCaptor.getValue();
        assertThat(entry.getEntryType()).isEqualTo(EntryType.CREDIT);
        assertThat(entry.getBalanceAfter()).isEqualByComparingTo("150.00");
    }

    @Test
    void withdrawMoney() {
        Accounts account = account("100.00");
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        returnSavedTransaction();

        BankingTransaction transaction = transactionService.withdraw(
                account.getId(), new BigDecimal("40.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("60.00");
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(transaction.getSourceAccount()).isSameAs(account);

        ArgumentCaptor<LedgerEntry> entryCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerEntryRepository).save(entryCaptor.capture());
        assertThat(entryCaptor.getValue().getEntryType()).isEqualTo(EntryType.DEBIT);
    }

    @Test
    void transferMoney() {
        Accounts source = account("500.00");
        Accounts destination = account("100.00");
        when(accountRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(accountRepository.findById(destination.getId())).thenReturn(Optional.of(destination));
        returnSavedTransaction();

        BankingTransaction transaction = transactionService.transfer(
                source.getId(), destination.getId(), new BigDecimal("125.00"));

        assertThat(source.getBalance()).isEqualByComparingTo("375.00");
        assertThat(destination.getBalance()).isEqualByComparingTo("225.00");
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.TRANSFER);

        ArgumentCaptor<LedgerEntry> entryCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerEntryRepository, org.mockito.Mockito.times(2)).save(entryCaptor.capture());
        List<LedgerEntry> entries = entryCaptor.getAllValues();

        assertThat(entries.get(0).getEntryType()).isEqualTo(EntryType.DEBIT);
        assertThat(entries.get(0).getBalanceAfter()).isEqualByComparingTo("375.00");
        assertThat(entries.get(1).getEntryType()).isEqualTo(EntryType.CREDIT);
        assertThat(entries.get(1).getBalanceAfter()).isEqualByComparingTo("225.00");
        assertThat(entries.get(0).getOccurredAt()).isEqualTo(entries.get(1).getOccurredAt());
    }

    @Test
    void cannotWithdrawMoreThanBalance() {
        Accounts account = account("25.00");
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.withdraw(
                account.getId(), new BigDecimal("30.00")))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(account.getBalance()).isEqualByComparingTo("25.00");
        verify(transactionRepository, never()).save(any());
        verify(ledgerEntryRepository, never()).save(any());
    }

    @Test
    void cannotUseZeroOrNegativeAmount() {
        UUID accountId = UUID.randomUUID();

        assertThatThrownBy(() -> transactionService.deposit(accountId, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> transactionService.withdraw(accountId, new BigDecimal("-1.00")))
                .isInstanceOf(IllegalArgumentException.class);

        verify(accountRepository, never()).findById(any());
    }

    @Test
    void cannotUseMoreThanTwoDecimalPlaces() {
        assertThatThrownBy(() -> transactionService.deposit(
                UUID.randomUUID(), new BigDecimal("10.001")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount can have maximum two decimal places");
    }

    @Test
    void cannotTransferToSameAccount() {
        UUID accountId = UUID.randomUUID();

        assertThatThrownBy(() -> transactionService.transfer(
                accountId, accountId, new BigDecimal("10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Source and destination accounts must be different");

        verify(accountRepository, never()).findById(any());
    }

    private Accounts account(String balance) {
        Accounts account = new Accounts();
        account.setId(UUID.randomUUID());
        account.setBalance(new BigDecimal(balance));
        return account;
    }

    private void returnSavedTransaction() {
        when(transactionRepository.save(any(BankingTransaction.class)))
                .thenAnswer(call -> call.getArgument(0));
    }
}
