package com.dxc.sooraj.coding_kata.repositories;

import com.dxc.sooraj.coding_kata.entities.Accounts;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;


import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {


    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestEntityManager entityManager;



    @Test
    void testAccountCrud() {
        // Create and save one account
        Accounts account = new Accounts();
        account.setBalance(new BigDecimal("1000.00"));
        account.setCreatedAt(Instant.parse("2026-10-12T10:00:00Z"));



        Accounts savedAccount = accountRepository.saveAndFlush(account);
        UUID accountId = savedAccount.getId();
        Long initialVersion = savedAccount.getVersion();

        assertThat(accountId).isNotNull();
        assertThat(initialVersion).isNotNull();



        // Clear cache and read again from database
        entityManager.clear();

        Accounts foundAccount = accountRepository.findById(accountId).orElseThrow();
        assertThat(foundAccount.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(foundAccount.getCreatedAt())
                .isEqualTo(Instant.parse("2026-10-12T10:00:00Z"));



        // Update the balance
        foundAccount.setBalance(new BigDecimal("1250.00"));
        accountRepository.saveAndFlush(foundAccount);
        entityManager.clear();



        Accounts updatedAccount = accountRepository.findById(accountId).orElseThrow();
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo("1250.00");
        assertThat(updatedAccount.getVersion()).isGreaterThan(initialVersion);



        // Delete account and check it is not available
        accountRepository.delete(updatedAccount);
        accountRepository.flush();
        entityManager.clear();

        assertThat(accountRepository.findById(accountId)).isEmpty();


    }
}
