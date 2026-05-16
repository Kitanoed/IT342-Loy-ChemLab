package edu.cit.loy.chemlab.features.files.controller;

import edu.cit.loy.chemlab.features.files.dto.FileResponse;
import edu.cit.loy.chemlab.features.files.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public ResponseEntity<FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("inventoryItemId") Long inventoryItemId,
            Authentication authentication
    ) {
        FileResponse response = fileService.uploadFile(file, inventoryItemId, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','TECHNICIAN','ADMIN')")
    public ResponseEntity<List<FileResponse>> listFiles(
            @RequestParam("inventoryItemId") Long inventoryItemId
    ) {
        return ResponseEntity.ok(fileService.listFiles(inventoryItemId));
    }

    @GetMapping("/{fileId}")
    @PreAuthorize("hasAnyRole('STUDENT','TECHNICIAN','ADMIN')")
    public ResponseEntity<FileResponse> getFile(@PathVariable Long fileId) {
        return ResponseEntity.ok(fileService.getFileById(fileId));
    }

    @GetMapping("/{fileId}/download")
    @PreAuthorize("hasAnyRole('STUDENT','TECHNICIAN','ADMIN')")
    public ResponseEntity<Void> downloadFile(@PathVariable Long fileId) {
        String publicUrl = fileService.getPublicUrl(fileId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(publicUrl))
                .build();
    }
}
