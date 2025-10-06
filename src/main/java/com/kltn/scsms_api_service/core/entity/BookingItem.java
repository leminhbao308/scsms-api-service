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
     * Loại item (SERVICE hoặc SERVICE_PACKAGE)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;
    
    /**
     * ID của service hoặc service package
     */
    @Column(name = "item_id", nullable = false)
    private UUID itemId;
    
    /**
     * Tên item tại thời điểm booking (snapshot)
     */
    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;
    
    /**
     * URL của item tại thời điểm booking
     */
    @Column(name = "item_url", length = 500)
    private String itemUrl;
    
    /**
     * Mô tả item tại thời điểm booking
     */
    @Column(name = "item_description", length = 1000)
    private String itemDescription;
    
    /**
     * Giá đơn vị tại thời điểm booking
     */
    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;
    
    /**
     * Số lượng
     */
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;
    
    /**
     * Thời gian thực hiện (phút) tại thời điểm booking
     */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    
    /**
     * Số tiền chiết khấu cho item này
     */
    @Column(name = "discount_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    /**
     * Số tiền thuế cho item này
     */
    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    /**
     * Tổng số tiền cho item này (đã tính chiết khấu và thuế)
     */
    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
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
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false)
    @Builder.Default
    private ItemStatus itemStatus = ItemStatus.PENDING;
    
    /**
     * Thời gian bắt đầu thực tế cho item này
     */
    @Column(name = "actual_start_at")
    private java.time.LocalDateTime actualStartAt;
    
    /**
     * Thời gian kết thúc thực tế cho item này
     */
    @Column(name = "actual_end_at")
    private java.time.LocalDateTime actualEndAt;
    
    // Business methods
    
    /**
     * Tính tổng tiền trước chiết khấu
     */
    public BigDecimal getSubtotalAmount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    /**
     * Tính tổng tiền sau chiết khấu và thuế
     */
    public BigDecimal calculateTotalAmount() {
        BigDecimal subtotal = getSubtotalAmount();
        BigDecimal afterDiscount = subtotal.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        return afterDiscount.add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
    }
    
    /**
     * Cập nhật tổng tiền
     */
    public void updateTotalAmount() {
        this.totalAmount = calculateTotalAmount();
    }
    
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
        this.actualStartAt = java.time.LocalDateTime.now();
    }
    
    /**
     * Hoàn thành item
     */
    public void completeItem() {
        this.itemStatus = ItemStatus.COMPLETED;
        this.actualEndAt = java.time.LocalDateTime.now();
    }
    
    /**
     * Tính thời gian thực tế (phút)
     */
    public Long getActualDurationMinutes() {
        if (actualStartAt != null && actualEndAt != null) {
            return java.time.Duration.between(actualStartAt, actualEndAt).toMinutes();
        }
        return null;
    }
    
    /**
     * Enum cho loại item
     */
    public enum ItemType {
        SERVICE,        // Dịch vụ đơn lẻ
        SERVICE_PACKAGE // Gói dịch vụ
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
