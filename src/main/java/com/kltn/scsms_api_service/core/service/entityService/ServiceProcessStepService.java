package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.ServiceProcessStep;
import com.kltn.scsms_api_service.core.repository.ServiceProcessStepRepository;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.exception.ServerSideException;
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
public class ServiceProcessStepService {
    
    private final ServiceProcessStepRepository serviceProcessStepRepository;
    
    /**
     * Lưu service process step
     */
    @Transactional
    public ServiceProcessStep save(ServiceProcessStep serviceProcessStep) {
        log.debug("Saving service process step: {}", serviceProcessStep.getName());
        return serviceProcessStepRepository.save(serviceProcessStep);
    }
    
    /**
     * Tìm service process step theo ID
     */
    public Optional<ServiceProcessStep> findById(UUID id) {
        log.debug("Finding service process step by ID: {}", id);
        return serviceProcessStepRepository.findById(id);
    }
    
    /**
     * Lấy service process step theo ID (throw exception nếu không tìm thấy)
     */
    public ServiceProcessStep getById(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "ServiceProcessStep not found with ID: " + id));
    }
    
    /**
     * Lấy service process step theo ID (throw exception nếu không tìm thấy) - alias
     */
    public ServiceProcessStep findByIdOrThrow(UUID id) {
        return getById(id);
    }
    
    /**
     * Tìm tất cả steps theo process ID
     */
    public List<ServiceProcessStep> findByProcessId(UUID processId) {
        log.debug("Finding service process steps by process ID: {}", processId);
        return serviceProcessStepRepository.findByServiceProcess_IdOrderByStepOrderAsc(processId);
    }
    
    /**
     * Tìm step theo process ID và step order
     */
    public Optional<ServiceProcessStep> findByProcessIdAndStepOrder(UUID processId, Integer stepOrder) {
        log.debug("Finding service process step by process ID: {} and step order: {}", processId, stepOrder);
        return serviceProcessStepRepository.findByServiceProcess_IdAndStepOrder(processId, stepOrder);
    }
    
    /**
     * Tìm step đầu tiên của process
     */
    public Optional<ServiceProcessStep> findFirstStepByProcessId(UUID processId) {
        log.debug("Finding first step by process ID: {}", processId);
        return serviceProcessStepRepository.findFirstStepByProcessId(processId);
    }
    
    /**
     * Tìm step cuối cùng của process
     */
    public Optional<ServiceProcessStep> findLastStepByProcessId(UUID processId) {
        log.debug("Finding last step by process ID: {}", processId);
        return serviceProcessStepRepository.findLastStepByProcessId(processId);
    }
    
    /**
     * Tìm step tiếp theo trong process
     */
    public Optional<ServiceProcessStep> findNextStepByProcessIdAndOrder(UUID processId, Integer currentOrder) {
        log.debug("Finding next step by process ID: {} and current order: {}", processId, currentOrder);
        return serviceProcessStepRepository.findNextStepByProcessIdAndOrder(processId, currentOrder);
    }
    
    /**
     * Tìm step trước đó trong process
     */
    public Optional<ServiceProcessStep> findPreviousStepByProcessIdAndOrder(UUID processId, Integer currentOrder) {
        log.debug("Finding previous step by process ID: {} and current order: {}", processId, currentOrder);
        return serviceProcessStepRepository.findPreviousStepByProcessIdAndOrder(processId, currentOrder);
    }
    
    /**
     * Tìm steps theo tên
     */
    public Page<ServiceProcessStep> findByNameContaining(String name, Pageable pageable) {
        log.debug("Finding service process steps by name containing: {}", name);
        return serviceProcessStepRepository.findByNameContainingIgnoreCaseOrderByStepOrderAsc(name, pageable);
    }
    
    /**
     * Tìm steps theo estimated time range
     */
    public List<ServiceProcessStep> findByEstimatedTimeBetween(Integer minTime, Integer maxTime) {
        log.debug("Finding service process steps by estimated time between: {} and {}", minTime, maxTime);
        return serviceProcessStepRepository.findByEstimatedTimeBetweenOrderByStepOrderAsc(minTime, maxTime);
    }
    
    
    /**
     * Tìm steps theo process với pagination
     */
    public Page<ServiceProcessStep> findByProcessId(UUID processId, Pageable pageable) {
        log.debug("Finding service process steps by process ID: {} with pagination", processId);
        return serviceProcessStepRepository.findByServiceProcess_IdOrderByStepOrderAsc(processId, pageable);
    }
    
    /**
     * Đếm số steps trong process
     */
    public long countByProcessId(UUID processId) {
        log.debug("Counting service process steps by process ID: {}", processId);
        return serviceProcessStepRepository.countByServiceProcess_Id(processId);
    }
    
    /**
     * Kiểm tra xem step order có tồn tại trong process không
     */
    public boolean existsByProcessIdAndStepOrder(UUID processId, Integer stepOrder) {
        log.debug("Checking if step order {} exists in process ID: {}", stepOrder, processId);
        return serviceProcessStepRepository.existsByServiceProcess_IdAndStepOrder(processId, stepOrder);
    }
    
    /**
     * Kiểm tra xem step order có tồn tại trong process không (không bao gồm step hiện tại)
     */
    public boolean existsByProcessIdAndStepOrderExcludingId(UUID processId, Integer stepOrder, UUID excludeId) {
        log.debug("Checking if step order {} exists in process ID: {} excluding step ID: {}", stepOrder, processId, excludeId);
        return serviceProcessStepRepository.findByServiceProcess_IdAndStepOrderExcludingId(processId, stepOrder, excludeId).isPresent();
    }
    
    /**
     * Kiểm tra xem step order có tồn tại trong process không (không bao gồm step hiện tại) - alias
     */
    public boolean existsByProcessIdAndStepOrderAndIdNot(UUID processId, Integer stepOrder, UUID excludeId) {
        return existsByProcessIdAndStepOrderExcludingId(processId, stepOrder, excludeId);
    }
    
    /**
     * Xóa service process step
     */
    @Transactional
    public void deleteById(UUID id) {
        log.debug("Deleting service process step by ID: {}", id);
        if (!serviceProcessStepRepository.existsById(id)) {
            throw new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "ServiceProcessStep not found with ID: " + id);
        }
        serviceProcessStepRepository.deleteById(id);
    }
    
    /**
     * Xóa tất cả steps theo process ID
     */
    @Transactional
    public void deleteByProcessId(UUID processId) {
        log.debug("Deleting all service process steps by process ID: {}", processId);
        List<ServiceProcessStep> steps = findByProcessId(processId);
        serviceProcessStepRepository.deleteAll(steps);
    }
    
    /**
     * Kiểm tra xem step có tồn tại không
     */
    public boolean existsById(UUID id) {
        return serviceProcessStepRepository.existsById(id);
    }
    
    /**
     * Lấy tất cả service process steps
     */
    public List<ServiceProcessStep> findAll() {
        log.debug("Finding all service process steps");
        return serviceProcessStepRepository.findAll();
    }
    
    /**
     * Lấy tất cả service process steps với pagination
     */
    public Page<ServiceProcessStep> findAll(Pageable pageable) {
        log.debug("Finding all service process steps with pagination");
        return serviceProcessStepRepository.findAll(pageable);
    }
    
    /**
     * Cập nhật service process step
     */
    @Transactional
    public ServiceProcessStep update(ServiceProcessStep serviceProcessStep) {
        log.debug("Updating service process step: {}", serviceProcessStep.getName());
        return serviceProcessStepRepository.save(serviceProcessStep);
    }
    
    /**
     * Xóa service process step
     */
    @Transactional
    public void delete(ServiceProcessStep serviceProcessStep) {
        log.debug("Deleting service process step: {}", serviceProcessStep.getName());
        serviceProcessStepRepository.delete(serviceProcessStep);
    }
    
    /**
     * Sắp xếp lại thứ tự các bước trong process
     */
    @Transactional
    public void reorderSteps(UUID processId) {
        log.debug("Reordering steps for process: {}", processId);
        List<ServiceProcessStep> steps = findByProcessId(processId);
        
        // Sắp xếp theo thứ tự hiện tại
        steps.sort((s1, s2) -> {
            if (s1.getStepOrder() == null && s2.getStepOrder() == null) return 0;
            if (s1.getStepOrder() == null) return 1;
            if (s2.getStepOrder() == null) return -1;
            return s1.getStepOrder().compareTo(s2.getStepOrder());
        });
        
        // Cập nhật lại thứ tự
        for (int i = 0; i < steps.size(); i++) {
            ServiceProcessStep step = steps.get(i);
            step.setStepOrder(i + 1);
            serviceProcessStepRepository.save(step);
        }
    }
    
    /**
     * Flush pending changes to database
     */
    @Transactional
    public void flush() {
        serviceProcessStepRepository.flush();
    }
}