package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.serviceBayManagement.ServiceBayInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.ServiceBayStatisticsDto;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.TechnicianInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.request.BayAvailabilityRequest;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.request.CreateServiceBayRequest;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.request.ServiceBayFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.request.UpdateServiceBayRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceBayService;
import com.kltn.scsms_api_service.core.service.entityService.BookingService;
import com.kltn.scsms_api_service.core.service.entityService.UserService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ServiceBayMapper;
import com.kltn.scsms_api_service.mapper.BookingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceBayManagementService {
    
    private final ServiceBayService serviceBayService;
    private final BranchService branchService;
    private final BookingService bookingService;
    private final UserService userService;
    private final ServiceBayMapper serviceBayMapper;
    private final BookingMapper bookingMapper;
    
    /**
     * Lấy tất cả service bays với filter và pagination
     */
    public Page<ServiceBayInfoDto> getAllServiceBays(ServiceBayFilterParam filterParam) {
        log.info("Getting all service bays with filters: {}", filterParam);
        
        List<ServiceBay> bays;
        
        if (filterParam.getBranchId() != null) {
            if (filterParam.getStatus() != null) {
                bays = serviceBayService.getByBranch(filterParam.getBranchId())
                        .stream()
                        .filter(bay -> bay.getStatus() == filterParam.getStatus())
                        .collect(Collectors.toList());
            } else {
                bays = serviceBayService.getByBranch(filterParam.getBranchId());
            }
        } else {
            // If no filters, get all service bays
            bays = serviceBayService.findAll();
        }
        
        // Apply keyword filter
        if (filterParam.getKeyword() != null && !filterParam.getKeyword().trim().isEmpty()) {
            if (filterParam.getBranchId() != null) {
                bays = serviceBayService.searchByKeywordInBranch(filterParam.getBranchId(), filterParam.getKeyword());
            } else {
                bays = serviceBayService.searchByKeyword(filterParam.getKeyword());
            }
        }
        
        // Apply active filter
        if (filterParam.getIsActive() != null) {
            bays = bays.stream()
                    .filter(bay -> bay.getIsActive().equals(filterParam.getIsActive()))
                    .collect(Collectors.toList());
        }
        
        List<ServiceBayInfoDto> bayDtos = serviceBayMapper.toServiceBayInfoDtoList(bays);
        
        // Simple pagination
        int page = filterParam.getPage();
        int size = filterParam.getSize();
        int start = page * size;
        int end = Math.min((start + size), bayDtos.size());
        
        // Fix: Check if start is within bounds
        List<ServiceBayInfoDto> pageContent;
        if (start >= bayDtos.size()) {
            // If start is beyond the list size, return empty list
            pageContent = new ArrayList<>();
        } else {
            pageContent = bayDtos.subList(start, end);
        }
        
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(pageContent, pageable, bayDtos.size());
    }
    
    /**
     * Lấy service bays cho dropdown
     */
    public List<ServiceBayInfoDto> getServiceBaysDropdown(UUID branchId) {
        log.info("Getting service bays dropdown for branch: {}", branchId);
        
        List<ServiceBay> bays;
        if (branchId != null) {
            bays = serviceBayService.getActiveByBranch(branchId);
        } else {
            bays = serviceBayService.findAll();
        }
        
        return serviceBayMapper.toServiceBayInfoDtoList(bays);
    }
    
    /**
     * Lấy service bay theo ID
     */
    public ServiceBayInfoDto getServiceBayById(UUID bayId) {
        log.info("Getting service bay by ID: {}", bayId);
        ServiceBay bay = serviceBayService.getById(bayId);
        return serviceBayMapper.toServiceBayInfoDto(bay);
    }
    
    /**
     * Lấy service bays theo branch
     */
    public List<ServiceBayInfoDto> getServiceBaysByBranch(UUID branchId) {
        log.info("Getting service bays for branch: {}", branchId);
        List<ServiceBay> bays = serviceBayService.getByBranch(branchId);
        return serviceBayMapper.toServiceBayInfoDtoList(bays);
    }
    
    
    /**
     * Lấy service bays hoạt động
     */
    public List<ServiceBayInfoDto> getActiveServiceBays(UUID branchId) {
        log.info("Getting active service bays for branch: {}", branchId);
        List<ServiceBay> bays = serviceBayService.getActiveByBranch(branchId);
        return serviceBayMapper.toServiceBayInfoDtoList(bays);
    }
    
    /**
     * Lấy service bays khả dụng trong khoảng thời gian
     */
    public List<ServiceBayInfoDto> getAvailableServiceBays(UUID branchId, String startTime, String endTime) {
        log.info("Getting available service bays for branch: {} from {} to {}", branchId, startTime, endTime);
        
        LocalDateTime start = LocalDateTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        List<ServiceBay> bays = serviceBayService.getAvailableBaysInTimeRange(branchId, start, end);
        
        return serviceBayMapper.toServiceBayInfoDtoList(bays);
    }
    
    /**
     * Tìm kiếm service bays
     */
    public List<ServiceBayInfoDto> searchServiceBays(String keyword, UUID branchId) {
        log.info("Searching service bays with keyword: {} in branch: {}", keyword, branchId);
        
        List<ServiceBay> bays;
        if (branchId != null) {
            bays = serviceBayService.searchByKeywordInBranch(branchId, keyword);
        } else {
            bays = serviceBayService.searchByKeyword(keyword);
        }
        
        return serviceBayMapper.toServiceBayInfoDtoList(bays);
    }
    
    /**
     * Tạo service bay mới
     */
    @Transactional
    public ServiceBayInfoDto createServiceBay(CreateServiceBayRequest request) {
        log.info("Creating service bay: {} for branch: {}", request.getBayName(), request.getBranchId());
        
        // Validate branch exists
        Branch branch = branchService.findById(request.getBranchId())
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Branch not found with ID: " + request.getBranchId()));
        
        // Create bay entity
        ServiceBay bay = serviceBayMapper.toEntity(request);
        bay.setBranch(branch);
        
        ServiceBay savedBay = serviceBayService.save(bay);
        
        return serviceBayMapper.toServiceBayInfoDto(savedBay);
    }
    
    /**
     * Cập nhật service bay
     */
    @Transactional
    public ServiceBayInfoDto updateServiceBay(UUID bayId, UpdateServiceBayRequest request) {
        log.info("Updating service bay: {}", bayId);
        
        // Get existing bay
        ServiceBay existingBay = serviceBayService.getById(bayId);
        
        // Update basic fields
        serviceBayMapper.updateEntity(existingBay, request);
        
        // Handle status update if provided
        if (request.getStatus() != null) {
            log.info("Updating service bay status from {} to {}", existingBay.getStatus(), request.getStatus());
            
            switch (request.getStatus()) {
                case ACTIVE:
                    existingBay.activateBay();
                    break;
                case MAINTENANCE:
                    existingBay.setMaintenance("Status updated via UI");
                    break;
                case CLOSED:
                    existingBay.closeBay("Status updated via UI");
                    break;
                case INACTIVE:
                    existingBay.setIsActive(false);
                    break;
            }
        }
        
        // Handle technician updates if provided
        if (request.getTechnicianIds() != null) {
            log.info("Updating technicians for service bay: {}", bayId);
            updateServiceBayTechnicians(existingBay, request.getTechnicianIds(), request.getDefaultTechnicianStatus());
        }
        
        ServiceBay savedBay = serviceBayService.update(existingBay);
        
        return serviceBayMapper.toServiceBayInfoDto(savedBay);
    }
    
    /**
     * Cập nhật danh sách kỹ thuật viên cho service bay
     */
    private void updateServiceBayTechnicians(ServiceBay serviceBay, List<UUID> technicianIds, 
                                           com.kltn.scsms_api_service.core.enums.TechnicianStatus defaultStatus) {
        log.info("Updating technicians for bay: {}, new technician count: {}", 
                serviceBay.getBayName(), technicianIds.size());
        
        // Clear existing technicians
        serviceBay.getTechnicians().clear();
        
        // Add new technicians
        for (UUID technicianId : technicianIds) {
            User technician = userService.findById(technicianId)
                    .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                        "Technician not found with ID: " + technicianId));
            
            // Validate that user is EMPLOYEE type
            if (technician.getUserType() != com.kltn.scsms_api_service.core.entity.enumAttribute.UserType.EMPLOYEE) {
                log.warn("User {} is not EMPLOYEE type, skipping assignment", technician.getFullName());
                continue;
            }
            
            serviceBay.addTechnician(technician);
            log.info("Added technician {} to service bay {}", technician.getFullName(), serviceBay.getBayName());
        }
    }
    
    /**
     * Xóa service bay
     */
    @Transactional
    public void deleteServiceBay(UUID bayId) {
        log.info("Deleting service bay: {}", bayId);
        serviceBayService.softDeleteById(bayId);
    }
    
    /**
     * Cập nhật trạng thái service bay
     */
    @Transactional
    public ServiceBayInfoDto updateServiceBayStatus(UUID bayId, ServiceBay.BayStatus status, String reason) {
        log.info("Updating service bay status: {} to {}", bayId, status);
        
        ServiceBay bay = serviceBayService.getById(bayId);
        
        switch (status) {
            case ACTIVE:
                bay.activateBay();
                break;
            case MAINTENANCE:
                bay.setMaintenance(reason);
                break;
            case CLOSED:
                bay.closeBay(reason);
                break;
            case INACTIVE:
                bay.setIsActive(false);
                break;
        }
        
        ServiceBay savedBay = serviceBayService.update(bay);
        return serviceBayMapper.toServiceBayInfoDto(savedBay);
    }
    
    /**
     * Kích hoạt service bay
     */
    @Transactional
    public ServiceBayInfoDto activateServiceBay(UUID bayId) {
        log.info("Activating service bay: {}", bayId);
        serviceBayService.activateBay(bayId);
        ServiceBay bay = serviceBayService.getById(bayId);
        return serviceBayMapper.toServiceBayInfoDto(bay);
    }
    
    /**
     * Đặt service bay vào trạng thái bảo trì
     */
    @Transactional
    public ServiceBayInfoDto deactivateServiceBay(UUID bayId, String reason) {
        log.info("Deactivating service bay: {} with reason: {}", bayId, reason);
        serviceBayService.setMaintenance(bayId, reason);
        ServiceBay bay = serviceBayService.getById(bayId);
        return serviceBayMapper.toServiceBayInfoDto(bay);
    }
    
    /**
     * Kiểm tra tính khả dụng của bay
     */
    public Boolean checkBayAvailability(UUID bayId, BayAvailabilityRequest request) {
        log.info("Checking availability for bay: {} from {} to {}", bayId, request.getStartTime(), request.getEndTime());
        return serviceBayService.isBayAvailableInTimeRange(bayId, request.getStartTime(), request.getEndTime());
    }
    
    /**
     * Lấy bookings của bay
     */
    public List<BookingInfoDto> getBayBookings(UUID bayId) {
        log.info("Getting bookings for bay: {}", bayId);
        
        // Validate bay exists
        serviceBayService.getById(bayId);
        
        // Get bookings for this bay
        List<Booking> bookings = bookingService.findByServiceBay(bayId);
        
        // Convert to DTOs
        return bookings.stream()
                .map(bookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy thống kê của bay
     */
    public ServiceBayStatisticsDto getBayStatistics(UUID bayId) {
        log.info("Getting statistics for bay: {}", bayId);
        ServiceBay bay = serviceBayService.getById(bayId);
        
        // Get all bookings for this bay
        List<Booking> bookings = bookingService.findByServiceBay(bayId);
        
        // Calculate statistics
        long totalBookings = bookings.size();
        long completedBookings = bookings.stream()
                .filter(booking -> booking.getStatus() == Booking.BookingStatus.COMPLETED)
                .count();
        long cancelledBookings = bookings.stream()
                .filter(booking -> booking.getStatus() == Booking.BookingStatus.CANCELLED)
                .count();
        long activeBookings = bookings.stream()
                .filter(booking -> booking.getStatus() == Booking.BookingStatus.IN_PROGRESS || 
                                 booking.getStatus() == Booking.BookingStatus.CONFIRMED)
                .count();
        
        // Calculate utilization rate (completed bookings / total bookings)
        double utilizationRate = totalBookings > 0 ? (double) completedBookings / totalBookings * 100.0 : 0.0;
        
        // Calculate average service time
        long averageServiceTimeMinutes = 0L;
        if (completedBookings > 0) {
            averageServiceTimeMinutes = bookings.stream()
                    .filter(booking -> booking.getStatus() == Booking.BookingStatus.COMPLETED)
                    .filter(booking -> booking.getEstimatedDurationMinutes() != null)
                    .mapToLong(Booking::getEstimatedDurationMinutes)
                    .sum() / completedBookings;
        }
        
        return ServiceBayStatisticsDto.builder()
                .bayId(bay.getBayId())
                .bayName(bay.getBayName())
                .bayCode(bay.getBayCode())
                .status(bay.getStatus().name())
                .totalBookings(totalBookings)
                .completedBookings(completedBookings)
                .cancelledBookings(cancelledBookings)
                .activeBookings(activeBookings)
                .utilizationRate(utilizationRate)
                .averageServiceTimeMinutes(averageServiceTimeMinutes)
                .build();
    }
    
    /**
     * Validate tên bay
     */
    public Boolean validateBayName(UUID branchId, String bayName, UUID bayId) {
        log.info("Validating bay name: {} for branch: {}", bayName, branchId);
        
        if (bayId != null) {
            return !serviceBayService.existsByBranchAndBayNameExcluding(branchId, bayName, bayId);
        } else {
            return !serviceBayService.existsByBranchAndBayName(branchId, bayName);
        }
    }
    
    
    /**
     * Lấy danh sách tất cả users (kỹ thuật viên)
     */
    public List<TechnicianInfoDto> getAllTechnicians() {
        log.info("Getting all technicians (EMPLOYEE users only)");
        
        // Create a filter param to get only EMPLOYEE users
        com.kltn.scsms_api_service.core.dto.userManagement.param.UserFilterParam filterParam = 
            new com.kltn.scsms_api_service.core.dto.userManagement.param.UserFilterParam();
        filterParam.setPage(0);
        filterParam.setSize(1000); // Large size to get all users
        filterParam.setUserType("EMPLOYEE"); // Only get EMPLOYEE users
        
        Page<User> userPage = userService.getAllUsersWithFilters(filterParam);
        List<User> users = userPage.getContent();
        
        return users.stream()
                .map(this::mapToTechnicianInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Map User entity to TechnicianInfoDto
     */
    private TechnicianInfoDto mapToTechnicianInfoDto(User technician) {
        return TechnicianInfoDto.builder()
                .technicianId(technician.getUserId())
                .technicianName(technician.getFullName())
                .technicianCode(technician.getEmail())
                .technicianPhone(technician.getPhoneNumber())
                .technicianEmail(technician.getEmail())
                .isActive(technician.getIsActive())
                .build();
    }
}
