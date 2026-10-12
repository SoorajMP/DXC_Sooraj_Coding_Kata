package com.dxc.sooraj.coding_kata.repositories;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.dxc.sooraj.coding_kata.entities.Accounts;
import com.dxc.sooraj.coding_kata.entities.BankingTransaction;
import com.dxc.sooraj.coding_kata.enums.TransactionType;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;



import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BankingTransactionRepositoryTest {


    @Autowired
    private AccountRepository accountRepository;


    @Autowired
    private BankingTransactionRepository transactionRepository;



    @Autowired
    private TestEntityManager entityManager;



    @Test
    void testBankingTransactionCrud() {
        // Accounts are needed before creating transfer
        Accounts sourceAccount = accountRepository.save(createAccount("1000.00"));
        Accounts destinationAccount = accountRepository.save(createAccount("500.00"));
        accountRepository.flush();

        // Create transfer transaction

        BankingTransaction transfer = new BankingTransaction();
        transfer.setSourceAccount(sourceAccount);
        transfer.setDestinationAccount(destinationAccount);
        transfer.setTransactionType(TransactionType.TRANSFER);
        transfer.setAmount(new BigDecimal("100.00"));
        transfer.setCreatedAt(Instant.parse("2026-10-12T10:30:00Z"));



        BankingTransaction savedTransfer = transactionRepository.saveAndFlush(transfer);
        UUID transferId = savedTransfer.getId();
        UUID sourceAccountId = sourceAccount.getId();
        UUID destinationAccountId = destinationAccount.getId();

        assertThat(transferId).isNotNull();

        // Clear cache and get transaction fr
        // om database
        entityManager.clear();

        BankingTransaction foundTransfer = transactionRepository.findById(transferId).orElseThrow();

        assertThat(foundTransfer.getTransactionType()).isEqualTo(TransactionType.TRANSFER);

        assertThat(foundTransfer.getAmount()).isEqualByComparingTo("100.00");
        assertThat(foundTransfer.getSourceAccount().getId()).isEqualTo(sourceAccountId);
        assertThat(foundTransfer.getDestinationAccount().getId())
                .isEqualTo(destinationAccountId);



        // Update transfer amount


        foundTransfer.setAmount(new BigDecimal("125.00"));
        transactionRepository.saveAndFlush(foundTransfer);
        entityManager.clear();

        BankingTransaction updatedTransfer = transactionRepository.findById(transferId)
                .orElseThrow();
        assertThat(updatedTransfer.getAmount()).isEqualByComparingTo("125.00");



        // Delete transaction
        transactionRepository.delete(updatedTransfer);
        transactionRepository.flush();
        entityManager.clear();

        assertThat(transactionRepository.findById(transferId)).isEmpty();
    }

    private Accounts createAccount(String balance) {
        Accounts account = new Accounts();
        account.setBalance(new BigDecimal(balance));
        account.setCreatedAt(Instant.parse("2026-10-12T10:00:00Z"));
        return account;
    }
}
