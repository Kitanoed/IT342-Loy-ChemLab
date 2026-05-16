package edu.cit.loy.chemlab.repository;

import edu.cit.loy.chemlab.entity.ItemRequest;
import edu.cit.loy.chemlab.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    @Query("""
        SELECT r FROM ItemRequest r
        WHERE (:status IS NULL OR r.status = :status)
          AND (:requesterId IS NULL OR r.requester.id = :requesterId)
        ORDER BY r.createdAt DESC
    """)
    Page<ItemRequest> findFiltered(
            @Param("status") RequestStatus status,
            @Param("requesterId") Long requesterId,
            Pageable pageable
    );

    Page<ItemRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId, Pageable pageable);
}
