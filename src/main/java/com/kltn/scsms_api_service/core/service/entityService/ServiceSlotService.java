package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.ServiceSlot;
import com.kltn.scsms_api_service.core.repository.ServiceSlotRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceSlotService {
    
    private final ServiceSlotRepository serviceSlotRepository;
    
    /**
     * Lưu slot mới
     */
    @Transactional
    public ServiceSlot save(ServiceSlot serviceSlot) {
        log.info("Saving service slot for branch: {} on date: {}", 
                serviceSlot.getBranch().getBranchId(), serviceSlot.getSlotDate());
        
        // Validate slot không trùng thời gian
        validateSlotTime(serviceSlot);
        
        return serviceSlotRepository.save(serviceSlot);
    }
    
    /**
     * Cập nhật slot
     */
    @Transactional
    public ServiceSlot update(ServiceSlot serviceSlot) {
        log.info("Updating service slot: {}", serviceSlot.getSlotId());
        
        // Validate slot không trùng thời gian (trừ chính nó)
        validateSlotTimeForUpdate(serviceSlot);
        
        return serviceSlotRepository.save(serviceSlot);
    }
    
    /**
     * Tìm slot theo ID
     */
    public ServiceSlot getById(UUID slotId) {
        return serviceSlotRepository.findById(slotId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                        "Service slot not found with ID: " + slotId));
    }
    
    /**
     * Tìm slot theo ID và chi nhánh
     */
    public ServiceSlot getByIdAndBranch(UUID slotId, UUID branchId) {
        return serviceSlotRepository.findBySlotIdAndBranch_BranchId(slotId, branchId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                        "Service slot not found with ID: " + slotId + " in branch: " + branchId));
    }
    
    /**
     * Lấy tất cả slot của chi nhánh
     */
    public List<ServiceSlot> getByBranch(UUID branchId) {
        return serviceSlotRepository.findByBranch_BranchIdOrderBySlotDateAscStartTimeAsc(branchId);
    }
    
    /**
     * Lấy slot theo chi nhánh và ngày
     */
    public List<ServiceSlot> getByBranchAndDate(UUID branchId, LocalDate slotDate) {
        return serviceSlotRepository.findByBranch_BranchIdAndSlotDateOrderByStartTimeAsc(branchId, slotDate);
    }
    
    /**
     * Lấy slot khả dụng theo chi nhánh và ngày
     */
    public List<ServiceSlot> getAvailableSlotsByBranchAndDate(UUID branchId, LocalDate slotDate) {
        return serviceSlotRepository.findByBranch_BranchIdAndSlotDateAndStatusOrderByStartTimeAsc(
                branchId, slotDate, ServiceSlot.SlotStatus.AVAILABLE);
    }
    
    /**
     * Lấy slot theo loại
     */
    public List<ServiceSlot> getByBranchAndDateAndCategory(UUID branchId, LocalDate slotDate, 
                                                          ServiceSlot.SlotCategory category) {
        return serviceSlotRepository.findByBranch_BranchIdAndSlotDateAndSlotCategoryOrderByStartTimeAsc(
                branchId, slotDate, category);
    }
    
    /**
     * Lấy slot khả dụng theo loại
     */
    public List<ServiceSlot> getAvailableSlotsByBranchAndDateAndCategory(UUID branchId, LocalDate slotDate, 
                                                                        ServiceSlot.SlotCategory category) {
        return serviceSlotRepository.findByBranch_BranchIdAndSlotDateAndSlotCategoryAndStatusOrderByStartTimeAsc(
                branchId, slotDate, category, ServiceSlot.SlotStatus.AVAILABLE);
    }
    
    /**
     * Tìm slot trong khoảng thời gian
     */
    public List<ServiceSlot> findAvailableSlotsInTimeRange(UUID branchId, LocalDate slotDate, 
                                                          LocalTime startTime, LocalTime endTime) {
        return serviceSlotRepository.findAvailableSlotsInTimeRange(
                branchId, slotDate, startTime, endTime, ServiceSlot.SlotStatus.AVAILABLE);
    }
    
    /**
     * Lấy slot theo khoảng ngày
     */
    public List<ServiceSlot> getByBranchAndDateRange(UUID branchId, LocalDate startDate, LocalDate endDate) {
        return serviceSlotRepository.findByBranch_BranchIdAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
                branchId, startDate, endDate);
    }
    
    /**
     * Lấy slot VIP khả dụng
     */
    public List<ServiceSlot> getAvailableVipSlots(UUID branchId, LocalDate slotDate) {
        return serviceSlotRepository.findByBranch_BranchIdAndSlotDateAndSlotCategoryAndStatusOrderByPriorityOrderAscStartTimeAsc(
                branchId, slotDate, ServiceSlot.SlotCategory.VIP, ServiceSlot.SlotStatus.AVAILABLE);
    }
    
    /**
     * Lấy slot theo priority order
     */
    public List<ServiceSlot> getByBranchAndDateOrderByPriority(UUID branchId, LocalDate slotDate) {
        return serviceSlotRepository.findByBranch_BranchIdAndSlotDateOrderByPriorityOrderAscStartTimeAsc(branchId, slotDate);
    }
    
    /**
     * Lấy slot trong tương lai
     */
    public List<ServiceSlot> getFutureAvailableSlots(UUID branchId, LocalDate fromDate) {
        return serviceSlotRepository.findFutureAvailableSlots(branchId, fromDate, ServiceSlot.SlotStatus.AVAILABLE);
    }
    
    /**
     * Phân trang slot theo chi nhánh
     */
    public Page<ServiceSlot> getByBranchWithPagination(UUID branchId, Pageable pageable) {
        return serviceSlotRepository.findByBranch_BranchIdOrderBySlotDateDescStartTimeAsc(branchId, pageable);
    }
    
    /**
     * Đếm slot theo chi nhánh và ngày
     */
    public long countByBranchAndDate(UUID branchId, LocalDate slotDate) {
        return serviceSlotRepository.countByBranch_BranchIdAndSlotDate(branchId, slotDate);
    }
    
    /**
     * Đếm slot khả dụng theo chi nhánh và ngày
     */
    public long countAvailableByBranchAndDate(UUID branchId, LocalDate slotDate) {
        return serviceSlotRepository.countByBranch_BranchIdAndSlotDateAndStatus(
                branchId, slotDate, ServiceSlot.SlotStatus.AVAILABLE);
    }
    
    /**
     * Kiểm tra slot có tồn tại không
     */
    public boolean existsByBranchAndDateTime(UUID branchId, LocalDate slotDate, 
                                           LocalTime startTime, LocalTime endTime) {
        return serviceSlotRepository.existsByBranch_BranchIdAndSlotDateAndStartTimeAndEndTime(
                branchId, slotDate, startTime, endTime);
    }
    
    /**
     * Xóa slot (soft delete)
     */
    @Transactional
    public void softDeleteById(UUID slotId) {
        log.info("Soft deleting service slot: {}", slotId);
        ServiceSlot slot = getById(slotId);
        slot.setIsDeleted(true);
        serviceSlotRepository.save(slot);
    }
    
    /**
     * Đóng slot
     */
    @Transactional
    public void closeSlot(UUID slotId, String reason) {
        log.info("Closing service slot: {} with reason: {}", slotId, reason);
        ServiceSlot slot = getById(slotId);
        slot.closeSlot(reason);
        serviceSlotRepository.save(slot);
    }
    
    /**
     * Mở lại slot
     */
    @Transactional
    public void openSlot(UUID slotId) {
        log.info("Opening service slot: {}", slotId);
        ServiceSlot slot = getById(slotId);
        slot.openSlot();
        serviceSlotRepository.save(slot);
    }
    
    /**
     * Validate slot không trùng thời gian khi tạo mới
     */
    private void validateSlotTime(ServiceSlot serviceSlot) {
        List<ServiceSlot> overlappingSlots = serviceSlotRepository.findOverlappingSlots(
                serviceSlot.getBranch().getBranchId(),
                serviceSlot.getSlotDate(),
                serviceSlot.getStartTime(),
                serviceSlot.getEndTime(),
                null // Không loại trừ slot nào khi tạo mới
        );
        
        if (!overlappingSlots.isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Slot time overlaps with existing slots");
        }
    }
    
    /**
     * Validate slot không trùng thời gian khi cập nhật
     */
    private void validateSlotTimeForUpdate(ServiceSlot serviceSlot) {
        List<ServiceSlot> overlappingSlots = serviceSlotRepository.findOverlappingSlots(
                serviceSlot.getBranch().getBranchId(),
                serviceSlot.getSlotDate(),
                serviceSlot.getStartTime(),
                serviceSlot.getEndTime(),
                serviceSlot.getSlotId() // Loại trừ chính slot đang cập nhật
        );
        
        if (!overlappingSlots.isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Slot time overlaps with existing slots");
        }
    }
}
