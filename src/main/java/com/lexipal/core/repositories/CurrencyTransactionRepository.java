package com.lexipal.core.repositories;

import com.lexipal.core.entities.CurrencyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrencyTransactionRepository extends JpaRepository<CurrencyTransaction, Long>
{
    List<CurrencyTransaction> findByUser_IdOrderByCreatedAtAscIdAsc(Long userId);
}
