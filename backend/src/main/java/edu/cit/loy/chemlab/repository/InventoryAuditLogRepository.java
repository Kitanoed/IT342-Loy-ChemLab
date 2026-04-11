package edu.cit.loy.chemlab.repository;

import edu.cit.loy.chemlab.entity.InventoryAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryAuditLogRepository extends JpaRepository<InventoryAuditLog, Long> {
    Page<InventoryAuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId, Pageable pageable);
    Page<InventoryAuditLog> findByEntityTypeAndCorrelationIdOrderByCreatedAtDesc(String entityType, UUID correlationId, Pageable pageable);
}
