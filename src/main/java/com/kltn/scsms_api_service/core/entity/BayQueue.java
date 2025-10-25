package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity quản lý hàng chờ của từng bay
 * Lưu trữ thông tin booking nào đang chờ ở bay nào, vị trí trong hàng chờ
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bay_queues", schema = GeneralConstant.DB_SCHEMA_DEV)
public class BayQueue extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "queue_id", nullable = false)
    private UUID queueId;
    
    /**
     * ID của bay
     */
    @Column(name = "bay_id", nullable = false)
    private UUID bayId;
    
    /**
     * ID của booking
     */
    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;
    
    /**
     * Vị trí trong hàng chờ (1, 2, 3...)
     */
    @Column(name = "queue_position", nullable = false)
    private Integer queuePosition;
    
    /**
     * Ngày của hàng chờ (để phân biệt hàng chờ theo ngày)
     */
    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate;
    
    /**
     * Thời gian bắt đầu dự kiến
     */
    @Column(name = "estimated_start_time")
    private LocalDateTime estimatedStartTime;
    
    /**
     * Thời gian hoàn thành dự kiến
     */
    @Column(name = "estimated_completion_time")
    private LocalDateTime estimatedCompletionTime;
    
    /**
     * Trạng thái hoạt động của queue entry
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * Ghi chú cho queue entry
     */
    @Column(name = "notes", length = 500)
    private String notes;
    
    // Relationships
    
    /**
     * Bay liên quan
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bay_id", insertable = false, updatable = false)
    private ServiceBay serviceBay;
    
    /**
     * Booking liên quan
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;
    
    // Business methods
    
    /**
     * Kiểm tra queue entry có active không
     */
    public boolean isQueueActive() {
        return isActive != null && isActive;
    }
    
    /**
     * Tính thời gian chờ ước tính (phút)
     */
    public Long getEstimatedWaitMinutes() {
        if (estimatedStartTime != null) {
            return java.time.Duration.between(LocalDateTime.now(), estimatedStartTime).toMinutes();
        }
        return null;
    }
    
    /**
     * Cập nhật vị trí trong hàng chờ
     */
    public void updateQueuePosition(Integer newPosition) {
        this.queuePosition = newPosition;
    }
    
    /**
     * Cập nhật thời gian dự kiến
     */
    public void updateEstimatedTimes(LocalDateTime startTime, LocalDateTime completionTime) {
        this.estimatedStartTime = startTime;
        this.estimatedCompletionTime = completionTime;
    }
    
    /**
     * Deactivate queue entry
     */
    public void deactivate() {
        this.isActive = false;
    }
    
    /**
     * Activate queue entry
     */
    public void activate() {
        this.isActive = true;
    }
}
