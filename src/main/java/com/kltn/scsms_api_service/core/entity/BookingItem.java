package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity quản lý từng dịch vụ/gói trong booking
 * Lưu trữ snapshot về giá và thông tin dịch vụ tại thời điểm booking
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "booking_items", schema = GeneralConstant.DB_SCHEMA_DEV)
public class BookingItem extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_item_id", nullable = false)
    private UUID bookingItemId;
    
    /**
     * Booking chứa item này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    /**
     * ID của service
     */
    @Column(name = "service_id", nullable = false)
    private UUID serviceId;
    
    /**
     * Tên service tại thời điểm booking (snapshot)
     */
    @Column(name = "service_name", nullable = false, length = 255)
    private String serviceName;
    
    /**
     * Mô tả service tại thời điểm booking
     */
    @Column(name = "service_description", length = 1000)
    private String serviceDescription;
    
    /**
     * Giá đơn vị tại thời điểm booking
     */
    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;
    
    /**
     * Thời gian thực hiện (phút) tại thời điểm booking
     */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    
    /**
     * Ghi chú cho item này
     */
    @Column(name = "notes", length = 500)
    private String notes;
    
    /**
     * Thứ tự hiển thị trong booking
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 1;
    
    /**
     * Trạng thái item (có thể khác với booking status)
     * Để track trạng thái từng dịch vụ trong booking
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false)
    @Builder.Default
    private ItemStatus itemStatus = ItemStatus.PENDING;
    
    // Business methods
    
    /**
     * Kiểm tra item có hoàn thành không
     */
    public boolean isCompleted() {
        return itemStatus == ItemStatus.COMPLETED;
    }
    
    /**
     * Kiểm tra item có đang thực hiện không
     */
    public boolean isInProgress() {
        return itemStatus == ItemStatus.IN_PROGRESS;
    }
    
    /**
     * Bắt đầu thực hiện item
     */
    public void startItem() {
        this.itemStatus = ItemStatus.IN_PROGRESS;
    }
    
    /**
     * Hoàn thành item
     */
    public void completeItem() {
        this.itemStatus = ItemStatus.COMPLETED;
    }
    
    /**
     * Enum cho trạng thái item
     */
    public enum ItemStatus {
        PENDING,        // Chờ thực hiện
        IN_PROGRESS,    // Đang thực hiện
        COMPLETED,      // Hoàn thành
        CANCELLED,      // Đã hủy
        SKIPPED         // Bỏ qua
    }
}
