package edu.cit.loy.chemlab.repository;

import edu.cit.loy.chemlab.entity.InventoryBulkJobRow;
import edu.cit.loy.chemlab.entity.BulkRowStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryBulkJobRowRepository extends JpaRepository<InventoryBulkJobRow, Long> {
    List<InventoryBulkJobRow> findByJob_IdOrderByRowNoAsc(UUID jobId);
    List<InventoryBulkJobRow> findByJob_IdAndRowStatusOrderByRowNoAsc(UUID jobId, BulkRowStatus rowStatus);
}
