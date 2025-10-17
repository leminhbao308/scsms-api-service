package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.abstracts.BaseResponseData;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.ServiceProcessInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.ServiceProcessStepInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.param.ServiceProcessFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.*;
import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.mapper.ServiceProcessMapper;
import com.kltn.scsms_api_service.mapper.ServiceProcessStepMapper;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServiceProcessManagementService {
    
    private final ServiceProcessService serviceProcessService;
    private final ServiceProcessStepService serviceProcessStepService;
    private final ServiceProcessMapper serviceProcessMapper;
    private final ServiceProcessStepMapper serviceProcessStepMapper;
    
    /**
     * Khởi tạo service process mặc định nếu chưa có
     */
    @PostConstruct
    @Transactional
    public void initializeDefaultServiceProcess() {
        try {
            Optional<ServiceProcess> defaultProcess = serviceProcessService.findDefaultProcess();
            if (defaultProcess.isEmpty()) {
                log.info("No default service process found, creating one...");
                
                ServiceProcess defaultServiceProcess = ServiceProcess.builder()
                        .code("DEFAULT_PROCESS")
                        .name("Quy trình chăm sóc xe mặc định")
                        .description("Quy trình chăm sóc xe mặc định được tạo tự động")
                        .isDefault(true)
                        .isActive(true)
                        .build();
                
                serviceProcessService.save(defaultServiceProcess);
                log.info("Default service process created successfully: {}", defaultServiceProcess.getCode());
            } else {
                log.info("Default service process already exists: {}", defaultProcess.get().getCode());
            }
        } catch (Exception e) {
            log.error("Error initializing default service process: {}", e.getMessage());
        }
    }
    
    // ========== SERVICE PROCESS MANAGEMENT ==========
    
    /**
     * Lấy tất cả service process với phân trang và lọc
     */
    public Page<ServiceProcessInfoDto> getAllServiceProcesses(ServiceProcessFilterParam filterParam, Pageable pageable) {
        log.info("Getting all service processes with filter: {}", filterParam);
        
        // Áp dụng các bộ lọc
        Page<ServiceProcess> serviceProcesses;
        
        if (filterParam.getCode() != null && !filterParam.getCode().trim().isEmpty()) {
            serviceProcesses = serviceProcessService.findByCodeContaining(filterParam.getCode(), pageable);
        } else if (filterParam.getName() != null && !filterParam.getName().trim().isEmpty()) {
            serviceProcesses = serviceProcessService.findByNameContaining(filterParam.getName(), pageable);
        } else if (filterParam.getHasSteps() != null) {
            if (filterParam.getHasSteps()) {
                serviceProcesses = serviceProcessService.findProcessesWithSteps(pageable);
            } else {
                serviceProcesses = serviceProcessService.findProcessesWithoutSteps(pageable);
            }
        } else {
            // Lấy tất cả với phân trang và processSteps
            serviceProcesses = serviceProcessService.findAllWithProcessSteps(pageable);
        }
        
        // Map to DTO
        Page<ServiceProcessInfoDto> result = serviceProcesses.map(serviceProcess -> {
            ServiceProcessInfoDto dto = serviceProcessMapper.toServiceProcessInfoDto(serviceProcess);
            
            // Set processId và processName cho từng step
            if (dto.getProcessSteps() != null) {
                dto.getProcessSteps().forEach(step -> {
                    // Set processId và processName từ serviceProcess
                    step.setProcessId(serviceProcess.getId());
                    step.setProcessName(serviceProcess.getName());
                    
                    // Set audit fields từ ServiceProcessStep entity
                    ServiceProcessStep stepEntity = serviceProcess.getProcessSteps().stream()
                            .filter(sps -> sps.getId().equals(step.getId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (stepEntity != null && step.getAudit() != null) {
                        step.getAudit().setCreatedDate(stepEntity.getCreatedDate());
                        step.getAudit().setModifiedDate(stepEntity.getModifiedDate());
                        step.getAudit().setCreatedBy(stepEntity.getCreatedBy());
                        step.getAudit().setModifiedBy(stepEntity.getModifiedBy());
                        step.getAudit().setIsActive(stepEntity.getIsActive());
                        step.getAudit().setIsDeleted(stepEntity.getIsDeleted());
                    }
                });
            }
            
            return dto;
        });
        
        return result;
    }
    
    /**
     * Lấy service process theo ID
     */
    public ServiceProcessInfoDto getServiceProcessById(UUID processId) {
        log.info("Getting service process by ID: {}", processId);
        
        ServiceProcess serviceProcess = serviceProcessService.findByIdOrThrow(processId);
        ServiceProcessInfoDto dto = serviceProcessMapper.toServiceProcessInfoDto(serviceProcess);
                    
                    // Set processId và processName cho từng step
                    if (dto.getProcessSteps() != null) {
                        dto.getProcessSteps().forEach(step -> {
                            // Set processId và processName từ serviceProcess
                            step.setProcessId(serviceProcess.getId());
                            step.setProcessName(serviceProcess.getName());
                            
                            // Set audit fields từ ServiceProcessStep entity
                            ServiceProcessStep stepEntity = serviceProcess.getProcessSteps().stream()
                                    .filter(sps -> sps.getId().equals(step.getId()))
                                    .findFirst()
                                    .orElse(null);
                            
                            if (stepEntity != null && step.getAudit() != null) {
                                step.getAudit().setCreatedDate(stepEntity.getCreatedDate());
                                step.getAudit().setModifiedDate(stepEntity.getModifiedDate());
                                step.getAudit().setCreatedBy(stepEntity.getCreatedBy());
                                step.getAudit().setModifiedBy(stepEntity.getModifiedBy());
                                step.getAudit().setIsActive(stepEntity.getIsActive());
                                step.getAudit().setIsDeleted(stepEntity.getIsDeleted());
                            }
                        });
                    }
                    
                    return dto;
    }
    
    /**
     * Tạo service process mới
     */
    @Transactional
    public ServiceProcessInfoDto createServiceProcess(CreateServiceProcessRequest request) {
        log.info("Creating new service process: {}", request.getName());
        
        // Kiểm tra code đã tồn tại chưa
        if (serviceProcessService.findByCode(request.getCode()).isPresent()) {
            throw new ClientSideException(ErrorCode.SERVICE_PROCESS_CODE_ALREADY_EXISTS);
        }
        
        // Tạo service process
        ServiceProcess serviceProcess = serviceProcessMapper.toEntity(request);
        serviceProcess = serviceProcessService.save(serviceProcess);
        
        // Tạo các steps nếu có
        if (request.getProcessSteps() != null && !request.getProcessSteps().isEmpty()) {
            for (CreateServiceProcessStepRequest stepRequest : request.getProcessSteps()) {
                ServiceProcessStep step = serviceProcessStepMapper.toEntity(stepRequest);
                step.setServiceProcess(serviceProcess);
                serviceProcessStepService.save(step);
            }
        }
        
        return getServiceProcessById(serviceProcess.getId());
    }
    
    /**
     * Cập nhật service process
     */
    @Transactional
    public ServiceProcessInfoDto updateServiceProcess(UUID processId, UpdateServiceProcessRequest request) {
        log.info("Updating service process: {}", processId);
        
        ServiceProcess existingProcess = serviceProcessService.findByIdOrThrow(processId);
        
        // Kiểm tra code đã tồn tại chưa (trừ process hiện tại)
        if (request.getCode() != null && !request.getCode().equals(existingProcess.getCode())) {
            Optional<ServiceProcess> existingByCode = serviceProcessService.findByCode(request.getCode());
            if (existingByCode.isPresent() && !existingByCode.get().getId().equals(processId)) {
                throw new ClientSideException(ErrorCode.SERVICE_PROCESS_CODE_ALREADY_EXISTS);
            }
        }
        
        // Cập nhật thông tin process
        serviceProcessMapper.updateEntity(existingProcess, request);
        existingProcess = serviceProcessService.update(existingProcess);
        
        // Cập nhật steps nếu có
        if (request.getProcessSteps() != null) {
            // Xóa tất cả steps cũ
            serviceProcessStepService.deleteByProcessId(processId);
            
            // Tạo lại các steps mới
            for (UpdateServiceProcessStepRequest stepRequest : request.getProcessSteps()) {
                ServiceProcessStep step = new ServiceProcessStep();
                serviceProcessStepMapper.updateEntity(step, stepRequest);
                step.setServiceProcess(existingProcess);
                serviceProcessStepService.save(step);
            }
        }
        
        return getServiceProcessById(processId);
    }
    
    /**
     * Xóa service process
     */
    @Transactional
    public BaseResponseData<?> deleteServiceProcess(UUID processId) {
        log.info("Deleting service process: {}", processId);
        
        serviceProcessService.findByIdOrThrow(processId);
        
        // Xóa tất cả steps trước
        serviceProcessStepService.deleteByProcessId(processId);
        
        // Xóa process
        serviceProcessService.deleteById(processId);
        
        return BaseResponseData.builder()
                .success(true)
                .message("Service process deleted successfully")
                .build();
    }
    
    // ========== SERVICE PROCESS STEP MANAGEMENT ==========
    
    /**
     * Lấy tất cả steps của một process
     */
    public List<ServiceProcessStepInfoDto> getServiceProcessSteps(UUID processId) {
        log.info("Getting service process steps for process: {}", processId);
        
        List<ServiceProcessStep> steps = serviceProcessStepService.findByProcessId(processId);
        return steps.stream()
                .map(step -> {
                    ServiceProcessStepInfoDto dto = serviceProcessStepMapper.toServiceProcessStepInfoDto(step);
                    dto.setProcessId(processId);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy step theo ID
     */
    public ServiceProcessStepInfoDto getServiceProcessStepById(UUID stepId) {
        log.info("Getting service process step by ID: {}", stepId);
        
        ServiceProcessStep step = serviceProcessStepService.findByIdOrThrow(stepId);
        ServiceProcessStepInfoDto dto = serviceProcessStepMapper.toServiceProcessStepInfoDto(step);
        dto.setProcessId(step.getServiceProcess().getId());
        dto.setProcessName(step.getServiceProcess().getName());
        
        return dto;
    }
    
    /**
     * Tạo step mới cho process
     */
    @Transactional
    public ServiceProcessStepInfoDto createServiceProcessStep(UUID processId, CreateServiceProcessStepRequest request) {
        log.info("Creating new service process step for process: {}", processId);
        
        ServiceProcess serviceProcess = serviceProcessService.findByIdOrThrow(processId);
        
        // Kiểm tra step order đã tồn tại chưa
        if (serviceProcessStepService.existsByProcessIdAndStepOrder(processId, request.getStepOrder())) {
            throw new ClientSideException(ErrorCode.SERVICE_PROCESS_STEP_ORDER_ALREADY_EXISTS);
        }
        
        ServiceProcessStep step = serviceProcessStepMapper.toEntity(request);
        step.setServiceProcess(serviceProcess);
        step = serviceProcessStepService.save(step);
        
        return getServiceProcessStepById(step.getId());
    }
    
    /**
     * Cập nhật step
     */
    @Transactional
    public ServiceProcessStepInfoDto updateServiceProcessStep(UUID stepId, UpdateServiceProcessStepRequest request) {
        log.info("Updating service process step: {}", stepId);
        
        ServiceProcessStep existingStep = serviceProcessStepService.findByIdOrThrow(stepId);
        
        // Kiểm tra step order đã tồn tại chưa (trừ step hiện tại)
        if (request.getStepOrder() != null && !request.getStepOrder().equals(existingStep.getStepOrder())) {
            if (serviceProcessStepService.existsByProcessIdAndStepOrderExcludingId(
                    existingStep.getServiceProcess().getId(), request.getStepOrder(), stepId)) {
                throw new ClientSideException(ErrorCode.SERVICE_PROCESS_STEP_ORDER_ALREADY_EXISTS);
            }
        }
        
        serviceProcessStepMapper.updateEntity(existingStep, request);
        existingStep = serviceProcessStepService.update(existingStep);
        
        return getServiceProcessStepById(stepId);
    }
    
    /**
     * Xóa step vĩnh viễn (hard delete)
     */
    @Transactional
    public BaseResponseData<?> deleteServiceProcessStep(UUID stepId) {
        log.info("Hard deleting service process step: {}", stepId);
        
        // Kiểm tra step có tồn tại không
        ServiceProcessStep step = serviceProcessStepService.findByIdOrThrow(stepId);
        log.info("Found step to delete: {} (ID: {})", step.getName(), stepId);
        
        // Thực hiện hard delete
        serviceProcessStepService.deleteById(stepId);
        log.info("Successfully hard deleted step: {} (ID: {})", step.getName(), stepId);
        
        return BaseResponseData.builder()
                .success(true)
                .message("Service process step permanently deleted")
                .build();
    }
    
    /**
     * Sắp xếp lại thứ tự các steps trong process
     */
    @Transactional
    public BaseResponseData<?> reorderServiceProcessSteps(UUID processId) {
        log.info("Reordering service process steps for process: {}", processId);
        
        serviceProcessStepService.reorderSteps(processId);
        
        return BaseResponseData.builder()
                .success(true)
                .message("Service process steps reordered successfully")
                .build();
    }
    
    // ========== ADDITIONAL METHODS FOR CONTROLLER ==========
    
    /**
     * Lấy service process theo code
     */
    public ServiceProcessInfoDto getServiceProcessByCode(String code) {
        log.info("Getting service process by code: {}", code);
        
        ServiceProcess serviceProcess = serviceProcessService.findByCode(code)
                .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PROCESS_NOT_FOUND));
        
        return getServiceProcessById(serviceProcess.getId());
    }
    
    /**
     * Lấy service process theo service ID
     */
    public ServiceProcessInfoDto getServiceProcessByService(UUID serviceId) {
        log.info("Getting service process by service ID: {}", serviceId);
        
        ServiceProcess serviceProcess = serviceProcessService.findByServiceId(serviceId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PROCESS_NOT_FOUND));
        
        return getServiceProcessById(serviceProcess.getId());
    }
    
    /**
     * Lấy service process mặc định
     */
    public ServiceProcessInfoDto getDefaultServiceProcess() {
        log.info("Getting default service process");
        
        ServiceProcess defaultProcess = serviceProcessService.findDefaultProcess()
                .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PROCESS_NOT_FOUND));
        
        return getServiceProcessById(defaultProcess.getId());
    }
    
    /**
     * Lấy tất cả service process đang hoạt động
     */
    public List<ServiceProcessInfoDto> getAllActiveServiceProcesses() {
        log.info("Getting all active service processes");
        
        List<ServiceProcess> activeProcesses = serviceProcessService.findAll(Pageable.unpaged()).getContent().stream()
                .filter(ServiceProcess::getIsActive)
                .collect(Collectors.toList());
        return activeProcesses.stream()
                .map(process -> getServiceProcessById(process.getId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Đặt service process làm mặc định
     */
    @Transactional
    public BaseResponseData<?> setDefaultServiceProcess(UUID processId) {
        log.info("Setting default service process: {}", processId);
        
        ServiceProcess process = serviceProcessService.findByIdOrThrow(processId);
        
        // Đặt process này làm default
        process.setIsDefault(true);
        serviceProcessService.update(process);
        
        return BaseResponseData.builder()
                .success(true)
                .message("Default service process set successfully")
                .build();
    }
    
    /**
     * Thêm step vào service process
     */
    @Transactional
    public ServiceProcessStepInfoDto addStepToServiceProcess(UUID processId, CreateServiceProcessStepRequest request) {
        log.info("Adding step to service process: {}", processId);
        
        return createServiceProcessStep(processId, request);
    }
}