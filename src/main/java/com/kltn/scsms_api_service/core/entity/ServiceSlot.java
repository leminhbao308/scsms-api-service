package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entity quản lý slot chăm sóc xe tại chi nhánh
 * Mỗi slot đại diện cho 1 vị trí chăm sóc xe trong 1 khoảng thời gian cụ thể
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_slots", schema = GeneralConstant.DB_SCHEMA_DEV)
public class ServiceSlot extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "slot_id", nullable = false)
    private UUID slotId;
    
    /**
     * Chi nhánh sở hữu slot này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    
    /**
     * Ngày áp dụng slot
     */
    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;
    
    /**
     * Thời gian bắt đầu slot
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    /**
     * Thời gian kết thúc slot
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    /**
     * Loại slot (Standard, VIP, Express, Maintenance)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "slot_category", nullable = false)
    @Builder.Default
    private SlotCategory slotCategory = SlotCategory.STANDARD;
    
    /**
     * Trạng thái slot
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SlotStatus status = SlotStatus.AVAILABLE;
    
    /**
     * Booking hiện tại của slot (nếu có)
     */
    @OneToOne(mappedBy = "serviceSlot", fetch = FetchType.LAZY)
    private Booking currentBooking;
    
    /**
     * Thứ tự ưu tiên của slot (1 = cao nhất)
     * Dùng để sắp xếp khi hiển thị cho admin
     */
    @Column(name = "priority_order")
    @Builder.Default
    private Integer priorityOrder = 1;
    
    /**
     * Ghi chú cho slot (lý do đóng, thông tin đặc biệt...)
     */
    @Column(name = "notes", length = 500)
    private String notes;
    
    
    // Business methods
    
    /**
     * Kiểm tra slot có khả dụng không
     */
    public boolean isAvailable() {
        return status == SlotStatus.AVAILABLE && currentBooking == null;
    }
    
    /**
     * Kiểm tra slot có bị đóng không
     */
    public boolean isClosed() {
        return status == SlotStatus.CLOSED;
    }
    
    /**
     * Kiểm tra slot có phải VIP không
     */
    public boolean isVipSlot() {
        return slotCategory == SlotCategory.VIP;
    }
    
    /**
     * Kiểm tra slot có phải maintenance không
     */
    public boolean isMaintenanceSlot() {
        return slotCategory == SlotCategory.MAINTENANCE;
    }
    
    /**
     * Tính thời lượng slot (phút)
     */
    public long getDurationInMinutes() {
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }
    
    /**
     * Kiểm tra slot có trùng thời gian với slot khác không
     */
    public boolean overlapsWith(ServiceSlot other) {
        if (!this.slotDate.equals(other.slotDate)) {
            return false;
        }
        
        return !(this.endTime.isBefore(other.startTime) || 
                this.startTime.isAfter(other.endTime));
    }
    
    /**
     * Đóng slot
     */
    public void closeSlot(String reason) {
        this.status = SlotStatus.CLOSED;
        this.notes = reason;
    }
    
    /**
     * Mở lại slot
     */
    public void openSlot() {
        this.status = SlotStatus.AVAILABLE;
        this.notes = null;
    }
    
    /**
     * Đặt booking cho slot này
     */
    public void assignBooking(Booking booking) {
        this.currentBooking = booking;
        this.status = SlotStatus.BOOKED;
    }
    
    /**
     * Hủy booking khỏi slot
     */
    public void unassignBooking() {
        this.currentBooking = null;
        this.status = SlotStatus.AVAILABLE;
    }
    
    
    /**
     * Enum cho trạng thái slot
     */
    public enum SlotStatus {
        AVAILABLE,  // Còn trống, có thể đặt
        BOOKED,     // Đã được đặt (có booking)
        CLOSED      // Tạm ngừng (chi nhánh nghỉ, bảo trì, ...)
    }
    
    /**
     * Enum cho loại slot
     */
    public enum SlotCategory {
        STANDARD,     // Slot thường
        VIP,          // Slot khu VIP (ưu tiên cao)
        EXPRESS,      // Slot nhanh (dịch vụ ngắn)
        MAINTENANCE   // Slot nội bộ bảo trì, không cho khách đặt
    }
}
