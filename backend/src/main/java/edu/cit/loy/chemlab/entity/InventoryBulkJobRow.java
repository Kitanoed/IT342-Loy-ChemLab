package edu.cit.loy.chemlab.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_bulk_job_rows", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"job_id", "row_no"})
})
public class InventoryBulkJobRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private InventoryBulkJob job;

    @Column(name = "row_no", nullable = false)
    private Integer rowNo;

    private Long itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BulkRowStatus rowStatus = BulkRowStatus.PENDING;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requestedChange;

    @Column(columnDefinition = "TEXT")
    private String beforeSnapshot;

    @Column(columnDefinition = "TEXT")
    private String afterSnapshot;

    @Column(length = 80)
    private String errorCode;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String warningMessages;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InventoryBulkJob getJob() {
        return job;
    }

    public void setJob(InventoryBulkJob job) {
        this.job = job;
    }

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public BulkRowStatus getRowStatus() {
        return rowStatus;
    }

    public void setRowStatus(BulkRowStatus rowStatus) {
        this.rowStatus = rowStatus;
    }

    public String getRequestedChange() {
        return requestedChange;
    }

    public void setRequestedChange(String requestedChange) {
        this.requestedChange = requestedChange;
    }

    public String getBeforeSnapshot() {
        return beforeSnapshot;
    }

    public void setBeforeSnapshot(String beforeSnapshot) {
        this.beforeSnapshot = beforeSnapshot;
    }

    public String getAfterSnapshot() {
        return afterSnapshot;
    }

    public void setAfterSnapshot(String afterSnapshot) {
        this.afterSnapshot = afterSnapshot;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getWarningMessages() {
        return warningMessages;
    }

    public void setWarningMessages(String warningMessages) {
        this.warningMessages = warningMessages;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
