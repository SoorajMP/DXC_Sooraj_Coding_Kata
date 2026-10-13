package com.dxc.sooraj.coding_kata.repositories;

import com.dxc.sooraj.coding_kata.entities.Accounts;
import com.dxc.sooraj.coding_kata.entities.BankingTransaction;
import com.dxc.sooraj.coding_kata.entities.LedgerEntry;
import com.dxc.sooraj.coding_kata.enums.EntryType;
import com.dxc.sooraj.coding_kata.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LedgerEntryRepositoryTest {


    @Autowired
    private AccountRepository accountRepository;


    @Autowired
    private BankingTransactionRepository transactionRepository;
    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testLedgerEntryCrudAndHistoryOrder() {

        // Account and transaction are needed for ledger entry
        Accounts account = new Accounts();
        account.setBalance(new BigDecimal("300.00"));
        account.setCreatedAt(Instant.parse("2026-10-12T09:00:00Z"));
        account = accountRepository.saveAndFlush(account);

        BankingTransaction deposit = new BankingTransaction();
        deposit.setTransactionType(TransactionType.DEPOSIT);
        deposit.setAmount(new BigDecimal("300.00"));
        deposit.setDestinationAccount(account);
        deposit.setCreatedAt(Instant.parse("2026-10-12T09:00:00Z"));
        deposit = transactionRepository.saveAndFlush(deposit);


        // Create two entries with different time
        LedgerEntry olderEntry = newLedgerEntry(
                account,
                deposit,
                "100.00",
                "100.00",
                Instant.parse("2026-10-12T09:00:00Z"));


        LedgerEntry newerEntry = newLedgerEntry(
                account,
                deposit,
                "200.00",
                "300.00",
                Instant.parse("2026-10-12T10:00:00Z"));


        olderEntry = ledgerEntryRepository.save(olderEntry);
        newerEntry = ledgerEntryRepository.saveAndFlush(newerEntry);

        UUID olderEntryId = olderEntry.getId();
        UUID newerEntryId = newerEntry.getId();
        UUID accountId = account.getId();


        assertThat(olderEntryId).isNotNull();
        assertThat(newerEntryId).isNotNull();



        // Read history and check latest entry comes first
        entityManager.clear();

        List<LedgerEntry> history =
                ledgerEntryRepository.findByAccount_IdOrderByOccurredAtDesc(accountId);



        assertThat(history)
                .extracting(LedgerEntry::getId)
                .containsExactly(newerEntryId, olderEntryId);



        // Update latest balance
        LedgerEntry entryToUpdate = history.getFirst();
        entryToUpdate.setBalanceAfter(new BigDecimal("325.00"));
        ledgerEntryRepository.saveAndFlush(entryToUpdate);


        entityManager.clear();

        assertThat(ledgerEntryRepository.findById(newerEntryId).orElseThrow()
                .getBalanceAfter()).isEqualByComparingTo("325.00");


        // Delete all ledger entries
        ledgerEntryRepository.deleteAll();
        ledgerEntryRepository.flush();
        entityManager.clear();


        assertThat(ledgerEntryRepository.findAll()).isEmpty();


    }

    private LedgerEntry newLedgerEntry(

            Accounts account,
            BankingTransaction transaction,
            String amount,
            String balanceAfter,
            Instant occurredAt) {
        LedgerEntry entry = new LedgerEntry();
        entry.setAccount(account);
        entry.setTransaction(transaction);
        entry.setEntryType(EntryType.CREDIT);
        entry.setAmount(new BigDecimal(amount));
        entry.setBalanceAfter(new BigDecimal(balanceAfter));
        entry.setOccurredAt(occurredAt);
        return entry;

    }
}
