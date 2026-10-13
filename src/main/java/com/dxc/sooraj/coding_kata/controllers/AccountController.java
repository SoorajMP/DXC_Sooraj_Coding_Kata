package com.dxc.sooraj.coding_kata.controllers;

import com.dxc.sooraj.coding_kata.dto.AccountResponse;
import com.dxc.sooraj.coding_kata.dto.BalanceResponse;
import org.springframework.web.bind.annotation.PathVariable;
import com.dxc.sooraj.coding_kata.dto.CreateAccountRequest;
import com.dxc.sooraj.coding_kata.dto.LedgerEntryResponse;
import com.dxc.sooraj.coding_kata.entities.Accounts;
import com.dxc.sooraj.coding_kata.services.AccountService;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AccountController {

    private final AccountService acntService;

    public AccountController(AccountService accountService) {
        this.acntService = accountService;
    }


    @PostMapping("/account")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@RequestBody CreateAccountRequest request) {
        BigDecimal initialBalance = request == null ? null : request.initialBalance();
        Accounts account = acntService.createAccount(initialBalance);
        return AccountResponse.from(account);
    }

    @GetMapping("/accounts/{accountId}/balancecheck")
    public BalanceResponse getBalance(@PathVariable("accountId") UUID accountId) {
        BigDecimal balance = acntService.getBalance(accountId);
        return new BalanceResponse(accountId, balance);
    }
    @GetMapping("/accounts/{accountId}/transaction")
    public List<LedgerEntryResponse> getTransactionHistory(
            @PathVariable("accountId") UUID accountId) {
        return acntService.getTransactionHistory(accountId)
                .stream()
                .map(LedgerEntryResponse::from)
                .toList();
    }
}
