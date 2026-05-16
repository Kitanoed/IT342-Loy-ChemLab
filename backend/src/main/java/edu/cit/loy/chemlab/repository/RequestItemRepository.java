package edu.cit.loy.chemlab.repository;

import edu.cit.loy.chemlab.entity.RequestItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestItemRepository extends JpaRepository<RequestItem, Long> {
    List<RequestItem> findByRequestIdOrderByIdAsc(Long requestId);
}
