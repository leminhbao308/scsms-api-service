package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.ServiceProcess;
import com.kltn.scsms_api_service.core.repository.ServiceProcessRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServiceProcessService {
    
    private final ServiceProcessRepository serviceProcessRepository;
    
    /**
     * Tìm service process theo ID
     */
    public Optional<ServiceProcess> findById(UUID id) {
        return serviceProcessRepository.findById(id);
    }
    
    /**
     * Tìm service process theo ID và throw exception nếu không tìm thấy
     */
    public ServiceProcess findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PROCESS_NOT_FOUND));
    }
    
    /**
     * Tìm service process theo code
     */
    public Optional<ServiceProcess> findByCode(String code) {
        return serviceProcessRepository.findByCodeAndIsDeletedFalse(code);
    }
    
    /**
     * Tìm service process theo code và throw exception nếu không tìm thấy
     */
    public ServiceProcess findByCodeOrThrow(String code) {
        return findByCode(code)
                .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PROCESS_NOT_FOUND));
    }
    
    /**
     * Tìm service process theo service ID
     */
    public Optional<ServiceProcess> findByServiceId(UUID serviceId) {
        return serviceProcessRepository.findByServiceIdAndIsDeletedFalse(serviceId);
    }
    
    /**
     * Tìm service process theo service ID và throw exception nếu không tìm thấy
     */
    public ServiceProcess findByServiceIdOrThrow(UUID serviceId) {
        return findByServiceId(serviceId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PROCESS_NOT_FOUND));
    }
    
    /**
     * Tìm service process mặc định
     */
    public Optional<ServiceProcess> findDefaultProcess() {
        return serviceProcessRepository.findByIsDefaultTrueAndIsActiveTrueAndIsDeletedFalse();
    }
    
    /**
     * Tìm tất cả service process đang hoạt động
     */
    public List<ServiceProcess> findAllActive() {
        return serviceProcessRepository.findByIsActiveTrueAndIsDeletedFalseOrderByNameAsc();
    }
    
    /**
     * Tìm tất cả service process với phân trang
     */
    public Page<ServiceProcess> findAll(Pageable pageable) {
        return serviceProcessRepository.findAll(pageable);
    }
    
    /**
     * Tìm tất cả service process với processSteps được load
     */
    public List<ServiceProcess> findAllWithProcessSteps() {
        return serviceProcessRepository.findAllWithProcessSteps();
    }
    
    /**
     * Tìm tất cả service process với processSteps được load (có phân trang)
     */
    public Page<ServiceProcess> findAllWithProcessSteps(Pageable pageable) {
        return serviceProcessRepository.findAllWithProcessSteps(pageable);
    }
    
    /**
     * Tìm service process theo tên (tìm kiếm gần đúng)
     */
    public Page<ServiceProcess> findByNameContaining(String name, Pageable pageable) {
        return serviceProcessRepository.findByNameContainingIgnoreCase(name, pageable);
    }
    
    /**
     * Tìm service process theo code (tìm kiếm gần đúng)
     */
    public Page<ServiceProcess> findByCodeContaining(String code, Pageable pageable) {
        return serviceProcessRepository.findByCodeContainingIgnoreCase(code, pageable);
    }
    
    /**
     * Tìm service process có bước
     */
    public Page<ServiceProcess> findProcessesWithSteps(Pageable pageable) {
        return serviceProcessRepository.findProcessesWithSteps(pageable);
    }
    
    /**
     * Tìm service process không có bước
     */
    public Page<ServiceProcess> findProcessesWithoutSteps(Pageable pageable) {
        return serviceProcessRepository.findProcessesWithoutSteps(pageable);
    }
    
    /**
     * Tìm service process theo thời gian dự kiến
     */
    public Page<ServiceProcess> findByEstimatedDurationBetween(Integer minDuration, Integer maxDuration, Pageable pageable) {
        return serviceProcessRepository.findByEstimatedDurationBetween(minDuration, maxDuration, pageable);
    }
    
    /**
     * Tìm service process được sử dụng bởi service
     */
    public List<ServiceProcess> findProcessesUsedByServices() {
        return serviceProcessRepository.findProcessesUsedByServices();
    }
    
    /**
     * Tìm service process được sử dụng bởi service package
     */
    public List<ServiceProcess> findProcessesUsedByServicePackages() {
        return serviceProcessRepository.findProcessesUsedByServicePackages();
    }
    
    /**
     * Lưu service process
     */
    @Transactional
    public ServiceProcess save(ServiceProcess serviceProcess) {
        log.info("Saving service process: {}", serviceProcess.getName());
        return serviceProcessRepository.save(serviceProcess);
    }
    
    /**
     * Cập nhật service process
     */
    @Transactional
    public ServiceProcess update(ServiceProcess serviceProcess) {
        log.info("Updating service process: {}", serviceProcess.getName());
        return serviceProcessRepository.save(serviceProcess);
    }
    
    /**
     * Xóa mềm service process
     */
    @Transactional
    public void delete(ServiceProcess serviceProcess) {
        log.info("Deleting service process: {}", serviceProcess.getName());
        serviceProcess.setIsDeleted(true);
        serviceProcessRepository.save(serviceProcess);
    }
    
    /**
     * Xóa mềm service process theo ID
     */
    @Transactional
    public void deleteById(UUID id) {
        ServiceProcess serviceProcess = findByIdOrThrow(id);
        delete(serviceProcess);
    }
    
    /**
     * Kiểm tra code đã tồn tại chưa (trừ id hiện tại)
     */
    public boolean existsByCodeAndIdNot(String code, UUID id) {
        return serviceProcessRepository.existsByCodeAndIdNot(code, id);
    }
    
    /**
     * Đếm số service process đang hoạt động
     */
    public long countActiveProcesses() {
        return serviceProcessRepository.countActiveProcesses();
    }
    
    /**
     * Kiểm tra service process có thể xóa không (không được sử dụng bởi service hoặc service package)
     */
    public boolean canDelete(ServiceProcess serviceProcess) {
        List<ServiceProcess> usedByServices = findProcessesUsedByServices();
        List<ServiceProcess> usedByServicePackages = findProcessesUsedByServicePackages();
        
        return !usedByServices.contains(serviceProcess) && !usedByServicePackages.contains(serviceProcess);
    }
    
    /**
     * Đặt service process làm mặc định (hủy mặc định của các process khác)
     */
    @Transactional
    public ServiceProcess setAsDefault(ServiceProcess serviceProcess) {
        // Hủy mặc định của tất cả process khác
        List<ServiceProcess> allProcesses = serviceProcessRepository.findAll();
        for (ServiceProcess process : allProcesses) {
            if (!process.getId().equals(serviceProcess.getId()) && process.getIsDefault()) {
                process.setIsDefault(false);
                serviceProcessRepository.save(process);
            }
        }
        
        // Đặt process hiện tại làm mặc định
        serviceProcess.setIsDefault(true);
        return serviceProcessRepository.save(serviceProcess);
    }
}
