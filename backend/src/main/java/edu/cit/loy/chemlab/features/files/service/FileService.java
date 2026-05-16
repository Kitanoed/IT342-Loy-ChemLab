package edu.cit.loy.chemlab.features.files.service;

import edu.cit.loy.chemlab.entity.InventoryItem;
import edu.cit.loy.chemlab.entity.UploadedFile;
import edu.cit.loy.chemlab.entity.User;
import edu.cit.loy.chemlab.exception.InventoryApiException;
import edu.cit.loy.chemlab.features.files.dto.FileResponse;
import edu.cit.loy.chemlab.repository.InventoryItemRepository;
import edu.cit.loy.chemlab.repository.UploadedFileRepository;
import edu.cit.loy.chemlab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final UploadedFileRepository fileRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final UserRepository userRepository;
    private final HttpClient httpClient;

    private final String supabaseUrl;
    private final String supabaseServiceKey;
    private final String storageBucket;

    public FileService(UploadedFileRepository fileRepository,
                       InventoryItemRepository inventoryItemRepository,
                       UserRepository userRepository,
                       @Value("${supabase.url:}") String supabaseUrl,
                       @Value("${supabase.service-key:}") String supabaseServiceKey,
                       @Value("${supabase.storage-bucket:sds-files}") String storageBucket) {
        this.fileRepository = fileRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.userRepository = userRepository;
        this.supabaseUrl = supabaseUrl;
        this.supabaseServiceKey = supabaseServiceKey;
        this.storageBucket = storageBucket;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Transactional
    public FileResponse uploadFile(MultipartFile file, Long inventoryItemId, String actorEmail) {
        User uploader = getUser(actorEmail);
        requireTechnicianOrAdmin(uploader);

        InventoryItem item = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new InventoryApiException("ITEM_NOT_FOUND", 404, "Inventory item not found."));

        // Validate file
        if (file.isEmpty()) {
            throw new InventoryApiException("VALIDATION_ERROR", 400, "File is empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InventoryApiException("VALIDATION_ERROR", 400, "File exceeds maximum size of 10MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new InventoryApiException("VALIDATION_ERROR", 400,
                    "Invalid file type. Allowed: PDF, JPEG, PNG.");
        }

        // Generate unique file path
        String extension = getExtension(file.getOriginalFilename());
        String storagePath = "item-" + inventoryItemId + "/" + UUID.randomUUID() + extension;

        // Upload to Supabase Storage
        uploadToSupabaseStorage(storagePath, file, contentType);

        // Save metadata to database
        UploadedFile uploaded = new UploadedFile();
        uploaded.setInventoryItem(item);
        uploaded.setUploader(uploader);
        uploaded.setFileName(file.getOriginalFilename());
        uploaded.setFileType(contentType);
        uploaded.setFilePath(storagePath);
        uploaded.setFileSize(file.getSize());
        UploadedFile saved = fileRepository.save(uploaded);

        return toResponse(saved);
    }

    public List<FileResponse> listFiles(Long inventoryItemId) {
        return fileRepository.findByInventoryItemIdOrderByCreatedAtDesc(inventoryItemId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public FileResponse getFileById(Long fileId) {
        UploadedFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new InventoryApiException("FILE_NOT_FOUND", 404, "File not found."));
        return toResponse(file);
    }

    public String getPublicUrl(Long fileId) {
        UploadedFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new InventoryApiException("FILE_NOT_FOUND", 404, "File not found."));
        return buildPublicUrl(file.getFilePath());
    }

    private void uploadToSupabaseStorage(String path, MultipartFile file, String contentType) {
        if (supabaseUrl == null || supabaseUrl.isBlank() || supabaseServiceKey == null || supabaseServiceKey.isBlank()) {
            throw new InventoryApiException("STORAGE_CONFIG_ERROR", 500,
                    "Supabase Storage is not configured. Set supabase.url and supabase.service-key.");
        }

        try {
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + storageBucket + "/" + path;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + supabaseServiceKey)
                    .header("Content-Type", contentType)
                    .header("x-upsert", "true")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new InventoryApiException("STORAGE_UPLOAD_ERROR", 502,
                        "Failed to upload to Supabase Storage. Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new InventoryApiException("STORAGE_UPLOAD_ERROR", 502,
                    "Failed to upload file to storage.", ex.getMessage());
        }
    }

    private String buildPublicUrl(String path) {
        if (supabaseUrl == null || supabaseUrl.isBlank()) {
            return "/api/files/download/" + path;
        }
        return supabaseUrl + "/storage/v1/object/public/" + storageBucket + "/" + path;
    }

    private FileResponse toResponse(UploadedFile file) {
        FileResponse resp = new FileResponse();
        resp.setId(file.getId());
        resp.setInventoryItemId(file.getInventoryItem().getId());
        resp.setFileName(file.getFileName());
        resp.setFileType(file.getFileType());
        resp.setFileSize(file.getFileSize());
        resp.setDownloadUrl(buildPublicUrl(file.getFilePath()));
        resp.setUploaderId(file.getUploader().getId());
        resp.setUploaderUsername(file.getUploader().getUsername());
        resp.setCreatedAt(file.getCreatedAt());
        return resp;
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx) : "";
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InventoryApiException("AUTH_USER_NOT_FOUND", 401, "Authenticated user not found."));
    }

    private void requireTechnicianOrAdmin(User user) {
        if (user.getRole() == User.Role.STUDENT) {
            throw new InventoryApiException("FORBIDDEN", 403, "Only Technician or Admin can upload files.");
        }
    }
}
