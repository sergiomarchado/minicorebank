package com.sergiom.minicorebank.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    @Query("""
      select coalesce(
        sum(case when e.direction='CREDIT' then e.amountMinor
                 when e.direction='DEBIT' then -e.amountMinor end), 0)
      from LedgerEntry e where e.account.id = :accountId
    """)
    long balanceOf(UUID accountId);
}

