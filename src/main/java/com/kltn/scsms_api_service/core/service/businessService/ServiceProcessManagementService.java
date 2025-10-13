package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.abstracts.BaseResponse;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.ServiceProcessInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.ServiceProcessStepInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.ServiceProcessStepProductInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.param.ServiceProcessFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.*;
import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.mapper.ServiceProcessMapper;
import com.kltn.scsms_api_service.mapper.ServiceProcessStepMapper;
import com.kltn.scsms_api_service.mapper.ServiceProcessStepProductMapper;
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
    private final ServiceProcessStepProductService serviceProcessStepProductService;
    private final ProductService productService;
    private final ServiceProcessMapper serviceProcessMapper;
    private final ServiceProcessStepMapper serviceProcessStepMapper;
    private final ServiceProcessStepProductMapper serviceProcessStepProductMapper;
    
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
                        .estimatedDuration(60) // 60 phút
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
        } else if (filterParam.getMinEstimatedDuration() != null && filterParam.getMaxEstimatedDuration() != null) {
            serviceProcesses = serviceProcessService.findByEstimatedDurationBetween(
                    filterParam.getMinEstimatedDuration(), 
                    filterParam.getMaxEstimatedDuration(), 
                    pageable);
        } else {
            // Lấy tất cả với phân trang và processSteps
            serviceProcesses = serviceProcessService.findAllWithProcessSteps(pageable);
        }
        
        // Load stepProducts riêng biệt để tránh MultipleBagFetchException
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
                    
                    // Load stepProducts cho từng step
                    List<ServiceProcessStepProduct> stepProducts = serviceProcessStepProductService.findByProcessIdWithProduct(serviceProcess.getId())
                            .stream()
                            .filter(spp -> spp.getServiceProcessStep().getId().equals(step.getId()))
                            .collect(Collectors.toList());
                    
                    List<ServiceProcessStepProductInfoDto> stepProductDtos = stepProducts.stream()
                            .map(serviceProcessStepProductMapper::toServiceProcessStepProductInfoDto)
                            .collect(Collectors.toList());
                    
                    step.setStepProducts(stepProductDtos);
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
        return serviceProcessMapper.toServiceProcessInfoDto(serviceProcess);
    }
    
    /**
     * Lấy service process theo code
     */
    public ServiceProcessInfoDto getServiceProcessByCode(String code) {
        log.info("Getting service process by code: {}", code);
        ServiceProcess serviceProcess = serviceProcessService.findByCodeOrThrow(code);
        return serviceProcessMapper.toServiceProcessInfoDto(serviceProcess);
    }
    
    /**
     * Lấy service process mặc định
     */
    public ServiceProcessInfoDto getDefaultServiceProcess() {
        log.info("Getting default service process");
        
        // Tìm service process mặc định
        Optional<ServiceProcess> defaultProcess = serviceProcessService.findDefaultProcess();
        
        if (defaultProcess.isPresent()) {
            return serviceProcessMapper.toServiceProcessInfoDto(defaultProcess.get());
        }
        
        // Nếu không có default, lấy service process đầu tiên đang hoạt động
        List<ServiceProcess> activeProcesses = serviceProcessService.findAllActive();
        if (!activeProcesses.isEmpty()) {
            log.info("No default service process found, returning first active process");
            return serviceProcessMapper.toServiceProcessInfoDto(activeProcesses.get(0));
        }
        
        // Nếu không có service process nào, throw exception
        throw new ClientSideException(ErrorCode.SERVICE_PROCESS_DEFAULT_NOT_FOUND);
    }
    
    /**
     * Lấy tất cả service process đang hoạt động
     */
    public List<ServiceProcessInfoDto> getAllActiveServiceProcesses() {
        log.info("Getting all active service processes");
        List<ServiceProcess> serviceProcesses = serviceProcessService.findAllWithProcessSteps();
        // Filter only active processes
        serviceProcesses = serviceProcesses.stream()
                .filter(sp -> sp.getIsActive() != null && sp.getIsActive())
                .collect(Collectors.toList());
        
        // Load stepProducts riêng biệt
        return serviceProcesses.stream()
                .map(serviceProcess -> {
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
                            
                            // Load stepProducts cho từng step
                            List<ServiceProcessStepProduct> stepProducts = serviceProcessStepProductService.findByProcessIdWithProduct(serviceProcess.getId())
                                    .stream()
                                    .filter(spp -> spp.getServiceProcessStep().getId().equals(step.getId()))
                                    .collect(Collectors.toList());
                            
                            List<ServiceProcessStepProductInfoDto> stepProductDtos = stepProducts.stream()
                                    .map(serviceProcessStepProductMapper::toServiceProcessStepProductInfoDto)
                                    .collect(Collectors.toList());
                            
                            step.setStepProducts(stepProductDtos);
                        });
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
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
        
        // Xử lý các bước nếu có
        if (request.getProcessSteps() != null && !request.getProcessSteps().isEmpty()) {
            processServiceProcessSteps(serviceProcess, request.getProcessSteps());
        }
        
        // Cập nhật thời gian dự kiến
        serviceProcess.updateEstimatedDuration();
        serviceProcess = serviceProcessService.update(serviceProcess);
        
        return serviceProcessMapper.toServiceProcessInfoDto(serviceProcess);
    }
    
    /**
     * Cập nhật service process
     */
    @Transactional
    public ServiceProcessInfoDto updateServiceProcess(UUID processId, UpdateServiceProcessRequest request) {
        log.info("Updating service process: {}", processId);
        
        ServiceProcess existingServiceProcess = serviceProcessService.findByIdOrThrow(processId);
        
        // Kiểm tra code đã tồn tại chưa (trừ id hiện tại)
        if (request.getCode() != null && !request.getCode().equals(existingServiceProcess.getCode())) {
            if (serviceProcessService.existsByCodeAndIdNot(request.getCode(), processId)) {
                throw new ClientSideException(ErrorCode.SERVICE_PROCESS_CODE_ALREADY_EXISTS);
            }
        }
        
        // Xử lý các bước trước (bao gồm cả trường hợp xóa tất cả steps)
        if (request.getProcessSteps() != null) {
            processUpdateServiceProcessSteps(existingServiceProcess, request.getProcessSteps());
        }
        
        // Cập nhật thông tin cơ bản sau khi đã xử lý steps
        serviceProcessMapper.updateEntity(existingServiceProcess, request);
        
        // Cập nhật thời gian dự kiến
        existingServiceProcess.updateEstimatedDuration();
        existingServiceProcess = serviceProcessService.update(existingServiceProcess);
        
        return serviceProcessMapper.toServiceProcessInfoDto(existingServiceProcess);
    }
    
    /**
     * Xóa service process
     */
    @Transactional
    public BaseResponse deleteServiceProcess(UUID processId) {
        log.info("Deleting service process: {}", processId);
        
        ServiceProcess serviceProcess = serviceProcessService.findByIdOrThrow(processId);
        
        // Kiểm tra có thể xóa không
        if (!serviceProcessService.canDelete(serviceProcess)) {
            throw new ClientSideException(ErrorCode.SERVICE_PROCESS_CANNOT_DELETE_IN_USE);
        }
        
        serviceProcessService.delete(serviceProcess);
        
        return new BaseResponse(true, "Service process deleted successfully") {};
    }
    
    /**
     * Đặt service process làm mặc định
     */
    @Transactional
    public ServiceProcessInfoDto setDefaultServiceProcess(UUID processId) {
        log.info("Setting default service process: {}", processId);
        
        ServiceProcess serviceProcess = serviceProcessService.findByIdOrThrow(processId);
        serviceProcess = serviceProcessService.setAsDefault(serviceProcess);
        
        return serviceProcessMapper.toServiceProcessInfoDto(serviceProcess);
    }
    
    // ========== SERVICE PROCESS STEP MANAGEMENT ==========
    
    /**
     * Lấy tất cả bước của một service process
     */
    public List<ServiceProcessStepInfoDto> getServiceProcessSteps(UUID processId) {
        log.info("Getting steps for service process: {}", processId);
        
        serviceProcessService.findByIdOrThrow(processId); // Kiểm tra process tồn tại
        List<ServiceProcessStep> steps = serviceProcessStepService.findByProcessId(processId);
        
        return steps.stream()
                .map(serviceProcessStepMapper::toServiceProcessStepInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy bước theo ID
     */
    public ServiceProcessStepInfoDto getServiceProcessStepById(UUID stepId) {
        log.info("Getting service process step by ID: {}", stepId);
        ServiceProcessStep step = serviceProcessStepService.findByIdOrThrow(stepId);
        return serviceProcessStepMapper.toServiceProcessStepInfoDto(step);
    }
    
    /**
     * Thêm bước vào service process
     */
    @Transactional
    public ServiceProcessStepInfoDto addStepToServiceProcess(UUID processId, CreateServiceProcessStepRequest request) {
        log.info("Adding step to service process: {}", processId);
        
        ServiceProcess serviceProcess = serviceProcessService.findByIdOrThrow(processId);
        
        // Kiểm tra step order đã tồn tại chưa
        if (serviceProcessStepService.existsByProcessIdAndStepOrderAndIdNot(processId, request.getStepOrder(), null)) {
            throw new ClientSideException(ErrorCode.SERVICE_PROCESS_STEP_ORDER_ALREADY_EXISTS);
        }
        
        // Tạo bước mới
        ServiceProcessStep step = serviceProcessStepMapper.toEntity(request);
        step.setServiceProcess(serviceProcess);
        
        // Set đầy đủ audit fields
        setAuditFieldsForServiceProcessStep(step);
        
        step = serviceProcessStepService.save(step);
        
        // Xử lý sản phẩm nếu có
        if (request.getStepProducts() != null && !request.getStepProducts().isEmpty()) {
            processServiceProcessStepProducts(step, request.getStepProducts());
        }
        
        return serviceProcessStepMapper.toServiceProcessStepInfoDto(step);
    }
    
    /**
     * Cập nhật bước
     */
    @Transactional
    public ServiceProcessStepInfoDto updateServiceProcessStep(UUID stepId, UpdateServiceProcessStepRequest request) {
        log.info("Updating service process step: {}", stepId);
        
        ServiceProcessStep existingStep = serviceProcessStepService.findByIdOrThrow(stepId);
        
        // Kiểm tra step order đã tồn tại chưa (trừ id hiện tại)
        if (request.getStepOrder() != null && !request.getStepOrder().equals(existingStep.getStepOrder())) {
            if (serviceProcessStepService.existsByProcessIdAndStepOrderAndIdNot(
                    existingStep.getServiceProcess().getId(), request.getStepOrder(), stepId)) {
                throw new ClientSideException(ErrorCode.SERVICE_PROCESS_STEP_ORDER_ALREADY_EXISTS);
            }
        }
        
        // Cập nhật thông tin cơ bản
        serviceProcessStepMapper.updateEntity(existingStep, request);
        
        // Xử lý sản phẩm nếu có
        if (request.getStepProducts() != null && !request.getStepProducts().isEmpty()) {
            processUpdateServiceProcessStepProducts(existingStep, request.getStepProducts());
        }
        
        existingStep = serviceProcessStepService.update(existingStep);
        
        return serviceProcessStepMapper.toServiceProcessStepInfoDto(existingStep);
    }
    
    /**
     * Xóa bước
     */
    @Transactional
    public BaseResponse deleteServiceProcessStep(UUID stepId) {
        log.info("Deleting service process step: {}", stepId);
        
        ServiceProcessStep step = serviceProcessStepService.findByIdOrThrow(stepId);
        serviceProcessStepService.delete(step);
        
        // Sắp xếp lại thứ tự các bước
        serviceProcessStepService.reorderSteps(step.getServiceProcess().getId());
        
        return new BaseResponse(true, "Service process step deleted successfully") {};
    }
    
    
    // ========== SERVICE PROCESS STEP PRODUCT MANAGEMENT ==========
    
    /**
     * Lấy tất cả sản phẩm của một bước
     */
    public List<ServiceProcessStepProductInfoDto> getServiceProcessStepProducts(UUID stepId) {
        log.info("Getting products for service process step: {}", stepId);
        
        serviceProcessStepService.findByIdOrThrow(stepId); // Kiểm tra step tồn tại
        List<ServiceProcessStepProduct> products = serviceProcessStepProductService.findByStepId(stepId);
        
        return products.stream()
                .map(serviceProcessStepProductMapper::toServiceProcessStepProductInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy sản phẩm theo ID
     */
    public ServiceProcessStepProductInfoDto getServiceProcessStepProductById(UUID productId) {
        log.info("Getting service process step product by ID: {}", productId);
        ServiceProcessStepProduct product = serviceProcessStepProductService.findByIdOrThrow(productId);
        return serviceProcessStepProductMapper.toServiceProcessStepProductInfoDto(product);
    }
    
    /**
     * Lấy tất cả sản phẩm của một service process
     */
    public List<ServiceProcessStepProductInfoDto> getServiceProcessProducts(UUID processId) {
        log.info("Getting all products for service process: {}", processId);
        
        // Validate process exists
        serviceProcessService.findByIdOrThrow(processId);
        
        // Get all products in the process
        List<ServiceProcessStepProduct> products = serviceProcessStepProductService.findByProcessIdWithProduct(processId);
        
        return products.stream()
                .map(serviceProcessStepProductMapper::toServiceProcessStepProductInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách ID của tất cả sản phẩm trong service process
     */
    public List<UUID> getServiceProcessProductIds(UUID processId) {
        log.info("Getting product IDs for service process: {}", processId);
        
        // Validate process exists
        serviceProcessService.findByIdOrThrow(processId);
        
        // Get all products in the process
        List<ServiceProcessStepProduct> products = serviceProcessStepProductService.findByProcessIdWithProduct(processId);
        
        return products.stream()
                .map(product -> product.getProduct().getProductId())
                .distinct() // Remove duplicates in case same product is used in multiple steps
                .collect(Collectors.toList());
    }
    
    /**
     * Thêm sản phẩm vào bước
     */
    @Transactional
    public ServiceProcessStepProductInfoDto addProductToStep(UUID stepId, CreateServiceProcessStepProductRequest request) {
        log.info("Adding product to step: {}", stepId);
        
        ServiceProcessStep step = serviceProcessStepService.findByIdOrThrow(stepId);
        
        // Kiểm tra sản phẩm đã tồn tại trong bước chưa
        if (serviceProcessStepProductService.existsByStepIdAndProductIdAndIdNot(stepId, request.getProductId(), null)) {
            throw new ClientSideException(ErrorCode.SERVICE_PROCESS_STEP_PRODUCT_ALREADY_EXISTS);
        }
        
        // Tạo step product mới
        ServiceProcessStepProduct stepProduct = serviceProcessStepProductMapper.toEntity(request);
        stepProduct.setServiceProcessStep(step);
        stepProduct.setProduct(productService.getById(request.getProductId()));
        
        // Set đầy đủ audit fields
        setAuditFieldsForServiceProcessStepProduct(stepProduct);
        
        stepProduct = serviceProcessStepProductService.save(stepProduct);
        
        return serviceProcessStepProductMapper.toServiceProcessStepProductInfoDto(stepProduct);
    }
    
    /**
     * Cập nhật sản phẩm trong bước
     */
    @Transactional
    public ServiceProcessStepProductInfoDto updateServiceProcessStepProduct(UUID productId, UpdateServiceProcessStepProductRequest request) {
        log.info("Updating service process step product: {}", productId);
        
        ServiceProcessStepProduct existingProduct = serviceProcessStepProductService.findByIdOrThrow(productId);
        
        // Kiểm tra sản phẩm đã tồn tại trong bước chưa (trừ id hiện tại)
        if (request.getProductId() != null && !request.getProductId().equals(existingProduct.getProduct().getProductId())) {
            if (serviceProcessStepProductService.existsByStepIdAndProductIdAndIdNot(
                    existingProduct.getServiceProcessStep().getId(), request.getProductId(), productId)) {
                throw new ClientSideException(ErrorCode.SERVICE_PROCESS_STEP_PRODUCT_ALREADY_EXISTS);
            }
        }
        
        // Cập nhật thông tin
        serviceProcessStepProductMapper.updateEntity(existingProduct, request);
        
        // Cập nhật sản phẩm nếu có thay đổi
        if (request.getProductId() != null && !request.getProductId().equals(existingProduct.getProduct().getProductId())) {
            existingProduct.setProduct(productService.getById(request.getProductId()));
        }
        
        existingProduct = serviceProcessStepProductService.update(existingProduct);
        
        return serviceProcessStepProductMapper.toServiceProcessStepProductInfoDto(existingProduct);
    }
    
    /**
     * Xóa sản phẩm khỏi bước
     */
    @Transactional
    public BaseResponse deleteServiceProcessStepProduct(UUID productId) {
        log.info("Deleting service process step product: {}", productId);
        
        ServiceProcessStepProduct product = serviceProcessStepProductService.findByIdOrThrow(productId);
        serviceProcessStepProductService.delete(product);
        
        return new BaseResponse(true, "Service process step product deleted successfully") {};
    }
    
    // ========== PRIVATE HELPER METHODS ==========
    
    /**
     * Xử lý các bước khi tạo service process
     */
    private void processServiceProcessSteps(ServiceProcess serviceProcess, List<CreateServiceProcessStepRequest> stepRequests) {
        for (CreateServiceProcessStepRequest stepRequest : stepRequests) {
            ServiceProcessStep step = serviceProcessStepMapper.toEntity(stepRequest);
            step.setServiceProcess(serviceProcess);
            
            // Set đầy đủ audit fields
            setAuditFieldsForServiceProcessStep(step);
            
            step = serviceProcessStepService.save(step);
            
            // Xử lý sản phẩm nếu có
            if (stepRequest.getStepProducts() != null && !stepRequest.getStepProducts().isEmpty()) {
                processServiceProcessStepProducts(step, stepRequest.getStepProducts());
            }
        }
    }
    
    /**
     * Xử lý cập nhật các bước
     */
    private void processUpdateServiceProcessSteps(ServiceProcess serviceProcess, List<UpdateServiceProcessStepRequest> stepRequests) {
        // Xóa tất cả bước cũ trước
        List<ServiceProcessStep> existingSteps = serviceProcessStepService.findByProcessId(serviceProcess.getId());
        for (ServiceProcessStep step : existingSteps) {
            serviceProcessStepService.delete(step);
        }
        
        // Clear collection để tránh Hibernate cascade issues
        serviceProcess.getProcessSteps().clear();
        
        // Flush để đảm bảo delete operations được thực hiện
        serviceProcessStepService.flush();
        
        // Tạo lại các bước mới
        for (UpdateServiceProcessStepRequest stepRequest : stepRequests) {
            ServiceProcessStep step = new ServiceProcessStep();
            step.setStepOrder(stepRequest.getStepOrder());
            step.setName(stepRequest.getName());
            step.setDescription(stepRequest.getDescription());
            step.setEstimatedTime(stepRequest.getEstimatedTime());
            step.setIsRequired(stepRequest.getIsRequired());
            step.setServiceProcess(serviceProcess);
            
            // Set đầy đủ audit fields
            setAuditFieldsForServiceProcessStep(step);
            
            step = serviceProcessStepService.save(step);
            
            // Xử lý sản phẩm nếu có
            if (stepRequest.getStepProducts() != null && !stepRequest.getStepProducts().isEmpty()) {
                processUpdateServiceProcessStepProducts(step, stepRequest.getStepProducts());
            }
        }
    }
    
    /**
     * Xử lý sản phẩm khi tạo bước
     */
    private void processServiceProcessStepProducts(ServiceProcessStep step, List<CreateServiceProcessStepProductRequest> productRequests) {
        for (CreateServiceProcessStepProductRequest productRequest : productRequests) {
            ServiceProcessStepProduct stepProduct = serviceProcessStepProductMapper.toEntity(productRequest);
            stepProduct.setServiceProcessStep(step);
            stepProduct.setProduct(productService.getById(productRequest.getProductId()));
            
            // Set đầy đủ audit fields
            setAuditFieldsForServiceProcessStepProduct(stepProduct);
            
            serviceProcessStepProductService.save(stepProduct);
        }
    }
    
    /**
     * Xử lý cập nhật sản phẩm
     */
    private void processUpdateServiceProcessStepProducts(ServiceProcessStep step, List<UpdateServiceProcessStepProductRequest> productRequests) {
        // Xóa tất cả sản phẩm cũ
        List<ServiceProcessStepProduct> existingProducts = serviceProcessStepProductService.findByStepId(step.getId());
        for (ServiceProcessStepProduct product : existingProducts) {
            serviceProcessStepProductService.delete(product);
        }
        
        // Tạo lại các sản phẩm mới
        for (UpdateServiceProcessStepProductRequest productRequest : productRequests) {
            ServiceProcessStepProduct stepProduct = new ServiceProcessStepProduct();
            stepProduct.setQuantity(productRequest.getQuantity());
            stepProduct.setUnit(productRequest.getUnit());
            stepProduct.setServiceProcessStep(step);
            stepProduct.setProduct(productService.getById(productRequest.getProductId()));
            
            // Set đầy đủ audit fields
            setAuditFieldsForServiceProcessStepProduct(stepProduct);
            
            serviceProcessStepProductService.save(stepProduct);
        }
    }
    
    /**
     * Helper method để set đầy đủ audit fields cho ServiceProcessStep
     */
    private void setAuditFieldsForServiceProcessStep(ServiceProcessStep step) {
        if (step.getIsActive() == null) {
            step.setIsActive(true);
        }
        if (step.getIsDeleted() == null) {
            step.setIsDeleted(false);
        }
        if (step.getCreatedBy() == null) {
            step.setCreatedBy("admin@scsms.com"); // TODO: Lấy từ SecurityContext
        }
        if (step.getModifiedBy() == null) {
            step.setModifiedBy("admin@scsms.com"); // TODO: Lấy từ SecurityContext
        }
    }
    
    /**
     * Helper method để set đầy đủ audit fields cho ServiceProcessStepProduct
     */
    private void setAuditFieldsForServiceProcessStepProduct(ServiceProcessStepProduct stepProduct) {
        if (stepProduct.getIsActive() == null) {
            stepProduct.setIsActive(true);
        }
        if (stepProduct.getIsDeleted() == null) {
            stepProduct.setIsDeleted(false);
        }
        if (stepProduct.getCreatedBy() == null) {
            stepProduct.setCreatedBy("admin@scsms.com"); // TODO: Lấy từ SecurityContext
        }
        if (stepProduct.getModifiedBy() == null) {
            stepProduct.setModifiedBy("admin@scsms.com"); // TODO: Lấy từ SecurityContext
        }
    }
}
