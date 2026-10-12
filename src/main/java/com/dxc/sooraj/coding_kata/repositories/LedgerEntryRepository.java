package com.dxc.sooraj.coding_kata.repositories;


import com.dxc.sooraj.coding_kata.entities.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository
        extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByAccount_IdOrderByOccurredAtDesc(UUID accountId);
}