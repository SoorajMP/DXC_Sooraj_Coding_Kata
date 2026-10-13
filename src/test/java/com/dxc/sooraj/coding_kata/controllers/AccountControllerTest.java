package com.dxc.sooraj.coding_kata.controllers;

import com.dxc.sooraj.coding_kata.entities.Accounts;
import com.dxc.sooraj.coding_kata.entities.BankingTransaction;
import com.dxc.sooraj.coding_kata.entities.LedgerEntry;
import com.dxc.sooraj.coding_kata.enums.EntryType;
import com.dxc.sooraj.coding_kata.exceptions.AccountNotFoundException;
import com.dxc.sooraj.coding_kata.services.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AccountController controller = new AccountController(accountService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createAccount() throws Exception {
        UUID accountId = UUID.randomUUID();
        Accounts account = account(accountId, "500.00");
        when(accountService.createAccount(any(BigDecimal.class))).thenReturn(account);

        mockMvc.perform(post("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"initialBalance": 500.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.balance").value(500.00));

        verify(accountService).createAccount(new BigDecimal("500.00"));
    }

    @Test
    void getBalance() throws Exception {
        UUID accountId = UUID.randomUUID();
        when(accountService.getBalance(accountId)).thenReturn(new BigDecimal("275.50"));

        mockMvc.perform(get("/api/v1/accounts/{accountId}/balancecheck", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.balance").value(275.50));
    }

    @Test
    void getTransactionHistory() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        BankingTransaction transaction = new BankingTransaction();
        transaction.setId(transactionId);

        LedgerEntry entry = new LedgerEntry();
        entry.setId(UUID.randomUUID());
        entry.setTransaction(transaction);
        entry.setEntryType(EntryType.CREDIT);
        entry.setAmount(new BigDecimal("100.00"));
        entry.setBalanceAfter(new BigDecimal("300.00"));
        entry.setOccurredAt(Instant.parse("2026-10-13T08:00:00Z"));
        when(accountService.getTransactionHistory(accountId)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/v1/accounts/{accountId}/transaction", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].transactionId").value(transactionId.toString()))
                .andExpect(jsonPath("$[0].entryType").value("CREDIT"))
                .andExpect(jsonPath("$[0].balanceAfter").value(300.00));
    }

    @Test
    void accountNotFoundReturns404() throws Exception {
        UUID accountId = UUID.randomUUID();
        when(accountService.getBalance(accountId))
                .thenThrow(new AccountNotFoundException(accountId));

        mockMvc.perform(get("/api/v1/accounts/{accountId}/balancecheck", accountId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Account not found: " + accountId))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/accounts/" + accountId + "/balancecheck"));
    }

    private Accounts account(UUID accountId, String balance) {
        Accounts account = new Accounts();
        account.setId(accountId);
        account.setBalance(new BigDecimal(balance));
        account.setCreatedAt(Instant.parse("2026-10-13T08:00:00Z"));
        return account;
    }
}
