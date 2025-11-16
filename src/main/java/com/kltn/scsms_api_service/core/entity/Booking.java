package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.BookingType;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity quản lý booking đặt lịch chăm sóc xe
 * Lưu trữ toàn bộ snapshot về giá/giờ/dịch vụ tại thời điểm đặt
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bookings", schema = GeneralConstant.DB_SCHEMA_DEV)
public class Booking extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;
    
    /**
     * Mã booking hiển thị cho khách hàng (vd: BK-20251007-0001)
     */
    @Column(name = "booking_code", unique = true, nullable = false, length = 50)
    private String bookingCode;
    
    /**
     * Khách hàng (nullable nếu là guest)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;
    
    /**
     * Snapshot thông tin khách hàng (bắt buộc nếu guest)
     */
    @Column(name = "customer_name", length = 255)
    private String customerName;
    
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;
    
    @Column(name = "customer_email", length = 255)
    private String customerEmail;
    
    /**
     * Xe (nullable nếu chưa có profile)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private VehicleProfile vehicle;
    
    /**
     * Snapshot thông tin xe
     */
    @Column(name = "vehicle_license_plate", length = 20)
    private String vehicleLicensePlate;
    
    @Column(name = "vehicle_brand_name", length = 100)
    private String vehicleBrandName;
    
    @Column(name = "vehicle_model_name", length = 100)
    private String vehicleModelName;
    
    @Column(name = "vehicle_type_name", length = 100)
    private String vehicleTypeName;
    
    @Column(name = "vehicle_year")
    private Integer vehicleYear;
    
    @Column(name = "vehicle_color", length = 50)
    private String vehicleColor;
    
    /**
     * Chi nhánh thực hiện
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    
    /**
     * Bay được đặt
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bay_id")
    private ServiceBay serviceBay;
    
    /**
     * Thời điểm slot đã được chọn (ghi nhận slot thực tế được đặt)
     */
    @Column(name = "preferred_start_at")
    private LocalDateTime preferredStartAt;
    
    /**
     * Thời điểm xác nhận (đã sắp xếp)
     */
    @Column(name = "scheduled_start_at")
    private LocalDateTime scheduledStartAt;
    
    /**
     * Thời điểm kết thúc dự kiến
     */
    @Column(name = "scheduled_end_at")
    private LocalDateTime scheduledEndAt;
    
    /**
     * Thời gian ước tính (phút)
     */
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
    
    /**
     * Thời gian hoàn thành thực tế
     */
    @Column(name = "actual_completion_time")
    private LocalDateTime actualCompletionTime;
    
    /**
     * Tổng giá sau chiết khấu
     */
    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice;
    
    /**
     * Tiền tệ
     */
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "VND";
    
    /**
     * Trạng thái thanh toán
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    /**
     * Trạng thái booking
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;
    
    /**
     * Loại booking: SCHEDULED (đặt lịch trước) hoặc WALK_IN (đặt tại chỗ)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false, length = 20)
    @Builder.Default
    private BookingType bookingType = BookingType.SCHEDULED;
    
    /**
     * Ghi chú
     */
    @Column(name = "notes", length = 1000)
    private String notes;
    
    /**
     * Thời gian check-in thực tế
     */
    @Column(name = "actual_check_in_at")
    private LocalDateTime actualCheckInAt;
    
    /**
     * Thời gian bắt đầu thực tế
     */
    @Column(name = "actual_start_at")
    private LocalDateTime actualStartAt;
    
    /**
     * Thời gian kết thúc thực tế
     */
    @Column(name = "actual_end_at")
    private LocalDateTime actualEndAt;
    
    /**
     * Lý do hủy
     */
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    /**
     * Thời gian hủy
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    /**
     * Người hủy
     */
    @Column(name = "cancelled_by")
    private String cancelledBy;
    
    // Relationships
    
    /**
     * Các dịch vụ trong booking
     */
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<BookingItem> bookingItems = new ArrayList<>();
    
    
    // Business methods
    
    /**
     * Kiểm tra booking có active không
     */
    public boolean isActive() {
        return status == BookingStatus.CONFIRMED || 
               status == BookingStatus.CHECKED_IN || 
               status == BookingStatus.IN_PROGRESS ||
               status == BookingStatus.PAUSED;
    }
    
    /**
     * Kiểm tra booking có bị hủy không
     */
    public boolean isCancelled() {
        return status == BookingStatus.CANCELLED || status == BookingStatus.NO_SHOW;
    }
    
    /**
     * Kiểm tra booking có hoàn thành không
     */
    public boolean isCompleted() {
        return status == BookingStatus.COMPLETED;
    }
    
    /**
     * Kiểm tra booking có cần thanh toán không
     */
    public boolean needsPayment() {
        return paymentStatus == PaymentStatus.PENDING || paymentStatus == PaymentStatus.PARTIAL;
    }
    
    /**
     * Kiểm tra booking có đã thanh toán đủ không
     */
    public boolean isFullyPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }
    
    /**
     * Tính tổng thời gian thực tế (phút)
     */
    public Long getActualDurationMinutes() {
        if (actualStartAt != null && actualEndAt != null) {
            return java.time.Duration.between(actualStartAt, actualEndAt).toMinutes();
        }
        return null;
    }
    
    /**
     * Tính tổng thời gian dự kiến (phút)
     * Không cộng buffer vào estimated duration
     */
    public Integer getTotalEstimatedDuration() {
        // Không cộng buffer, chỉ trả về estimatedDurationMinutes
        return estimatedDurationMinutes;
    }
    
    /**
     * Cập nhật trạng thái booking
     */
    public void updateStatus(BookingStatus newStatus, String reason) {
        this.status = newStatus;
        if (reason != null) {
            this.notes = (this.notes != null ? this.notes + "\n" : "") + reason;
        }
    }
    
    /**
     * Hủy booking
     */
    public void cancelBooking(String reason, String cancelledBy) {
        this.status = BookingStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledBy = cancelledBy;
        this.cancelledAt = LocalDateTime.now();
    }
    
    /**
     * Check-in booking
     */
    public void checkIn() {
        this.status = BookingStatus.CHECKED_IN;
        this.actualCheckInAt = LocalDateTime.now();
    }
    
    /**
     * Bắt đầu thực hiện
     */
    public void startService() {
        this.status = BookingStatus.IN_PROGRESS;
        this.actualStartAt = LocalDateTime.now();
    }
    
    /**
     * Hoàn thành dịch vụ
     */
    public void completeService() {
        this.status = BookingStatus.COMPLETED;
        this.actualEndAt = LocalDateTime.now();
        this.actualCompletionTime = LocalDateTime.now();
    }
    
    /**
     * Hoàn thành dịch vụ với thời gian cụ thể
     */
    public void completeService(LocalDateTime completionTime) {
        this.status = BookingStatus.COMPLETED;
        this.actualEndAt = completionTime;
        this.actualCompletionTime = completionTime;
    }
    
    /**
     * Enum cho trạng thái thanh toán
     */
    public enum PaymentStatus {
        PENDING,    // Chờ thanh toán
        PAID,       // Đã thanh toán đủ
        PARTIAL,    // Thanh toán một phần
        REFUNDED    // Đã hoàn tiền
    }
    
    /**
     * Enum cho trạng thái booking
     */
    public enum BookingStatus {
        PENDING,        // Chờ xác nhận
        CONFIRMED,      // Đã xác nhận
        CHECKED_IN,     // Đã check-in
        IN_PROGRESS,    // Đang thực hiện
        PAUSED,         // Tạm dừng
        COMPLETED,      // Hoàn thành
        CANCELLED,      // Đã hủy
        NO_SHOW         // Khách không đến
    }

}
