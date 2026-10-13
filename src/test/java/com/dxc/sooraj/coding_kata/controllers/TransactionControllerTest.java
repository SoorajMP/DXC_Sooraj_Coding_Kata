package com.dxc.sooraj.coding_kata.controllers;

import com.dxc.sooraj.coding_kata.entities.Accounts;
import com.dxc.sooraj.coding_kata.entities.BankingTransaction;
import com.dxc.sooraj.coding_kata.enums.TransactionType;
import com.dxc.sooraj.coding_kata.exceptions.InsufficientFundsException;
import com.dxc.sooraj.coding_kata.services.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TransactionController controller = new TransactionController(transactionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void depositMoney() throws Exception {
        UUID accountId = UUID.randomUUID();
        BankingTransaction deposit = transaction(
                TransactionType.DEPOSIT, null, accountId, "50.00");
        when(transactionService.deposit(eq(accountId), any(BigDecimal.class)))
                .thenReturn(deposit);

        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 50.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.destinationAccountId").value(accountId.toString()));

        verify(transactionService).deposit(accountId, new BigDecimal("50.00"));
    }

    @Test
    void withdrawMoney() throws Exception {
        UUID accountId = UUID.randomUUID();
        BankingTransaction withdrawal = transaction(
                TransactionType.WITHDRAWAL, accountId, null, "25.00");
        when(transactionService.withdraw(eq(accountId), any(BigDecimal.class)))
                .thenReturn(withdrawal);

        mockMvc.perform(post("/api/v1/accounts/{accountId}/withdrawal", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 25.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.sourceAccountId").value(accountId.toString()));
    }

    @Test
    void transferMoney() throws Exception {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();
        BankingTransaction transfer = transaction(
                TransactionType.TRANSFER, sourceId, destinationId, "125.00");
        when(transactionService.transfer(
                eq(sourceId), eq(destinationId), any(BigDecimal.class)))
                .thenReturn(transfer);

        mockMvc.perform(post("/api/v1/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId": "%s",
                                  "destinationAccountId": "%s",
                                  "amount": 125.00
                                }
                                """.formatted(sourceId, destinationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType").value("TRANSFER"))
                .andExpect(jsonPath("$.sourceAccountId").value(sourceId.toString()))
                .andExpect(jsonPath("$.destinationAccountId").value(destinationId.toString()));
    }

    @Test
    void invalidAmountReturns400() throws Exception {
        UUID accountId = UUID.randomUUID();
        when(transactionService.deposit(accountId, BigDecimal.ZERO))
                .thenThrow(new IllegalArgumentException("Amount must be greater than zero"));

        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Amount must be greater than zero"));
    }

    @Test
    void insufficientFundsReturns422() throws Exception {
        UUID accountId = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("25.00");
        BigDecimal amount = new BigDecimal("30.00");
        when(transactionService.withdraw(accountId, amount))
                .thenThrow(new InsufficientFundsException(accountId, balance, amount));

        mockMvc.perform(post("/api/v1/accounts/{accountId}/withdrawal", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 30.00}
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void concurrentUpdateReturns409() throws Exception {
        UUID accountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("10.00");
        when(transactionService.deposit(accountId, amount))
                .thenThrow(new OptimisticLockingFailureException("Account changed"));

        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 10.00}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Account was updated by another request. Please retry"));
    }

    private BankingTransaction transaction(
            TransactionType type,
            UUID sourceId,
            UUID destinationId,
            String amount) {

        BankingTransaction transaction = new BankingTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setTransactionType(type);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setCreatedAt(Instant.parse("2026-10-13T08:00:00Z"));

        if (sourceId != null) {
            Accounts source = new Accounts();
            source.setId(sourceId);
            transaction.setSourceAccount(source);
        }

        if (destinationId != null) {
            Accounts destination = new Accounts();
            destination.setId(destinationId);
            transaction.setDestinationAccount(destination);
        }

        return transaction;
    }
}
