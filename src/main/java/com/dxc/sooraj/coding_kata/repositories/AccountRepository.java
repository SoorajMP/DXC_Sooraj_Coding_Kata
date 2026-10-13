package com.dxc.sooraj.coding_kata.repositories;

import com.dxc.sooraj.coding_kata.entities.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Accounts, UUID> {
}
