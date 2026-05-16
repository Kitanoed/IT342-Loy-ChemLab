package edu.cit.loy.chemlab.repository;

import edu.cit.loy.chemlab.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
    List<UploadedFile> findByInventoryItemIdOrderByCreatedAtDesc(Long inventoryItemId);
}
