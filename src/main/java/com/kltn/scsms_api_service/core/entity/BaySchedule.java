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
 * Entity quản lý lịch trình của từng bay trong ngày
 * Mỗi bay sẽ có các slot thời gian có thể đặt booking
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bay_schedules", schema = GeneralConstant.DB_SCHEMA_DEV)
public class BaySchedule extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;
    
    /**
     * Bay được lên lịch
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bay_id", nullable = false)
    private ServiceBay serviceBay;
    
    /**
     * Ngày lên lịch
     */
    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;
    
    /**
     * Giờ bắt đầu slot
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    /**
     * Giờ kết thúc slot
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    /**
     * Trạng thái slot
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.AVAILABLE;
    
    /**
     * Booking sử dụng slot này (nullable nếu AVAILABLE)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;
    
    /**
     * Thời gian bắt đầu thực tế (khi check-in)
     */
    @Column(name = "actual_start_time")
    private LocalTime actualStartTime;
    
    /**
     * Thời gian kết thúc thực tế (khi hoàn thành)
     */
    @Column(name = "actual_end_time")
    private LocalTime actualEndTime;
    
    /**
     * Ghi chú cho slot
     */
    @Column(name = "notes", length = 500)
    private String notes;
    
    // Business methods
    
    /**
     * Kiểm tra slot có sẵn sàng không
     */
    public boolean isAvailable() {
        return status == ScheduleStatus.AVAILABLE;
    }
    
    /**
     * Kiểm tra slot có được đặt không
     */
    public boolean isBooked() {
        return status == ScheduleStatus.BOOKED;
    }
    
    /**
     * Kiểm tra slot có đang thực hiện không
     */
    public boolean isInProgress() {
        return status == ScheduleStatus.IN_PROGRESS;
    }
    
    /**
     * Kiểm tra slot có hoàn thành không
     */
    public boolean isCompleted() {
        return status == ScheduleStatus.COMPLETED;
    }
    
    /**
     * Kiểm tra slot có bị hủy không
     */
    public boolean isCancelled() {
        return status == ScheduleStatus.CANCELLED;
    }
    
    /**
     * Đặt slot cho booking
     */
    public void bookSlot(Booking booking) {
        if (!isAvailable()) {
            throw new IllegalStateException("Cannot book slot that is not available");
        }
        this.booking = booking;
        this.status = ScheduleStatus.BOOKED;
    }
    
    /**
     * Bắt đầu thực hiện dịch vụ
     */
    public void startService() {
        if (!isBooked()) {
            throw new IllegalStateException("Cannot start service for slot that is not booked");
        }
        this.status = ScheduleStatus.IN_PROGRESS;
        this.actualStartTime = LocalTime.now();
    }
    
    /**
     * Hoàn thành dịch vụ
     */
    public void completeService() {
        if (!isInProgress()) {
            throw new IllegalStateException("Cannot complete service for slot that is not in progress");
        }
        this.status = ScheduleStatus.COMPLETED;
        this.actualEndTime = LocalTime.now();
    }
    
    /**
     * Hoàn thành slot với thời gian cụ thể
     */
    public void completeService(LocalTime completionTime) {
        if (!isInProgress()) {
            throw new IllegalStateException("Cannot complete service for slot that is not in progress");
        }
        this.status = ScheduleStatus.COMPLETED;
        this.actualEndTime = completionTime;
    }
    
    /**
     * Hủy slot
     */
    public void cancelSlot(String reason) {
        this.status = ScheduleStatus.CANCELLED;
        this.notes = reason;
        this.booking = null;
    }
    
    /**
     * Giải phóng slot (trở về trạng thái available)
     */
    public void releaseSlot() {
        this.status = ScheduleStatus.AVAILABLE;
        this.booking = null;
        this.actualStartTime = null;
        this.actualEndTime = null;
        this.notes = null;
    }
    
    /**
     * Kiểm tra slot có hoàn thành sớm không
     */
    public boolean isCompletedEarly() {
        if (!isCompleted() || actualEndTime == null || endTime == null) {
            return false;
        }
        return actualEndTime.isBefore(endTime);
    }
    
    /**
     * Tính số phút hoàn thành sớm
     */
    public int getEarlyCompletionMinutes() {
        if (!isCompletedEarly()) {
            return 0;
        }
        return (int) java.time.Duration.between(actualEndTime, endTime).toMinutes();
    }
    
    /**
     * Enum cho trạng thái slot
     */
    public enum ScheduleStatus {
        AVAILABLE,      // Có thể đặt
        BOOKED,         // Đã được đặt
        IN_PROGRESS,    // Đang thực hiện
        COMPLETED,      // Hoàn thành
        CANCELLED       // Đã hủy
    }
}
