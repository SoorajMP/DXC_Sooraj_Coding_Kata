package com.dxc.sooraj.coding_kata.repositories;

import com.dxc.sooraj.coding_kata.entities.BankingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BankingTransactionRepository
        extends JpaRepository<BankingTransaction, UUID> {
}
