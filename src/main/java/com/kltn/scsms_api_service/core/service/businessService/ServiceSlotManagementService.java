package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.serviceSlotManagement.ServiceSlotInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceSlotManagement.param.ServiceSlotFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceSlotManagement.request.CreateServiceSlotRequest;
import com.kltn.scsms_api_service.core.dto.serviceSlotManagement.request.UpdateServiceSlotRequest;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.ServiceSlot;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceSlotService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ServiceSlotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServiceSlotManagementService {
    
    private final ServiceSlotService serviceSlotService;
    private final BranchService branchService;
    private final ServiceSlotMapper serviceSlotMapper;
    
    /**
     * Lấy tất cả slot của chi nhánh
     */
    public List<ServiceSlotInfoDto> getAllSlotsByBranch(UUID branchId) {
        log.info("Getting all slots for branch: {}", branchId);
        List<ServiceSlot> slots = serviceSlotService.getByBranch(branchId);
        return slots.stream()
                .map(serviceSlotMapper::toServiceSlotInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy slot theo chi nhánh với phân trang và filter
     */
    public Page<ServiceSlotInfoDto> getAllSlotsByBranch(ServiceSlotFilterParam filterParam) {
        log.info("Getting slots for branch: {} with filter: {}", filterParam.getBranchId(), filterParam);
        
        // Standardize filter
        filterParam = filterParam.standardizeFilterRequest(filterParam);
        
        // Create pageable
        Sort sort = Sort.by(
            filterParam.getDirection().equalsIgnoreCase("DESC") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, 
            filterParam.getSort()
        );
        Pageable pageable = PageRequest.of(filterParam.getPage(), filterParam.getSize(), sort);
        
        // Get slots with filtering
        Page<ServiceSlot> slotPage = serviceSlotService.getByBranchWithPagination(filterParam.getBranchId(), pageable);
        
        return slotPage.map(serviceSlotMapper::toServiceSlotInfoDto);
    }
    
    /**
     * Lấy slot theo ID
     */
    public ServiceSlotInfoDto getSlotById(UUID slotId) {
        log.info("Getting slot by ID: {}", slotId);
        ServiceSlot slot = serviceSlotService.getById(slotId);
        return serviceSlotMapper.toServiceSlotInfoDto(slot);
    }
    
    /**
     * Lấy slot theo chi nhánh và ngày
     */
    public List<ServiceSlotInfoDto> getSlotsByBranchAndDate(UUID branchId, LocalDate slotDate) {
        log.info("Getting slots for branch: {} on date: {}", branchId, slotDate);
        List<ServiceSlot> slots = serviceSlotService.getByBranchAndDate(branchId, slotDate);
        return slots.stream()
                .map(serviceSlotMapper::toServiceSlotInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy slot khả dụng theo chi nhánh và ngày
     */
    public List<ServiceSlotInfoDto> getAvailableSlotsByBranchAndDate(UUID branchId, LocalDate slotDate) {
        log.info("Getting available slots for branch: {} on date: {}", branchId, slotDate);
        List<ServiceSlot> slots = serviceSlotService.getAvailableSlotsByBranchAndDate(branchId, slotDate);
        return slots.stream()
                .map(serviceSlotMapper::toServiceSlotInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy slot theo loại
     */
    public List<ServiceSlotInfoDto> getSlotsByCategory(UUID branchId, LocalDate slotDate, 
                                                      ServiceSlot.SlotCategory category) {
        log.info("Getting slots for branch: {} on date: {} with category: {}", branchId, slotDate, category);
        List<ServiceSlot> slots = serviceSlotService.getByBranchAndDateAndCategory(branchId, slotDate, category);
        return slots.stream()
                .map(serviceSlotMapper::toServiceSlotInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy slot khả dụng theo loại
     */
    public List<ServiceSlotInfoDto> getAvailableSlotsByCategory(UUID branchId, LocalDate slotDate, 
                                                               ServiceSlot.SlotCategory category) {
        log.info("Getting available slots for branch: {} on date: {} with category: {}", branchId, slotDate, category);
        List<ServiceSlot> slots = serviceSlotService.getAvailableSlotsByBranchAndDateAndCategory(branchId, slotDate, category);
        return slots.stream()
                .map(serviceSlotMapper::toServiceSlotInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy slot VIP khả dụng
     */
    public List<ServiceSlotInfoDto> getAvailableVipSlots(UUID branchId, LocalDate slotDate) {
        log.info("Getting available VIP slots for branch: {} on date: {}", branchId, slotDate);
        List<ServiceSlot> slots = serviceSlotService.getAvailableVipSlots(branchId, slotDate);
        return slots.stream()
                .map(serviceSlotMapper::toServiceSlotInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy slot trong khoảng thời gian
     */
    public List<ServiceSlotInfoDto> getSlotsInTimeRange(UUID branchId, LocalDate slotDate, 
                                                       LocalTime startTime, LocalTime endTime) {
        log.info("Getting slots for branch: {} on date: {} in time range: {} - {}", 
                branchId, slotDate, startTime, endTime);
        List<ServiceSlot> slots = serviceSlotService.findAvailableSlotsInTimeRange(branchId, slotDate, startTime, endTime);
        return slots.stream()
                .map(serviceSlotMapper::toServiceSlotInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy slot theo khoảng ngày
     */
    public List<ServiceSlotInfoDto> getSlotsByDateRange(UUID branchId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting slots for branch: {} in date range: {} - {}", branchId, startDate, endDate);
        List<ServiceSlot> slots = serviceSlotService.getByBranchAndDateRange(branchId, startDate, endDate);
        return slots.stream()
                .map(serviceSlotMapper::toServiceSlotInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy slot trong tương lai
     */
    public List<ServiceSlotInfoDto> getFutureAvailableSlots(UUID branchId, LocalDate fromDate) {
        log.info("Getting future available slots for branch: {} from date: {}", branchId, fromDate);
        List<ServiceSlot> slots = serviceSlotService.getFutureAvailableSlots(branchId, fromDate);
        return slots.stream()
                .map(serviceSlotMapper::toServiceSlotInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Tạo slot mới
     */
    @Transactional
    public ServiceSlotInfoDto createSlot(CreateServiceSlotRequest request) {
        log.info("Creating service slot for branch: {} on date: {}", 
                request.getBranchId(), request.getSlotDate());
        
        // Validate branch exists
        Branch branch = branchService.findById(request.getBranchId())
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Branch not found with ID: " + request.getBranchId()));
        
        // Create slot entity
        ServiceSlot slot = serviceSlotMapper.toEntity(request);
        slot.setBranch(branch);
        
        // Set default values
        if (slot.getStatus() == null) {
            slot.setStatus(ServiceSlot.SlotStatus.AVAILABLE);
        }
        if (slot.getPriorityOrder() == null) {
            slot.setPriorityOrder(1);
        }
        
        ServiceSlot savedSlot = serviceSlotService.save(slot);
        
        return serviceSlotMapper.toServiceSlotInfoDto(savedSlot);
    }
    
    /**
     * Cập nhật slot
     */
    @Transactional
    public ServiceSlotInfoDto updateSlot(UUID slotId, UpdateServiceSlotRequest request) {
        log.info("Updating service slot: {}", slotId);
        
        // Get existing slot
        ServiceSlot existingSlot = serviceSlotService.getById(slotId);
        
        // Update slot
        ServiceSlot updatedSlot = serviceSlotMapper.updateEntity(existingSlot, request);
        ServiceSlot savedSlot = serviceSlotService.update(updatedSlot);
        
        return serviceSlotMapper.toServiceSlotInfoDto(savedSlot);
    }
    
    /**
     * Xóa slot
     */
    @Transactional
    public void deleteSlot(UUID slotId) {
        log.info("Deleting service slot: {}", slotId);
        serviceSlotService.softDeleteById(slotId);
    }
    
    /**
     * Đóng slot
     */
    @Transactional
    public void closeSlot(UUID slotId, String reason) {
        log.info("Closing service slot: {} with reason: {}", slotId, reason);
        serviceSlotService.closeSlot(slotId, reason);
    }
    
    /**
     * Mở lại slot
     */
    @Transactional
    public void openSlot(UUID slotId) {
        log.info("Opening service slot: {}", slotId);
        serviceSlotService.openSlot(slotId);
    }
    
    /**
     * Tạo nhiều slot cùng lúc (bulk create)
     */
    @Transactional
    public List<ServiceSlotInfoDto> createMultipleSlots(List<CreateServiceSlotRequest> requests) {
        log.info("Creating {} service slots", requests.size());
        
        List<ServiceSlotInfoDto> createdSlots = requests.stream()
                .map(this::createSlot)
                .collect(Collectors.toList());
        
        log.info("Successfully created {} service slots", createdSlots.size());
        return createdSlots;
    }
    
    /**
     * Tạo slot theo pattern (ví dụ: tạo slot cho cả tuần)
     */
    @Transactional
    public List<ServiceSlotInfoDto> createSlotsByPattern(UUID branchId, LocalDate startDate, LocalDate endDate,
                                                        LocalTime startTime, LocalTime endTime,
                                                        ServiceSlot.SlotCategory category, Integer priorityOrder) {
        log.info("Creating slots by pattern for branch: {} from {} to {}", branchId, startDate, endDate);
        
        List<ServiceSlotInfoDto> createdSlots = startDate.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    CreateServiceSlotRequest request = CreateServiceSlotRequest.builder()
                            .branchId(branchId)
                            .slotDate(date)
                            .startTime(startTime)
                            .endTime(endTime)
                            .slotCategory(category)
                            .priorityOrder(priorityOrder)
                            .status(ServiceSlot.SlotStatus.AVAILABLE)
                            .build();
                    return createSlot(request);
                })
                .collect(Collectors.toList());
        
        log.info("Successfully created {} slots by pattern", createdSlots.size());
        return createdSlots;
    }
    
    /**
     * Lấy thống kê slot
     */
    public SlotStatisticsDto getSlotStatistics(UUID branchId, LocalDate slotDate) {
        log.info("Getting slot statistics for branch: {} on date: {}", branchId, slotDate);
        
        long totalSlots = serviceSlotService.countByBranchAndDate(branchId, slotDate);
        long availableSlots = serviceSlotService.countAvailableByBranchAndDate(branchId, slotDate);
        long bookedSlots = totalSlots - availableSlots;
        
        return SlotStatisticsDto.builder()
                .totalSlots(totalSlots)
                .availableSlots(availableSlots)
                .bookedSlots(bookedSlots)
                .utilizationRate(totalSlots > 0 ? (double) bookedSlots / totalSlots * 100 : 0)
                .build();
    }
    
    /**
     * DTO cho thống kê slot
     */
    @lombok.Data
    @lombok.Builder
    public static class SlotStatisticsDto {
        private long totalSlots;
        private long availableSlots;
        private long bookedSlots;
        private double utilizationRate;
    }
}
