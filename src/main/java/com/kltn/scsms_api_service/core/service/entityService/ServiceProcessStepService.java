package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.ServiceProcessStep;
import com.kltn.scsms_api_service.core.repository.ServiceProcessStepRepository;
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
public class ServiceProcessStepService {
    
    private final ServiceProcessStepRepository serviceProcessStepRepository;
    
    /**
     * Tìm service process step theo ID
     */
    public Optional<ServiceProcessStep> findById(UUID id) {
        return serviceProcessStepRepository.findById(id);
    }
    
    /**
     * Tìm service process step theo ID và throw exception nếu không tìm thấy
     */
    public ServiceProcessStep findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PROCESS_STEP_NOT_FOUND));
    }
    
    /**
     * Tìm tất cả bước của một service process
     */
    public List<ServiceProcessStep> findByProcessId(UUID processId) {
        return serviceProcessStepRepository.findByServiceProcessIdAndIsDeletedFalseOrderByStepOrderAsc(processId);
    }
    
    /**
     * Tìm bước theo thứ tự trong một service process
     */
    public Optional<ServiceProcessStep> findByProcessIdAndStepOrder(UUID processId, Integer stepOrder) {
        return serviceProcessStepRepository.findByServiceProcessIdAndStepOrderAndIsDeletedFalse(processId, stepOrder);
    }
    
    /**
     * Tìm bước đầu tiên của một service process
     */
    public Optional<ServiceProcessStep> findFirstStepByProcessId(UUID processId) {
        return serviceProcessStepRepository.findFirstStepByProcessId(processId);
    }
    
    /**
     * Tìm bước cuối cùng của một service process
     */
    public Optional<ServiceProcessStep> findLastStepByProcessId(UUID processId) {
        return serviceProcessStepRepository.findLastStepByProcessId(processId);
    }
    
    /**
     * Tìm bước tiếp theo của một bước
     */
    public Optional<ServiceProcessStep> findNextStep(UUID processId, Integer currentStepOrder) {
        return serviceProcessStepRepository.findNextStep(processId, currentStepOrder + 1);
    }
    
    /**
     * Tìm bước trước đó của một bước
     */
    public Optional<ServiceProcessStep> findPreviousStep(UUID processId, Integer currentStepOrder) {
        return serviceProcessStepRepository.findPreviousStep(processId, currentStepOrder - 1);
    }
    
    /**
     * Tìm bước theo tên trong một service process
     */
    public Page<ServiceProcessStep> findByNameContainingAndProcessId(UUID processId, String name, Pageable pageable) {
        return serviceProcessStepRepository.findByNameContainingIgnoreCaseAndProcessId(processId, name, pageable);
    }
    
    /**
     * Tìm bước bắt buộc của một service process
     */
    public List<ServiceProcessStep> findRequiredStepsByProcessId(UUID processId) {
        return serviceProcessStepRepository.findRequiredStepsByProcessId(processId);
    }
    
    /**
     * Tìm bước không bắt buộc của một service process
     */
    public List<ServiceProcessStep> findOptionalStepsByProcessId(UUID processId) {
        return serviceProcessStepRepository.findOptionalStepsByProcessId(processId);
    }
    
    /**
     * Tìm bước có sản phẩm
     */
    public List<ServiceProcessStep> findStepsWithProductsByProcessId(UUID processId) {
        return serviceProcessStepRepository.findStepsWithProductsByProcessId(processId);
    }
    
    /**
     * Tìm bước không có sản phẩm
     */
    public List<ServiceProcessStep> findStepsWithoutProductsByProcessId(UUID processId) {
        return serviceProcessStepRepository.findStepsWithoutProductsByProcessId(processId);
    }
    
    /**
     * Tìm bước theo thời gian dự kiến
     */
    public List<ServiceProcessStep> findByEstimatedTimeBetweenAndProcessId(UUID processId, Integer minTime, Integer maxTime) {
        return serviceProcessStepRepository.findByEstimatedTimeBetweenAndProcessId(processId, minTime, maxTime);
    }
    
    /**
     * Đếm số bước của một service process
     */
    public long countStepsByProcessId(UUID processId) {
        return serviceProcessStepRepository.countStepsByProcessId(processId);
    }
    
    /**
     * Lưu service process step
     */
    @Transactional
    public ServiceProcessStep save(ServiceProcessStep serviceProcessStep) {
        log.info("Saving service process step: {}", serviceProcessStep.getName());
        return serviceProcessStepRepository.save(serviceProcessStep);
    }
    
    /**
     * Cập nhật service process step
     */
    @Transactional
    public ServiceProcessStep update(ServiceProcessStep serviceProcessStep) {
        log.info("Updating service process step: {}", serviceProcessStep.getName());
        return serviceProcessStepRepository.save(serviceProcessStep);
    }
    
    /**
     * Xóa mềm service process step
     */
    @Transactional
    public void delete(ServiceProcessStep serviceProcessStep) {
        log.info("Deleting service process step: {}", serviceProcessStep.getName());
        serviceProcessStep.setIsDeleted(true);
        serviceProcessStepRepository.save(serviceProcessStep);
    }
    
    /**
     * Xóa mềm service process step theo ID
     */
    @Transactional
    public void deleteById(UUID id) {
        ServiceProcessStep serviceProcessStep = findByIdOrThrow(id);
        delete(serviceProcessStep);
    }
    
    /**
     * Kiểm tra step order đã tồn tại trong process chưa (trừ id hiện tại)
     */
    public boolean existsByProcessIdAndStepOrderAndIdNot(UUID processId, Integer stepOrder, UUID id) {
        return serviceProcessStepRepository.existsByProcessIdAndStepOrderAndIdNot(processId, stepOrder, id);
    }
    
    /**
     * Sắp xếp lại thứ tự các bước
     */
    @Transactional
    public void reorderSteps(UUID processId) {
        List<ServiceProcessStep> steps = findByProcessId(processId);
        for (int i = 0; i < steps.size(); i++) {
            ServiceProcessStep step = steps.get(i);
            step.setStepOrder(i + 1);
            serviceProcessStepRepository.save(step);
        }
    }
    
}
