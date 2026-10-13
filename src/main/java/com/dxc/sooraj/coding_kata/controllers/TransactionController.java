package com.dxc.sooraj.coding_kata.controllers;

import com.dxc.sooraj.coding_kata.dto.AmountRequest;
import com.dxc.sooraj.coding_kata.dto.TransactionResponse;
import com.dxc.sooraj.coding_kata.dto.TransferRequest;
import com.dxc.sooraj.coding_kata.entities.BankingTransaction;
import com.dxc.sooraj.coding_kata.services.TransactionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/accounts/{accountId}/deposit")
    public TransactionResponse deposit(
            @PathVariable("accountId") UUID accountId,
            @RequestBody AmountRequest request) {

        BigDecimal amount = request == null ? null : request.amount();
        BankingTransaction transaction = transactionService.deposit(accountId, amount);
        return TransactionResponse.from(transaction);
    }

    @PostMapping("/accounts/{accountId}/withdrawal")
    public TransactionResponse withdraw(
            @PathVariable("accountId") UUID accountId,
            @RequestBody AmountRequest request) {

        BigDecimal amount = request == null ? null : request.amount();
        BankingTransaction transaction = transactionService.withdraw(accountId, amount);
        return TransactionResponse.from(transaction);
    }

    @PostMapping("/transfer")
    public TransactionResponse transfer(@RequestBody TransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Transfer request is required");
        }

        BankingTransaction transaction = transactionService.transfer(
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.amount());
        return TransactionResponse.from(transaction);
    }
}
