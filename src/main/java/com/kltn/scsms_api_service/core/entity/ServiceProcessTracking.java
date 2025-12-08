package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity quản lý theo dõi quá trình thực hiện chăm sóc xe
 * Ghi nhận các bước thực hiện của dịch vụ và tiến độ hoàn thành
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_process_tracking", schema = GeneralConstant.DB_SCHEMA_DEV)
public class ServiceProcessTracking extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tracking_id", nullable = false)
    private UUID trackingId;
    
    /**
     * Booking đang được thực hiện
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    /**
     * Bước dịch vụ đang được thực hiện (lưu toàn bộ thông tin)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_step_id", nullable = false)
    private ServiceProcessStep serviceStep;
    
    // Technician removed - will use bay's assigned technicians
    
    /**
     * Bay đang được sử dụng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bay_id", nullable = false)
    private ServiceBay bay;
    
    /**
     * ID của dịch vụ đang được thực hiện (để dễ dàng phân loại trên UI)
     */
    @Column(name = "car_service_id")
    private UUID carServiceId;
    
    /**
     * Thời điểm bắt đầu thực hiện bước này
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    /**
     * Thời điểm hoàn tất bước này
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    // Duration fields removed - will use service step's default duration
    
    /**
     * Trạng thái tiến trình của bước
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TrackingStatus status = TrackingStatus.PENDING;
    
    // Progress percent removed - simplified tracking
    
    /**
     * Ghi chú từ kỹ thuật viên hoặc quản lý
     */
    @Column(name = "notes", length = 1000)
    private String notes;
    
    /**
     * Danh sách ảnh/video chụp trong quá trình chăm sóc
     * Lưu dưới dạng JSON array
     */
    @Column(name = "evidence_media_urls", columnDefinition = "TEXT")
    private String evidenceMediaUrls;
    
    /**
     * Người cập nhật gần nhất
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by")
    private User lastUpdatedBy;
    
    /**
     * Thời điểm cập nhật cuối cùng
     */
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;
    
    // Business methods
    
    /**
     * Bắt đầu thực hiện bước
     */
    public void startStep(User updatedBy) {
        this.status = TrackingStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
        this.lastUpdatedBy = updatedBy;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    /**
     * Bắt đầu thực hiện bước (simplified - no user tracking)
     */
    public void startStep() {
        this.status = TrackingStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    // Progress update method removed - simplified tracking
    
    /**
     * Hoàn thành bước
     */
    public void completeStep() {
        this.status = TrackingStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    /**
     * Hủy bước
     */
    public void cancelStep(String reason, User cancelledBy) {
        this.status = TrackingStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
        this.notes = (this.notes != null ? this.notes + "\n" : "") + "Cancelled: " + reason;
        this.lastUpdatedBy = cancelledBy;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    /**
     * Thêm ghi chú
     */
    public void addNote(String note, User addedBy) {
        this.notes = (this.notes != null ? this.notes + "\n" : "") + 
                    LocalDateTime.now().toString() + " - " + note;
        this.lastUpdatedBy = addedBy;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    /**
     * Thêm ghi chú (simplified - no user tracking)
     */
    public void addNote(String note) {
        this.notes = (this.notes != null ? this.notes + "\n" : "") + 
                    LocalDateTime.now().toString() + " - " + note;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    /**
     * Thêm media evidence
     */
    public void addEvidenceMedia(String mediaUrl, User addedBy) {
        // Parse existing URLs and add new one
        String currentUrls = this.evidenceMediaUrls != null ? this.evidenceMediaUrls : "[]";
        // Simple JSON array handling - in production, use proper JSON library
        if (currentUrls.equals("[]")) {
            this.evidenceMediaUrls = "[\"" + mediaUrl + "\"]";
        } else {
            this.evidenceMediaUrls = currentUrls.substring(0, currentUrls.length() - 1) + 
                                   ",\"" + mediaUrl + "\"]";
        }
        this.lastUpdatedBy = addedBy;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    /**
     * Kiểm tra bước có đang thực hiện không
     */
    public boolean isInProgress() {
        return status == TrackingStatus.IN_PROGRESS;
    }
    
    /**
     * Kiểm tra bước đã hoàn thành chưa
     */
    public boolean isCompleted() {
        return status == TrackingStatus.COMPLETED;
    }
    
    /**
     * Kiểm tra bước có bị hủy không
     */
    public boolean isCancelled() {
        return status == TrackingStatus.CANCELLED;
    }
    
    // Efficiency calculation removed - simplified tracking
    
    /**
     * Cập nhật thời gian modified
     */
    @Override
    protected void onUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    /**
     * Enum cho trạng thái theo dõi
     */
    public enum TrackingStatus {
        PENDING,        // Chờ thực hiện
        IN_PROGRESS,    // Đang thực hiện
        COMPLETED,      // Hoàn thành
        CANCELLED       // Đã hủy
    }
}
