package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.ServiceProcessTrackingInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request.*;
import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.exception.ServerSideException;
import com.kltn.scsms_api_service.mapper.ServiceProcessTrackingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Business service quản lý theo dõi quá trình thực hiện dịch vụ
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceProcessTrackingManagementService {
    
    private final ServiceProcessTrackingService serviceProcessTrackingService;
    private final ServiceProcessStepService serviceProcessStepService;
    private final BookingService bookingService;
    private final UserService userService;
    private final ServiceBayService serviceBayService;
    private final ServiceProcessTrackingMapper serviceProcessTrackingMapper;
    
    /**
     * Tạo tracking mới cho booking
     */
    public ServiceProcessTrackingInfoDto createTracking(CreateServiceProcessTrackingRequest request) {
        log.info("Creating tracking for booking: {}", request.getBookingId());
        
        // Validate booking exists
        Booking booking = bookingService.getById(request.getBookingId());
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED && 
            booking.getStatus() != Booking.BookingStatus.IN_PROGRESS) {
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_CANNOT_BE_STARTED, 
                "Booking must be confirmed or in progress to create tracking");
        }
        
        // Validate service step exists
        ServiceProcessStep serviceStep = serviceProcessStepService.getById(request.getServiceStepId());
        
        // Validate technician exists
        User technician = userService.findById(request.getTechnicianId())
                .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "Technician not found"));
        
        // Validate bay exists
        ServiceBay bay = serviceBayService.getById(request.getBayId());
        
        // Create tracking
        ServiceProcessTracking tracking = ServiceProcessTracking.builder()
                .booking(booking)
                .serviceStep(serviceStep)
                .technician(technician)
                .bay(bay)
                .estimatedDuration(request.getEstimatedDuration() != null ? request.getEstimatedDuration() : serviceStep.getEstimatedTime())
                .status(request.getStatus() != null ? request.getStatus() : ServiceProcessTracking.TrackingStatus.PENDING)
                .progressPercent(request.getProgressPercent() != null ? request.getProgressPercent() : BigDecimal.ZERO)
                .notes(request.getNotes())
                .evidenceMediaUrls(request.getEvidenceMediaUrls())
                .build();
        
        ServiceProcessTracking savedTracking = serviceProcessTrackingService.save(tracking);
        return serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(savedTracking);
    }
    
    /**
     * Bắt đầu thực hiện bước
     */
    public ServiceProcessTrackingInfoDto startStep(UUID trackingId, StartStepRequest request) {
        log.info("Starting step for tracking: {}", trackingId);
        
        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        
        // Validate tracking can be started
        if (tracking.getStatus() != ServiceProcessTracking.TrackingStatus.PENDING) {
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_CANNOT_BE_STARTED,
                "Tracking must be in PENDING status to start");
        }
        
        // Get technician
        User technician = userService.findById(request.getTechnicianId())
                .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "Technician not found"));
        
        // Start step
        tracking.startStep(technician);
        if (request.getNotes() != null) {
            tracking.addNote(request.getNotes(), technician);
        }
        
        ServiceProcessTracking updatedTracking = serviceProcessTrackingService.update(tracking);
        return serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(updatedTracking);
    }
    
    /**
     * Cập nhật tiến độ
     */
    public ServiceProcessTrackingInfoDto updateProgress(UUID trackingId, ProgressUpdateRequest request) {
        log.info("Updating progress for tracking: {}", trackingId);
        
        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        
        // Validate tracking is in progress
        if (!tracking.isInProgress()) {
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_CANNOT_BE_UPDATED,
                "Tracking must be in progress to update progress");
        }
        
        // Update progress
        tracking.updateProgress(request.getProgressPercent(), tracking.getTechnician());
        if (request.getNotes() != null) {
            tracking.addNote(request.getNotes(), tracking.getTechnician());
        }
        
        ServiceProcessTracking updatedTracking = serviceProcessTrackingService.update(tracking);
        return serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(updatedTracking);
    }
    
    /**
     * Hoàn thành bước
     */
    public ServiceProcessTrackingInfoDto completeStep(UUID trackingId, CompleteStepRequest request) {
        log.info("Completing step for tracking: {}", trackingId);
        
        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        
        // Validate tracking can be completed
        if (!tracking.isInProgress()) {
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_CANNOT_BE_COMPLETED,
                "Tracking must be in progress to complete");
        }
        
        // Complete step
        tracking.completeStep();
        if (request.getNotes() != null) {
            tracking.addNote(request.getNotes(), tracking.getTechnician());
        }
        if (request.getEvidenceMediaUrls() != null && !request.getEvidenceMediaUrls().trim().isEmpty()) {
            // Parse and add media URLs
            String[] mediaUrls = request.getEvidenceMediaUrls().split(",");
            for (String mediaUrl : mediaUrls) {
                String trimmedUrl = mediaUrl.trim();
                if (!trimmedUrl.isEmpty()) {
                    tracking.addEvidenceMedia(trimmedUrl, tracking.getTechnician());
                }
            }
        }
        
        ServiceProcessTracking updatedTracking = serviceProcessTrackingService.update(tracking);
        return serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(updatedTracking);
    }
    
    /**
     * Hủy bước
     */
    public ServiceProcessTrackingInfoDto cancelStep(UUID trackingId, CancelStepRequest request) {
        log.info("Cancelling step for tracking: {}", trackingId);
        
        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        
        // Validate tracking can be cancelled
        if (tracking.isCompleted() || tracking.isCancelled()) {
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_CANNOT_BE_CANCELLED,
                "Tracking cannot be cancelled in current status");
        }
        
        // Cancel step
        tracking.cancelStep(request.getReason(), tracking.getTechnician());
        
        ServiceProcessTracking updatedTracking = serviceProcessTrackingService.update(tracking);
        return serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(updatedTracking);
    }
    
    /**
     * Lấy tracking theo booking
     */
    @Transactional(readOnly = true)
    public List<ServiceProcessTrackingInfoDto> getTrackingsByBooking(UUID bookingId) {
        log.info("Getting trackings for booking: {}", bookingId);
        
        List<ServiceProcessTracking> trackings = serviceProcessTrackingService.findByBooking(bookingId);
        return trackings.stream()
                .map(serviceProcessTrackingMapper::toServiceProcessTrackingInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy tracking theo kỹ thuật viên
     */
    @Transactional(readOnly = true)
    public List<ServiceProcessTrackingInfoDto> getTrackingsByTechnician(UUID technicianId) {
        log.info("Getting trackings for technician: {}", technicianId);
        
        List<ServiceProcessTracking> trackings = serviceProcessTrackingService.findByTechnician(technicianId);
        return trackings.stream()
                .map(serviceProcessTrackingMapper::toServiceProcessTrackingInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy tracking đang thực hiện
     */
    @Transactional(readOnly = true)
    public List<ServiceProcessTrackingInfoDto> getInProgressTrackings() {
        log.info("Getting in-progress trackings");
        
        List<ServiceProcessTracking> trackings = serviceProcessTrackingService.findInProgressTrackings();
        return trackings.stream()
                .map(serviceProcessTrackingMapper::toServiceProcessTrackingInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy tracking theo bay
     */
    @Transactional(readOnly = true)
    public List<ServiceProcessTrackingInfoDto> getTrackingsByBay(UUID bayId) {
        log.info("Getting trackings for bay: {}", bayId);
        
        List<ServiceProcessTracking> trackings = serviceProcessTrackingService.findByBay(bayId);
        return trackings.stream()
                .map(serviceProcessTrackingMapper::toServiceProcessTrackingInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy tất cả tracking với phân trang
     */
    @Transactional(readOnly = true)
    public Page<ServiceProcessTrackingInfoDto> getAllTrackings(Pageable pageable) {
        log.info("Getting all trackings with pagination");
        
        Page<ServiceProcessTracking> trackings = serviceProcessTrackingService.findAll(pageable);
        return trackings.map(serviceProcessTrackingMapper::toServiceProcessTrackingInfoDto);
    }
    
    /**
     * Lấy tracking theo ID
     */
    @Transactional(readOnly = true)
    public ServiceProcessTrackingInfoDto getTrackingById(UUID trackingId) {
        log.info("Getting tracking by ID: {}", trackingId);
        
        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        return serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(tracking);
    }
    
    /**
     * Cập nhật tracking
     */
    public ServiceProcessTrackingInfoDto updateTracking(UUID trackingId, UpdateServiceProcessTrackingRequest request) {
        log.info("Updating tracking: {}", trackingId);
        
        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        serviceProcessTrackingMapper.updateEntity(tracking, request);
        
        ServiceProcessTracking updatedTracking = serviceProcessTrackingService.update(tracking);
        return serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(updatedTracking);
    }
    
    /**
     * Xóa tracking
     */
    public void deleteTracking(UUID trackingId) {
        log.info("Deleting tracking: {}", trackingId);
        
        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        
        // Validate tracking can be deleted
        if (tracking.isInProgress()) {
            throw new ServerSideException(ErrorCode.SERVICE_PROCESS_TRACKING_DELETE_FAILED,
                "Cannot delete tracking that is in progress");
        }
        
        serviceProcessTrackingService.delete(trackingId);
    }
    
    /**
     * Lấy thống kê hiệu suất theo kỹ thuật viên
     */
    @Transactional(readOnly = true)
    public BigDecimal getTechnicianEfficiency(UUID technicianId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting efficiency for technician: {} from {} to {}", technicianId, startDate, endDate);
        
        Double efficiency = serviceProcessTrackingService.getAverageEfficiencyByTechnician(technicianId, startDate, endDate);
        return efficiency != null ? BigDecimal.valueOf(efficiency) : BigDecimal.ZERO;
    }
    
    /**
     * Lấy tổng thời gian làm việc theo kỹ thuật viên
     */
    @Transactional(readOnly = true)
    public Long getTechnicianTotalWorkTime(UUID technicianId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting total work time for technician: {} from {} to {}", technicianId, startDate, endDate);
        
        return serviceProcessTrackingService.sumActualDurationByTechnician(technicianId, startDate, endDate);
    }
    
    /**
     * Thêm media evidence cho tracking
     */
    public ServiceProcessTrackingInfoDto addEvidenceMedia(UUID trackingId, String mediaUrl) {
        log.info("Adding evidence media for tracking: {}", trackingId);
        
        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        
        // Add evidence media
        tracking.addEvidenceMedia(mediaUrl, tracking.getTechnician());
        
        ServiceProcessTracking updatedTracking = serviceProcessTrackingService.update(tracking);
        return serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(updatedTracking);
    }
}
