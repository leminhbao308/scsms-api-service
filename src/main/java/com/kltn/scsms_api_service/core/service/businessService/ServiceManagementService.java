package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceProductInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.param.ServiceFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceRequest;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceRequest;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.ServiceProcessInfoDto;
import com.kltn.scsms_api_service.core.entity.Category;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.Service;
import com.kltn.scsms_api_service.core.entity.ServiceProcess;
import com.kltn.scsms_api_service.core.entity.ServiceProduct;
import com.kltn.scsms_api_service.core.service.entityService.CategoryService;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceProcessService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceProductService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ServiceMapper;
import com.kltn.scsms_api_service.mapper.ServiceProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServiceManagementService {
    
    private final ServiceService serviceService;
    private final CategoryService categoryService;
    private final ServiceProcessService serviceProcessService;
    private final ServiceProductService serviceProductService;
    private final ProductService productService;
    private final ServiceMapper serviceMapper;
    private final ServiceProductMapper serviceProductMapper;
    
    public List<ServiceInfoDto> getAllServices() {
        log.info("Getting all services");
        List<Service> services = serviceService.findAll();
        return services.stream()
                .map(serviceMapper::toServiceInfoDto)
                .collect(Collectors.toList());
    }
    
    public Page<ServiceInfoDto> getAllServices(ServiceFilterParam filterParam) {
        log.info("Getting all services with filter: {}", filterParam);
        
        // Standardize filter
        filterParam = filterParam.standardizeFilterRequest(filterParam);
        
        // Create pageable
        Sort sort = Sort.by(
            filterParam.getDirection().equalsIgnoreCase("DESC") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, 
            filterParam.getSort()
        );
        Pageable pageable = PageRequest.of(filterParam.getPage(), filterParam.getSize(), sort);
        
        // Get services with filtering
        Page<Service> servicePage = serviceService.findAll(pageable);
        
        return servicePage.map(serviceMapper::toServiceInfoDto);
    }
    
    public ServiceInfoDto getServiceById(UUID serviceId) {
        log.info("Getting service by ID: {}", serviceId);
        Service service = serviceService.getById(serviceId);
        ServiceInfoDto dto = serviceMapper.toServiceInfoDto(service);
        
        // Load service products
        List<ServiceProduct> serviceProducts = serviceProductService.findByServiceIdWithProduct(serviceId);
        List<ServiceProductInfoDto> serviceProductDtos = serviceProducts.stream()
                .map(serviceProductMapper::toServiceProductInfoDto)
                .collect(Collectors.toList());
        dto.setServiceProducts(serviceProductDtos);
        
        // Load service process if exists
        if (service.getServiceProcess() != null) {
            ServiceProcessInfoDto processDto = ServiceProcessInfoDto.builder()
                    .id(service.getServiceProcess().getId())
                    .code(service.getServiceProcess().getCode())
                    .name(service.getServiceProcess().getName())
                    .description(service.getServiceProcess().getDescription())
                    .estimatedDuration(service.getServiceProcess().getEstimatedDuration())
                    .isDefault(service.getServiceProcess().getIsDefault())
                    .isActive(service.getServiceProcess().getIsActive())
                    .build();
            dto.setServiceProcess(processDto);
        }
        
        return dto;
    }
    
    public ServiceInfoDto getServiceByUrl(String serviceUrl) {
        log.info("Getting service by URL: {}", serviceUrl);
        Service service = serviceService.getByServiceUrl(serviceUrl);
        return serviceMapper.toServiceInfoDto(service);
    }
    
    public List<ServiceInfoDto> getServicesByCategory(UUID categoryId) {
        log.info("Getting services by category ID: {}", categoryId);
        List<Service> services = serviceService.findByCategoryId(categoryId);
        return services.stream()
                .map(serviceMapper::toServiceInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServiceInfoDto> getServicesByTypeId(UUID serviceTypeId) {
        log.info("Getting services by type ID: {}", serviceTypeId);
        List<Service> services = serviceService.findByServiceTypeId(serviceTypeId);
        return services.stream()
                .map(serviceMapper::toServiceInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServiceInfoDto> getServicesBySkillLevel(Service.SkillLevel skillLevel) {
        log.info("Getting services by skill level: {}", skillLevel);
        List<Service> services = serviceService.findBySkillLevel(skillLevel);
        return services.stream()
                .map(serviceMapper::toServiceInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServiceInfoDto> searchServices(String keyword) {
        log.info("Searching services by keyword: {}", keyword);
        List<Service> services = serviceService.searchByKeyword(keyword);
        return services.stream()
                .map(serviceMapper::toServiceInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServiceInfoDto> getFeaturedServices() {
        log.info("Getting featured services");
        List<Service> services = serviceService.findFeaturedServices();
        return services.stream()
                .map(serviceMapper::toServiceInfoDto)
                .collect(Collectors.toList());
    }
    
    
    
    @Transactional
    public ServiceInfoDto createService(CreateServiceRequest createServiceRequest) {
        log.info("Creating service: {}", createServiceRequest.getServiceName());
        
        // Validate service URL uniqueness
        if (serviceService.existsByServiceUrl(createServiceRequest.getServiceUrl())) {
            throw new ClientSideException(ErrorCode.SERVICE_URL_EXISTS, 
                "Service URL already exists: " + createServiceRequest.getServiceUrl());
        }
        
        // Validate category exists if provided
        Category category = null;
        if (createServiceRequest.getCategoryId() != null) {
            category = categoryService.getById(createServiceRequest.getCategoryId());
        }
        
        // Handle service process
        ServiceProcess serviceProcess = null;
        if (createServiceRequest.getServiceProcess() != null) {
            // Create new service process
            serviceProcess = createServiceProcessFromRequest(createServiceRequest.getServiceProcess());
        } else if (createServiceRequest.getServiceProcessId() != null) {
            // Use existing service process
            serviceProcess = serviceProcessService.findByIdOrThrow(createServiceRequest.getServiceProcessId());
        }
        
        // Create service
        Service service = serviceMapper.toEntity(createServiceRequest);
        service.setCategory(category);
        service.setServiceProcess(serviceProcess);
        
        // Set default values
        if (service.getIsFeatured() == null) {
            service.setIsFeatured(false);
        }
        
        // Save service
        Service savedService = serviceService.save(service);
        
        // Handle service products
        if (createServiceRequest.getServiceProducts() != null && !createServiceRequest.getServiceProducts().isEmpty()) {
            processServiceProducts(savedService, createServiceRequest.getServiceProducts());
        }
        
        return getServiceById(savedService.getServiceId());
    }
    
    @Transactional
    public ServiceInfoDto updateService(UUID serviceId, UpdateServiceRequest updateServiceRequest) {
        log.info("Updating service with ID: {}", serviceId);
        
        // Get existing service
        Service existingService = serviceService.getById(serviceId);
        
        // Validate service URL uniqueness if changed
        if (updateServiceRequest.getServiceUrl() != null && 
            !updateServiceRequest.getServiceUrl().equals(existingService.getServiceUrl()) &&
            serviceService.existsByServiceUrl(updateServiceRequest.getServiceUrl())) {
            throw new ClientSideException(ErrorCode.SERVICE_URL_EXISTS, 
                "Service URL already exists: " + updateServiceRequest.getServiceUrl());
        }
        
        // Validate category exists if provided
        if (updateServiceRequest.getCategoryId() != null) {
            Category category = categoryService.getById(updateServiceRequest.getCategoryId());
            existingService.setCategory(category);
        }
        
        // Handle service process
        if (updateServiceRequest.getServiceProcess() != null) {
            // Update existing service process
            updateServiceProcessFromRequest(existingService.getServiceProcess(), updateServiceRequest.getServiceProcess());
        } else if (updateServiceRequest.getServiceProcessId() != null) {
            // Use different service process
            ServiceProcess serviceProcess = serviceProcessService.findByIdOrThrow(updateServiceRequest.getServiceProcessId());
            existingService.setServiceProcess(serviceProcess);
        }
        
        // Update service
        Service updatedService = serviceMapper.updateEntity(existingService, updateServiceRequest);
        Service savedService = serviceService.update(updatedService);
        
        // Handle service products
        if (updateServiceRequest.getServiceProducts() != null) {
            processUpdateServiceProducts(savedService, updateServiceRequest.getServiceProducts());
        }
        
        return getServiceById(savedService.getServiceId());
    }
    
    @Transactional
    public void deleteService(UUID serviceId) {
        log.info("Deleting service with ID: {}", serviceId);
        serviceService.softDeleteById(serviceId);
    }
    
    
    @Transactional
    public void updateServiceStatus(UUID serviceId, Boolean isActive) {
        log.info("Updating service status for ID: {} to active: {}", serviceId, isActive);
        Service service = serviceService.getById(serviceId);
        service.setIsActive(isActive);
        // Note: Only update isActive, do not touch isDeleted field
        // isDeleted should only be updated when explicitly deleting the service
        serviceService.update(service);
    }
    
    public long getServiceCountByCategory(UUID categoryId) {
        log.info("Getting service count by category ID: {}", categoryId);
        return serviceService.countByCategoryId(categoryId);
    }
    
    public long getServiceCountByTypeId(UUID serviceTypeId) {
        log.info("Getting service count by type ID: {}", serviceTypeId);
        return serviceService.countByServiceTypeId(serviceTypeId);
    }
    
    public long getServiceCountBySkillLevel(Service.SkillLevel skillLevel) {
        log.info("Getting service count by skill level: {}", skillLevel);
        return serviceService.countBySkillLevel(skillLevel);
    }
    
    // ========== SERVICE PRODUCT MANAGEMENT ==========
    
    /**
     * Lấy tất cả sản phẩm của một service
     */
    public List<ServiceProductInfoDto> getServiceProducts(UUID serviceId) {
        log.info("Getting products for service: {}", serviceId);
        List<ServiceProduct> serviceProducts = serviceProductService.findByServiceIdWithProduct(serviceId);
        return serviceProducts.stream()
                .map(serviceProductMapper::toServiceProductInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Thêm sản phẩm vào service
     */
    @Transactional
    public ServiceProductInfoDto addProductToService(UUID serviceId, com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceProductRequest request) {
        log.info("Adding product to service: {}", serviceId);
        
        // Validate service exists
        Service service = serviceService.getById(serviceId);
        
        // Validate product exists
        Product product = productService.getById(request.getProductId());
        
        // Check if product already exists in service
        if (serviceProductService.existsByServiceIdAndProductIdAndIdNot(serviceId, request.getProductId(), null)) {
            throw new ClientSideException(ErrorCode.SERVICE_PRODUCT_ALREADY_EXISTS);
        }
        
        // Create ServiceProduct
        ServiceProduct serviceProduct = ServiceProduct.builder()
                .service(service)
                .product(product)
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .notes(request.getNotes())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : true)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();
        
        ServiceProduct savedProduct = serviceProductService.save(serviceProduct);
        return serviceProductMapper.toServiceProductInfoDto(savedProduct);
    }
    
    /**
     * Cập nhật service product
     */
    @Transactional
    public ServiceProductInfoDto updateServiceProduct(UUID serviceProductId, com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceProductRequest request) {
        log.info("Updating service product: {}", serviceProductId);
        
        ServiceProduct existingProduct = serviceProductService.findByIdOrThrow(serviceProductId);
        
        // Check if product already exists in service (if productId changed)
        if (request.getProductId() != null && !request.getProductId().equals(existingProduct.getProduct().getProductId())) {
            if (serviceProductService.existsByServiceIdAndProductIdAndIdNot(
                    existingProduct.getService().getServiceId(), request.getProductId(), serviceProductId)) {
                throw new ClientSideException(ErrorCode.SERVICE_PRODUCT_ALREADY_EXISTS);
            }
        }
        
        // Update product information
        serviceProductMapper.updateEntity(existingProduct, request);
        
        // Update product if changed
        if (request.getProductId() != null && !request.getProductId().equals(existingProduct.getProduct().getProductId())) {
            Product product = productService.getById(request.getProductId());
            existingProduct.setProduct(product);
        }
        
        ServiceProduct updatedProduct = serviceProductService.update(existingProduct);
        return serviceProductMapper.toServiceProductInfoDto(updatedProduct);
    }
    
    /**
     * Xóa hẳn sản phẩm khỏi service
     */
    @Transactional
    public void removeProductFromService(UUID serviceProductId) {
        log.info("Removing service product permanently: {}", serviceProductId);
        serviceProductService.deleteById(serviceProductId);
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Tạo ServiceProcess từ CreateServiceProcessRequest
     */
    private ServiceProcess createServiceProcessFromRequest(com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.CreateServiceProcessRequest request) {
        ServiceProcess serviceProcess = ServiceProcess.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .estimatedDuration(request.getEstimatedDuration())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        ServiceProcess savedProcess = serviceProcessService.save(serviceProcess);
        
        // Xử lý các bước nếu có
        if (request.getProcessSteps() != null && !request.getProcessSteps().isEmpty()) {
            // TODO: Implement process steps creation
            // This would require ServiceProcessStepService
        }
        
        return savedProcess;
    }
    
    /**
     * Cập nhật ServiceProcess từ UpdateServiceProcessRequest
     */
    private void updateServiceProcessFromRequest(ServiceProcess existingProcess, com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.UpdateServiceProcessRequest request) {
        if (existingProcess == null) {
            return;
        }
        
        if (request.getCode() != null) {
            existingProcess.setCode(request.getCode());
        }
        if (request.getName() != null) {
            existingProcess.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existingProcess.setDescription(request.getDescription());
        }
        if (request.getEstimatedDuration() != null) {
            existingProcess.setEstimatedDuration(request.getEstimatedDuration());
        }
        if (request.getIsDefault() != null) {
            existingProcess.setIsDefault(request.getIsDefault());
        }
        if (request.getIsActive() != null) {
            existingProcess.setIsActive(request.getIsActive());
        }
        
        serviceProcessService.update(existingProcess);
        
        // Xử lý các bước nếu có
        if (request.getProcessSteps() != null) {
            // TODO: Implement process steps update
            // This would require ServiceProcessStepService
        }
    }
    
    /**
     * Xử lý ServiceProducts khi tạo service
     */
    private void processServiceProducts(Service service, List<com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceProductRequest> productRequests) {
        for (com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceProductRequest productRequest : productRequests) {
            // Validate product exists
            Product product = productService.getById(productRequest.getProductId());
            
            // Create ServiceProduct
            ServiceProduct serviceProduct = ServiceProduct.builder()
                    .service(service)
                    .product(product)
                    .quantity(productRequest.getQuantity())
                    .unit(productRequest.getUnit())
                    .notes(productRequest.getNotes())
                    .isRequired(productRequest.getIsRequired() != null ? productRequest.getIsRequired() : true)
                    .sortOrder(productRequest.getSortOrder() != null ? productRequest.getSortOrder() : 0)
                    .build();
            
            serviceProductService.save(serviceProduct);
        }
    }
    
    /**
     * Xử lý cập nhật ServiceProducts
     */
    private void processUpdateServiceProducts(Service service, List<com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceProductRequest> productRequests) {
        // Xóa tất cả service products cũ
        List<ServiceProduct> existingProducts = serviceProductService.findByServiceId(service.getServiceId());
        for (ServiceProduct product : existingProducts) {
            serviceProductService.delete(product);
        }
        
        // Tạo lại các service products mới
        for (com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceProductRequest productRequest : productRequests) {
            // Validate product exists
            Product product = productService.getById(productRequest.getProductId());
            
            // Create ServiceProduct
            ServiceProduct serviceProduct = ServiceProduct.builder()
                    .service(service)
                    .product(product)
                    .quantity(productRequest.getQuantity())
                    .unit(productRequest.getUnit())
                    .notes(productRequest.getNotes())
                    .isRequired(productRequest.getIsRequired() != null ? productRequest.getIsRequired() : true)
                    .sortOrder(productRequest.getSortOrder() != null ? productRequest.getSortOrder() : 0)
                    .build();
            
            serviceProductService.save(serviceProduct);
        }
    }
}