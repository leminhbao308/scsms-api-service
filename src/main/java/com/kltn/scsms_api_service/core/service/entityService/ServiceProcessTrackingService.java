package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.ServiceProcessTracking;
import com.kltn.scsms_api_service.core.repository.ServiceProcessTrackingRepository;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.exception.ServerSideException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServiceProcessTrackingService {
    
    private final ServiceProcessTrackingRepository serviceProcessTrackingRepository;
    
    /**
     * Lưu tracking mới
     */
    @Transactional
    public ServiceProcessTracking save(ServiceProcessTracking tracking) {
        try {
            log.info("Saving service process tracking for booking: {}", tracking.getBooking().getBookingId());
            return serviceProcessTrackingRepository.save(tracking);
        } catch (Exception e) {
            log.error("Error saving service process tracking: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_SAVE_FAILED, e.getMessage());
        }
    }
    
    /**
     * Cập nhật tracking
     */
    @Transactional
    public ServiceProcessTracking update(ServiceProcessTracking tracking) {
        try {
            log.info("Updating service process tracking: {}", tracking.getTrackingId());
            return serviceProcessTrackingRepository.save(tracking);
        } catch (Exception e) {
            log.error("Error updating service process tracking: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_UPDATE_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tìm tracking theo ID
     */
    public Optional<ServiceProcessTracking> findById(UUID trackingId) {
        try {
            return serviceProcessTrackingRepository.findById(trackingId);
        } catch (Exception e) {
            log.error("Error finding service process tracking by ID: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Lấy tracking theo ID (throw exception nếu không tìm thấy)
     */
    public ServiceProcessTracking getById(UUID trackingId) {
        return findById(trackingId)
                .orElseThrow(() -> new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_NOT_FOUND));
    }
    
    /**
     * Lấy tất cả tracking với phân trang
     */
    public Page<ServiceProcessTracking> findAll(Pageable pageable) {
        try {
            return serviceProcessTrackingRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("Error finding all service process tracking: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tìm tracking theo booking
     */
    public List<ServiceProcessTracking> findByBooking(UUID bookingId) {
        try {
            return serviceProcessTrackingRepository.findByBooking_BookingIdOrderByCreatedDateAsc(bookingId);
        } catch (Exception e) {
            log.error("Error finding service process tracking by booking: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tìm tracking theo kỹ thuật viên
     */
    public List<ServiceProcessTracking> findByTechnician(UUID technicianId) {
        try {
            return serviceProcessTrackingRepository.findByTechnician_UserIdOrderByStartTimeDesc(technicianId);
        } catch (Exception e) {
            log.error("Error finding service process tracking by technician: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tìm tracking theo slot
     */
    public List<ServiceProcessTracking> findBySlot(UUID slotId) {
        try {
            return serviceProcessTrackingRepository.findBySlot_SlotIdOrderByStartTimeDesc(slotId);
        } catch (Exception e) {
            log.error("Error finding service process tracking by slot: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tìm tracking theo trạng thái
     */
    public List<ServiceProcessTracking> findByStatus(ServiceProcessTracking.TrackingStatus status) {
        try {
            return serviceProcessTrackingRepository.findByStatusOrderByStartTimeDesc(status);
        } catch (Exception e) {
            log.error("Error finding service process tracking by status: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tìm tracking theo booking và trạng thái
     */
    public List<ServiceProcessTracking> findByBookingAndStatus(UUID bookingId, ServiceProcessTracking.TrackingStatus status) {
        try {
            return serviceProcessTrackingRepository.findByBooking_BookingIdAndStatusOrderByCreatedDateAsc(bookingId, status);
        } catch (Exception e) {
            log.error("Error finding service process tracking by booking and status: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tìm tracking đang thực hiện
     */
    public List<ServiceProcessTracking> findInProgressTrackings() {
        try {
            return serviceProcessTrackingRepository.findInProgressTrackings();
        } catch (Exception e) {
            log.error("Error finding in-progress trackings: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tìm tracking theo khoảng thời gian
     */
    public List<ServiceProcessTracking> findByTimeRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return serviceProcessTrackingRepository.findByTimeRange(startDate, endDate);
        } catch (Exception e) {
            log.error("Error finding service process tracking by time range: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tìm tracking theo kỹ thuật viên và khoảng thời gian
     */
    public List<ServiceProcessTracking> findByTechnicianAndTimeRange(UUID technicianId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return serviceProcessTrackingRepository.findByTechnicianAndTimeRange(technicianId, startDate, endDate);
        } catch (Exception e) {
            log.error("Error finding service process tracking by technician and time range: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tìm tracking theo slot và khoảng thời gian
     */
    public List<ServiceProcessTracking> findBySlotAndTimeRange(UUID slotId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return serviceProcessTrackingRepository.findBySlotAndTimeRange(slotId, startDate, endDate);
        } catch (Exception e) {
            log.error("Error finding service process tracking by slot and time range: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tìm tracking có tiến độ thấp
     */
    public List<ServiceProcessTracking> findLowProgressTrackings(BigDecimal threshold, LocalDateTime thresholdTime) {
        try {
            return serviceProcessTrackingRepository.findLowProgressTrackings(threshold, thresholdTime);
        } catch (Exception e) {
            log.error("Error finding low progress trackings: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tìm tracking cuối cùng của booking
     */
    public Optional<ServiceProcessTracking> findLatestTrackingForBooking(UUID bookingId) {
        try {
            return Optional.ofNullable(serviceProcessTrackingRepository.findLatestTrackingForBooking(bookingId));
        } catch (Exception e) {
            log.error("Error finding latest tracking for booking: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Kiểm tra xem có tracking nào đang thực hiện cho booking không
     */
    public boolean existsInProgressTrackingForBooking(UUID bookingId) {
        try {
            return serviceProcessTrackingRepository.existsInProgressTrackingForBooking(bookingId);
        } catch (Exception e) {
            log.error("Error checking in-progress tracking for booking: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Đếm tracking theo trạng thái
     */
    public long countByStatus(ServiceProcessTracking.TrackingStatus status) {
        try {
            return serviceProcessTrackingRepository.countByStatus(status);
        } catch (Exception e) {
            log.error("Error counting service process tracking by status: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Đếm tracking theo kỹ thuật viên
     */
    public long countByTechnician(UUID technicianId) {
        try {
            return serviceProcessTrackingRepository.countByTechnician_UserId(technicianId);
        } catch (Exception e) {
            log.error("Error counting service process tracking by technician: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tính tổng thời gian thực tế theo kỹ thuật viên
     */
    public Long sumActualDurationByTechnician(UUID technicianId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return serviceProcessTrackingRepository.sumActualDurationByTechnician(technicianId, startDate, endDate);
        } catch (Exception e) {
            log.error("Error summing actual duration by technician: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tính tổng thời gian ước lượng theo kỹ thuật viên
     */
    public Long sumEstimatedDurationByTechnician(UUID technicianId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return serviceProcessTrackingRepository.sumEstimatedDurationByTechnician(technicianId, startDate, endDate);
        } catch (Exception e) {
            log.error("Error summing estimated duration by technician: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Tính hiệu suất trung bình theo kỹ thuật viên
     */
    public Double getAverageEfficiencyByTechnician(UUID technicianId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return serviceProcessTrackingRepository.getAverageEfficiencyByTechnician(technicianId, startDate, endDate);
        } catch (Exception e) {
            log.error("Error getting average efficiency by technician: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_FIND_FAILED, e.getMessage());
        }
    }
    
    /**
     * Xóa tracking
     */
    @Transactional
    public void delete(UUID trackingId) {
        try {
            log.info("Deleting service process tracking: {}", trackingId);
            serviceProcessTrackingRepository.deleteById(trackingId);
        } catch (Exception e) {
            log.error("Error deleting service process tracking: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_DELETE_FAILED, e.getMessage());
        }
    }
    
    /**
     * Xóa tracking theo booking
     */
    @Transactional
    public void deleteByBooking(UUID bookingId) {
        try {
            log.info("Deleting service process tracking by booking: {}", bookingId);
            List<ServiceProcessTracking> trackings = findByBooking(bookingId);
            serviceProcessTrackingRepository.deleteAll(trackings);
        } catch (Exception e) {
            log.error("Error deleting service process tracking by booking: {}", e.getMessage(), e);
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_DELETE_FAILED, e.getMessage());
        }
    }
}
