package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.ServiceBay;
import com.kltn.scsms_api_service.core.repository.ServiceBayRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceBayService {
    
    private final ServiceBayRepository serviceBayRepository;
    
    /**
     * Lấy tất cả service bays
     */
    public List<ServiceBay> findAll() {
        return serviceBayRepository.findAll();
    }
    
    /**
     * Lưu bay mới
     */
    @Transactional
    public ServiceBay save(ServiceBay serviceBay) {
        log.info("Saving service bay: {} for branch: {}", 
                serviceBay.getBayName(), serviceBay.getBranch().getBranchId());
        
        // Validate bay name không trùng trong cùng branch
        validateBayName(serviceBay);
        
        // Validate bay code không trùng
        validateBayCode(serviceBay);
        
        return serviceBayRepository.save(serviceBay);
    }
    
    /**
     * Cập nhật bay
     */
    @Transactional
    public ServiceBay update(ServiceBay serviceBay) {
        log.info("Updating service bay: {}", serviceBay.getBayId());
        
        // Validate bay name không trùng trong cùng branch (trừ chính nó)
        validateBayNameForUpdate(serviceBay);
        
        // Validate bay code không trùng (trừ chính nó)
        validateBayCodeForUpdate(serviceBay);
        
        return serviceBayRepository.save(serviceBay);
    }
    
    /**
     * Tìm bay theo ID
     */
    public ServiceBay getById(UUID bayId) {
        return serviceBayRepository.findById(bayId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                        "Service bay not found with ID: " + bayId));
    }
    
    /**
     * Tìm bay theo ID và chi nhánh
     */
    public ServiceBay getByIdAndBranch(UUID bayId, UUID branchId) {
        return serviceBayRepository.findByBranch_BranchIdOrderByDisplayOrderAscBayNameAsc(branchId)
                .stream()
                .filter(bay -> bay.getBayId().equals(bayId))
                .findFirst()
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                        "Service bay not found with ID: " + bayId + " in branch: " + branchId));
    }
    
    /**
     * Lấy tất cả bay của chi nhánh
     */
    public List<ServiceBay> getByBranch(UUID branchId) {
        return serviceBayRepository.findByBranch_BranchIdOrderByDisplayOrderAscBayNameAsc(branchId);
    }
    
    /**
     * Lấy bay hoạt động của chi nhánh
     */
    public List<ServiceBay> getActiveByBranch(UUID branchId) {
        return serviceBayRepository.findByBranch_BranchIdAndStatusOrderByDisplayOrderAscBayNameAsc(
                branchId, ServiceBay.BayStatus.ACTIVE);
    }
    
    
    /**
     * Tìm bay khả dụng trong khoảng thời gian
     */
    public List<ServiceBay> getAvailableBaysInTimeRange(UUID branchId, LocalDateTime startTime, LocalDateTime endTime) {
        return serviceBayRepository.findAvailableBaysInTimeRange(branchId, startTime, endTime);
    }
    
    
    /**
     * Kiểm tra bay có khả dụng trong khoảng thời gian không
     */
    public boolean isBayAvailableInTimeRange(UUID bayId, LocalDateTime startTime, LocalDateTime endTime) {
        return serviceBayRepository.isBayAvailableInTimeRange(bayId, startTime, endTime);
    }
    
    /**
     * Tìm bay theo tên
     */
    public Optional<ServiceBay> getByBayName(String bayName) {
        return serviceBayRepository.findByBayName(bayName);
    }
    
    /**
     * Tìm bay theo mã bay
     */
    public Optional<ServiceBay> getByBayCode(String bayCode) {
        return serviceBayRepository.findByBayCode(bayCode);
    }
    
    /**
     * Tìm bay theo từ khóa
     */
    public List<ServiceBay> searchByKeyword(String keyword) {
        return serviceBayRepository.searchByKeyword(keyword);
    }
    
    /**
     * Tìm bay theo từ khóa trong chi nhánh
     */
    public List<ServiceBay> searchByKeywordInBranch(UUID branchId, String keyword) {
        return serviceBayRepository.searchByKeywordInBranch(branchId, keyword);
    }
    
    /**
     * Kiểm tra tên bay đã tồn tại trong chi nhánh chưa
     */
    public boolean existsByBranchAndBayName(UUID branchId, String bayName) {
        return serviceBayRepository.existsByBranch_BranchIdAndBayName(branchId, bayName);
    }
    
    /**
     * Kiểm tra tên bay đã tồn tại trong chi nhánh chưa (trừ bay hiện tại)
     */
    public boolean existsByBranchAndBayNameExcluding(UUID branchId, String bayName, UUID bayId) {
        return serviceBayRepository.existsByBranch_BranchIdAndBayNameAndBayIdNot(branchId, bayName, bayId);
    }
    
    /**
     * Kiểm tra mã bay đã tồn tại chưa
     */
    public boolean existsByBayCode(String bayCode) {
        return serviceBayRepository.existsByBayCode(bayCode);
    }
    
    /**
     * Kiểm tra mã bay đã tồn tại chưa (trừ bay hiện tại)
     */
    public boolean existsByBayCodeExcluding(String bayCode, UUID bayId) {
        return serviceBayRepository.existsByBayCodeAndBayIdNot(bayCode, bayId);
    }
    
    /**
     * Đếm số bay theo chi nhánh
     */
    public long countByBranch(UUID branchId) {
        return serviceBayRepository.countByBranch_BranchId(branchId);
    }
    
    /**
     * Đếm số bay theo chi nhánh và trạng thái
     */
    public long countByBranchAndStatus(UUID branchId, ServiceBay.BayStatus status) {
        return serviceBayRepository.countByBranch_BranchIdAndStatus(branchId, status);
    }
    
    
    /**
     * Xóa mềm bay
     */
    @Transactional
    public void softDeleteById(UUID bayId) {
        log.info("Soft deleting service bay: {}", bayId);
        ServiceBay bay = getById(bayId);
        bay.setIsDeleted(true);
        serviceBayRepository.save(bay);
    }
    
    /**
     * Kích hoạt bay
     */
    @Transactional
    public void activateBay(UUID bayId) {
        log.info("Activating service bay: {}", bayId);
        ServiceBay bay = getById(bayId);
        bay.activateBay();
        serviceBayRepository.save(bay);
    }
    
    /**
     * Đặt bay vào trạng thái bảo trì
     */
    @Transactional
    public void setMaintenance(UUID bayId, String reason) {
        log.info("Setting service bay to maintenance: {} with reason: {}", bayId, reason);
        ServiceBay bay = getById(bayId);
        bay.setMaintenance(reason);
        serviceBayRepository.save(bay);
    }
    
    /**
     * Đóng bay
     */
    @Transactional
    public void closeBay(UUID bayId, String reason) {
        log.info("Closing service bay: {} with reason: {}", bayId, reason);
        ServiceBay bay = getById(bayId);
        bay.closeBay(reason);
        serviceBayRepository.save(bay);
    }
    
    /**
     * Validate bay name không trùng trong cùng branch
     */
    private void validateBayName(ServiceBay serviceBay) {
        if (existsByBranchAndBayName(serviceBay.getBranch().getBranchId(), serviceBay.getBayName())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Bay name '" + serviceBay.getBayName() + "' already exists in this branch");
        }
    }
    
    /**
     * Validate bay name không trùng trong cùng branch (trừ bay hiện tại)
     */
    private void validateBayNameForUpdate(ServiceBay serviceBay) {
        if (existsByBranchAndBayNameExcluding(serviceBay.getBranch().getBranchId(), 
                serviceBay.getBayName(), serviceBay.getBayId())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Bay name '" + serviceBay.getBayName() + "' already exists in this branch");
        }
    }
    
    /**
     * Validate bay code không trùng
     */
    private void validateBayCode(ServiceBay serviceBay) {
        if (serviceBay.getBayCode() != null && !serviceBay.getBayCode().trim().isEmpty()) {
            if (existsByBayCode(serviceBay.getBayCode())) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                        "Bay code '" + serviceBay.getBayCode() + "' already exists");
            }
        }
    }
    
    /**
     * Validate bay code không trùng (trừ bay hiện tại)
     */
    private void validateBayCodeForUpdate(ServiceBay serviceBay) {
        if (serviceBay.getBayCode() != null && !serviceBay.getBayCode().trim().isEmpty()) {
            if (existsByBayCodeExcluding(serviceBay.getBayCode(), serviceBay.getBayId())) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                        "Bay code '" + serviceBay.getBayCode() + "' already exists");
            }
        }
    }
}
