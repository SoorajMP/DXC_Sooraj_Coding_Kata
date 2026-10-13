package com.dxc.sooraj.coding_kata.services;

import com.dxc.sooraj.coding_kata.entities.Accounts;
import com.dxc.sooraj.coding_kata.entities.LedgerEntry;
import com.dxc.sooraj.coding_kata.exceptions.AccountNotFoundException;
import com.dxc.sooraj.coding_kata.repositories.AccountRepository;
import com.dxc.sooraj.coding_kata.repositories.LedgerEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, ledgerEntryRepository);
    }

    @Test
    void createAccountWithInitialBalance() {
        when(accountRepository.save(any(Accounts.class)))
                .thenAnswer(call -> call.getArgument(0));

        Accounts account = accountService.createAccount(new BigDecimal("500"));

        assertThat(account.getBalance()).isEqualByComparingTo("500.00");
        assertThat(account.getCreatedAt()).isNotNull();
        verify(accountRepository).save(account);
    }

    @Test
    void cannotCreateAccountWithNegativeBalance() {
        assertThatThrownBy(() -> accountService.createAccount(new BigDecimal("-1.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Initial balance cannot be negative");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void getAccountBalance() {
        UUID accountId = UUID.randomUUID();
        Accounts account = account(accountId, "275.50");
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        BigDecimal balance = accountService.getBalance(accountId);

        assertThat(balance).isEqualByComparingTo("275.50");
    }

    @Test
    void getTransactionHistory() {
        UUID accountId = UUID.randomUUID();
        Accounts account = account(accountId, "100.00");
        List<LedgerEntry> entries = List.of(new LedgerEntry(), new LedgerEntry());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(ledgerEntryRepository.findByAccount_IdOrderByOccurredAtDesc(accountId))
                .thenReturn(entries);

        List<LedgerEntry> history = accountService.getTransactionHistory(accountId);

        assertThat(history).isSameAs(entries);
    }

    @Test
    void accountNotFound() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getBalance(accountId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(accountId.toString());
    }

    private Accounts account(UUID accountId, String balance) {
        Accounts account = new Accounts();
        account.setId(accountId);
        account.setBalance(new BigDecimal(balance));
        return account;
    }
}
