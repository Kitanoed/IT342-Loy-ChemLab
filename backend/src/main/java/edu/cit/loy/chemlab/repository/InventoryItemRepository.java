package edu.cit.loy.chemlab.repository;

import edu.cit.loy.chemlab.entity.InventoryItem;
import edu.cit.loy.chemlab.entity.InventoryItemType;
import edu.cit.loy.chemlab.entity.InventoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    @Query("""
        SELECT i FROM InventoryItem i
        WHERE i.archived = false
          AND (:type IS NULL OR i.itemType = :type)
          AND (:status IS NULL OR i.status = :status)
          AND (:labId IS NULL OR i.labId = :labId)
    """)
    Page<InventoryItem> search(
            @Param("search") String search,
            @Param("type") InventoryItemType type,
            @Param("status") InventoryStatus status,
            @Param("labId") Long labId,
            Pageable pageable
    );

    Optional<InventoryItem> findFirstByItemNameIgnoreCaseAndArchivedFalse(String itemName);

    boolean existsByItemCodeIgnoreCase(String itemCode);
}
