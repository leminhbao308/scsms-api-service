package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity quản lý bệ dịch vụ tại chi nhánh
 * Mỗi bay đại diện cho 1 vị trí vật lý có thể phục vụ khách hàng
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_bays", schema = GeneralConstant.DB_SCHEMA_DEV)
public class ServiceBay extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "bay_id", nullable = false)
    private UUID bayId;

    /**
     * Chi nhánh sở hữu bay này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    /**
     * Tên bay (vd: "Bệ rửa xe 1", "Bệ sửa chữa 2")
     */
    @Column(name = "bay_name", nullable = false, length = 255)
    private String bayName;

    /**
     * Mã bay (vd: "BAY-001", "WASH-01")
     */
    @Column(name = "bay_code", length = 50)
    private String bayCode;

    /**
     * Mô tả bay
     */
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Trạng thái bay
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BayStatus status = BayStatus.ACTIVE;

    /**
     * Thứ tự ưu tiên hiển thị
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 1;

    /**
     * Ghi chú
     */
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * Giờ bắt đầu làm việc (mặc định 8h sáng)
     */
    @Column(name = "working_hours_start")
    @Builder.Default
    private LocalTime workingHoursStart = LocalTime.of(8, 0);

    /**
     * Giờ kết thúc làm việc (mặc định 18h chiều)
     */
    @Column(name = "working_hours_end")
    @Builder.Default
    private LocalTime workingHoursEnd = LocalTime.of(18, 0);

    /**
     * Thời gian mỗi slot (phút) - mặc định 60 phút
     */
    @Column(name = "slot_duration_minutes")
    @Builder.Default
    private Integer slotDurationMinutes = 60;

    /**
     * Thời gian đệm giữa các booking (phút) - mặc định 10 phút
     */
    @Column(name = "buffer_minutes")
    @Builder.Default
    private Integer bufferMinutes = 10;

    /**
     * Số lượng booking tối đa đồng thời - mặc định 1
     */
    @Column(name = "max_concurrent_bookings")
    @Builder.Default
    private Integer maxConcurrentBookings = 1;

    /**
     * Cho phép khách hàng booking bay này hay không
     * Mục đích: Kiểm soát bay nào được phép đặt lịch
     */
    @Column(name = "allow_booking", nullable = false)
    @Builder.Default
    private Boolean allowBooking = true;

    /**
     * Các booking sử dụng bay này
     */
    @OneToMany(mappedBy = "serviceBay", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    /**
     * Danh sách kỹ thuật viên được gán cho bay này
     * Một bay có thể có nhiều kỹ thuật viên
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "service_bay_technicians", schema = GeneralConstant.DB_SCHEMA_DEV, joinColumns = @JoinColumn(name = "bay_id"), inverseJoinColumns = @JoinColumn(name = "technician_id"))
    @Builder.Default
    private List<User> technicians = new ArrayList<>();

    // Business methods

    /**
     * Kiểm tra bay có hoạt động không
     */
    public boolean isActive() {
        return status == BayStatus.ACTIVE;
    }

    /**
     * Kiểm tra bay có cho phép booking không
     */
    public boolean isBookingAllowed() {
        return allowBooking != null && allowBooking;
    }

    /**
     * Kiểm tra bay có sẵn sàng cho booking không
     * (Hoạt động + Cho phép booking)
     */
    public boolean isAvailableForBooking() {
        return isActive() && isBookingAllowed();
    }

    /**
     * Kiểm tra bay có bị bảo trì không
     */
    public boolean isMaintenance() {
        return status == BayStatus.MAINTENANCE;
    }

    /**
     * Kiểm tra bay có bị đóng không
     */
    public boolean isClosed() {
        return status == BayStatus.CLOSED;
    }

    /**
     * Đặt bay vào trạng thái bảo trì
     */
    public void setMaintenance(String reason) {
        this.status = BayStatus.MAINTENANCE;
        this.notes = reason;
    }

    /**
     * Đóng bay
     */
    public void closeBay(String reason) {
        this.status = BayStatus.CLOSED;
        this.notes = reason;
    }

    /**
     * Kích hoạt bay
     */
    public void activateBay() {
        this.status = BayStatus.ACTIVE;
        this.notes = null;
    }

    /**
     * Cho phép booking cho bay này
     */
    public void enableBooking() {
        this.allowBooking = true;
    }

    /**
     * Cấm booking cho bay này
     */
    public void disableBooking() {
        this.allowBooking = false;
    }

    /**
     * Cấm booking với lý do
     */
    public void disableBooking(String reason) {
        this.allowBooking = false;
        this.notes = reason;
    }

    // Technician management methods

    /**
     * Thêm kỹ thuật viên vào bay
     */
    public void addTechnician(User technician) {
        if (technician != null && !this.technicians.contains(technician)) {
            this.technicians.add(technician);
        }
    }

    /**
     * Xóa kỹ thuật viên khỏi bay
     */
    public void removeTechnician(User technician) {
        if (technician != null) {
            this.technicians.remove(technician);
        }
    }

    /**
     * Kiểm tra kỹ thuật viên có trong bay không
     */
    public boolean hasTechnician(User technician) {
        return technician != null && this.technicians.contains(technician);
    }

    /**
     * Lấy số lượng kỹ thuật viên
     */
    public int getTechnicianCount() {
        return this.technicians != null ? this.technicians.size() : 0;
    }

    /**
     * Kiểm tra bay có kỹ thuật viên không
     */
    public boolean hasTechnicians() {
        return getTechnicianCount() > 0;
    }

    // Schedule management methods

    /**
     * Kiểm tra bay có hoạt động trong giờ làm việc không
     */
    public boolean isWorkingHours(java.time.LocalTime currentTime) {
        return !currentTime.isBefore(workingHoursStart) && currentTime.isBefore(workingHoursEnd);
    }

    /**
     * Lấy số slot trong ngày
     */
    public int getTotalSlotsPerDay() {
        return (int) java.time.Duration.between(workingHoursStart, workingHoursEnd).toHours();
    }

    /**
     * Kiểm tra slot có hợp lệ không
     */
    public boolean isValidSlot(int hour) {
        LocalTime slotTime = LocalTime.of(hour, 0);
        return !slotTime.isBefore(workingHoursStart) && slotTime.isBefore(workingHoursEnd);
    }

    /**
     * Tính thời gian kết thúc dự kiến cho slot
     */
    public java.time.LocalTime calculateSlotEndTime(java.time.LocalTime startTime, int serviceDurationMinutes) {
        return startTime.plusMinutes(serviceDurationMinutes + bufferMinutes);
    }

    /**
     * Enum cho trạng thái bay
     */
    public enum BayStatus {
        ACTIVE, // Hoạt động bình thường
        MAINTENANCE, // Đang bảo trì
        CLOSED, // Tạm đóng
        INACTIVE // Không hoạt động
    }
}
