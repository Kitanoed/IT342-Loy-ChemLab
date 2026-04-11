package edu.cit.loy.chemlab.repository;

import edu.cit.loy.chemlab.entity.InventoryBulkJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryBulkJobRepository extends JpaRepository<InventoryBulkJob, UUID> {
    Optional<InventoryBulkJob> findByIdempotencyKey(String idempotencyKey);
}
