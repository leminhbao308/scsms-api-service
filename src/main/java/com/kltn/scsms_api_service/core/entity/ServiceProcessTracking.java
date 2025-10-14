package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
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
    
    /**
     * Kỹ thuật viên đang phụ trách bước này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician;
    
    /**
     * Bay đang được sử dụng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bay_id", nullable = false)
    private ServiceBay bay;
    
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
    
    /**
     * Thời gian ước lượng hoàn thành (phút)
     */
    @Column(name = "estimated_duration")
    private Integer estimatedDuration;
    
    /**
     * Thời gian thực tế thực hiện (phút)
     */
    @Column(name = "actual_duration")
    private Integer actualDuration;
    
    /**
     * Trạng thái tiến trình của bước
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TrackingStatus status = TrackingStatus.PENDING;
    
    /**
     * Tiến độ hoàn thành (0.00 - 100.00)
     */
    @Column(name = "progress_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progressPercent = BigDecimal.ZERO;
    
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
    public void startStep(User technician) {
        this.status = TrackingStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
        this.technician = technician;
        this.progressPercent = BigDecimal.ZERO;
        this.lastUpdatedBy = technician;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    /**
     * Cập nhật tiến độ
     */
    public void updateProgress(BigDecimal progressPercent, User updatedBy) {
        if (progressPercent.compareTo(BigDecimal.ZERO) < 0) {
            progressPercent = BigDecimal.ZERO;
        }
        if (progressPercent.compareTo(new BigDecimal("100")) > 0) {
            progressPercent = new BigDecimal("100");
        }
        
        this.progressPercent = progressPercent;
        this.lastUpdatedBy = updatedBy;
        this.lastUpdatedAt = LocalDateTime.now();
        
        // Tự động hoàn thành nếu đạt 100%
        if (progressPercent.compareTo(new BigDecimal("100")) == 0) {
            completeStep();
        }
    }
    
    /**
     * Hoàn thành bước
     */
    public void completeStep() {
        this.status = TrackingStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.progressPercent = new BigDecimal("100");
        this.lastUpdatedAt = LocalDateTime.now();
        
        // Tính thời gian thực tế
        if (this.startTime != null) {
            this.actualDuration = (int) java.time.Duration.between(this.startTime, this.endTime).toMinutes();
        }
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
        
        // Tính thời gian thực tế
        if (this.startTime != null) {
            this.actualDuration = (int) java.time.Duration.between(this.startTime, this.endTime).toMinutes();
        }
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
    
    /**
     * Tính hiệu suất (actual vs estimated duration)
     */
    public BigDecimal getEfficiency() {
        if (estimatedDuration == null || estimatedDuration == 0) {
            return BigDecimal.ZERO;
        }
        if (actualDuration == null) {
            return BigDecimal.ZERO;
        }
        
        return new BigDecimal(estimatedDuration)
                .divide(new BigDecimal(actualDuration), 4, java.math.RoundingMode.HALF_UP);
    }
    
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
