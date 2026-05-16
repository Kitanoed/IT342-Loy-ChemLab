package edu.cit.loy.chemlab.repository;

import edu.cit.loy.chemlab.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByRequestIdOrderByCreatedAtDesc(Long requestId, Pageable pageable);
    Page<Transaction> findByInventoryItemIdOrderByCreatedAtDesc(Long inventoryItemId, Pageable pageable);
}
