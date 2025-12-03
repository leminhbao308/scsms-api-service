package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.DraftStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity quản lý booking draft - Lưu trữ các lựa chọn của khách hàng trong quá trình đặt lịch
 * Giúp AI không bị quên dữ liệu và đơn giản hóa quy trình đặt lịch
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "booking_drafts", schema = GeneralConstant.DB_SCHEMA_DEV)
public class BookingDraft extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "draft_id", nullable = false)
    private UUID draftId;

    /**
     * Khách hàng (nullable nếu là guest)
     */
    @Column(name = "customer_id")
    private UUID customerId;

    /**
     * Session ID từ frontend (để track conversation)
     */
    @Column(name = "session_id", length = 255)
    private String sessionId;

    /**
     * Conversation ID từ AI (optional)
     */
    @Column(name = "conversation_id", length = 255)
    private String conversationId;

    // ========== Booking Data ==========

    /**
     * Xe đã chọn
     */
    @Column(name = "vehicle_id")
    private UUID vehicleId;

    @Column(name = "vehicle_license_plate", length = 20)
    private String vehicleLicensePlate;

    /**
     * Ngày giờ đặt lịch
     */
    @Column(name = "date_time")
    private LocalDateTime dateTime;

    /**
     * Chi nhánh đã chọn
     */
    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "branch_name", length = 255)
    private String branchName;

    /**
     * Dịch vụ đã chọn
     */
    @Column(name = "service_id")
    private UUID serviceId;

    @Column(name = "service_type", length = 100)
    private String serviceType;

    /**
     * Bay đã chọn
     */
    @Column(name = "bay_id")
    private UUID bayId;

    @Column(name = "bay_name", length = 255)
    private String bayName;

    /**
     * Khung giờ đã chọn (format: "08:00")
     */
    @Column(name = "time_slot", length = 10)
    private String timeSlot;

    // ========== Metadata ==========

    /**
     * Trạng thái draft
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DraftStatus status = DraftStatus.IN_PROGRESS;

    /**
     * Bước hiện tại trong quy trình (1-7)
     */
    @Column(name = "current_step", nullable = false)
    @Builder.Default
    private Integer currentStep = 1;

    /**
     * Thời điểm hết hạn (TTL)
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Thời điểm hoạt động cuối cùng (để detect abandoned)
     */
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    // ========== Helper Methods ==========

    /**
     * Kiểm tra đã có vehicle chưa
     */
    public boolean hasVehicle() {
        return vehicleId != null || (vehicleLicensePlate != null && !vehicleLicensePlate.trim().isEmpty());
    }

    /**
     * Kiểm tra đã có date chưa
     */
    public boolean hasDate() {
        return dateTime != null;
    }

    /**
     * Kiểm tra đã có branch chưa
     */
    public boolean hasBranch() {
        return branchId != null || (branchName != null && !branchName.trim().isEmpty());
    }

    /**
     * Kiểm tra đã có service chưa
     */
    public boolean hasService() {
        return serviceId != null || (serviceType != null && !serviceType.trim().isEmpty());
    }

    /**
     * Kiểm tra đã có bay chưa
     */
    public boolean hasBay() {
        return bayId != null || (bayName != null && !bayName.trim().isEmpty());
    }

    /**
     * Kiểm tra đã có time slot chưa
     */
    public boolean hasTime() {
        return timeSlot != null && !timeSlot.trim().isEmpty();
    }

    /**
     * Kiểm tra draft đã đầy đủ chưa
     */
    public boolean isComplete() {
        return hasVehicle() && hasDate() && hasBranch() && hasService() && hasBay() && hasTime();
    }

    /**
     * Tự động cập nhật current_step dựa trên dữ liệu đã có
     */
    public void updateCurrentStep() {
        if (!hasVehicle()) {
            currentStep = 1;
        } else if (!hasDate()) {
            currentStep = 2;
        } else if (!hasBranch()) {
            currentStep = 3;
        } else if (!hasService()) {
            currentStep = 4;
        } else if (!hasBay()) {
            currentStep = 5;
        } else if (!hasTime()) {
            currentStep = 6;
        } else {
            currentStep = 7; // Đầy đủ, sẵn sàng tạo booking
        }
    }

    /**
     * Cập nhật last_activity_at khi có hoạt động mới
     */
    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }
}

