package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceProductDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.param.ServiceFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceRequest;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceProductRequest;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceRequest;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceProductRequest;
import com.kltn.scsms_api_service.core.entity.Category;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.Service;
import com.kltn.scsms_api_service.core.entity.ServiceProduct;
import com.kltn.scsms_api_service.core.service.entityService.CategoryService;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceProductService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
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

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private final ProductService productService;
    private final ServiceProductService serviceProductService;
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
        return serviceMapper.toServiceInfoDto(service);
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
    
    public List<ServiceInfoDto> getServicesByType(Service.ServiceType serviceType) {
        log.info("Getting services by type: {}", serviceType);
        List<Service> services = serviceService.findByServiceType(serviceType);
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
    
    
    public List<ServiceInfoDto> getPackageServices() {
        log.info("Getting package services");
        List<Service> services = serviceService.findPackageServices();
        return services.stream()
                .map(serviceMapper::toServiceInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServiceInfoDto> getNonPackageServices() {
        log.info("Getting non-package services");
        List<Service> services = serviceService.findNonPackageServices();
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
        
        // Create service
        Service service = serviceMapper.toEntity(createServiceRequest);
        service.setCategory(category);
        
        // Set default values
        if (service.getIsPackage() == null) {
            service.setIsPackage(false);
        }
        if (service.getPhotoRequired() == null) {
            service.setPhotoRequired(false);
        }

        if (service.getIsFeatured() == null) {
            service.setIsFeatured(false);
        }
        if (service.getLaborCost() == null) {
            service.setLaborCost(BigDecimal.ZERO);
        }
        
        // Save service first
        Service savedService = serviceService.save(service);
        
        // Process service products if provided
        if (createServiceRequest.getServiceProducts() != null && !createServiceRequest.getServiceProducts().isEmpty()) {
            processServiceProducts(savedService, createServiceRequest.getServiceProducts());
        }
        
        // Update pricing after adding products
        savedService.updatePricing();
        savedService = serviceService.update(savedService);
        
        return serviceMapper.toServiceInfoDto(savedService);
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
        
        // Update service
        Service updatedService = serviceMapper.updateEntity(existingService, updateServiceRequest);
        
        // Process service products if provided
        if (updateServiceRequest.getServiceProducts() != null) {
            processUpdateServiceProducts(updatedService, updateServiceRequest.getServiceProducts());
        }
        
        // Update pricing after changes
        updatedService.updatePricing();
        Service savedService = serviceService.update(updatedService);
        
        return serviceMapper.toServiceInfoDto(savedService);
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
    
    public long getServiceCountByType(Service.ServiceType serviceType) {
        log.info("Getting service count by type: {}", serviceType);
        return serviceService.countByServiceType(serviceType);
    }
    
    public long getServiceCountBySkillLevel(Service.SkillLevel skillLevel) {
        log.info("Getting service count by skill level: {}", skillLevel);
        return serviceService.countBySkillLevel(skillLevel);
    }
    
    // Service Product Management Methods
    
    /**
     * Xử lý thêm service products khi tạo service
     */
    private void processServiceProducts(Service service, List<CreateServiceProductRequest> serviceProductRequests) {
        log.info("Processing {} service products for service: {}", serviceProductRequests.size(), service.getServiceId());
        
        for (CreateServiceProductRequest request : serviceProductRequests) {
            // Validate product exists
            Product product = productService.getById(request.getProductId());
            
            // Check if product already exists in service
            if (serviceProductService.existsByServiceIdAndProductId(service.getServiceId(), request.getProductId())) {
                throw new ClientSideException(ErrorCode.SERVICE_PRODUCT_ALREADY_EXISTS, 
                    "Product already exists in service: " + product.getProductName());
            }
            
            // Create service product
            ServiceProduct serviceProduct = serviceProductMapper.toEntity(request);
            serviceProduct.setService(service);
            serviceProduct.setProduct(product);
            
            // Set default values
            if (serviceProduct.getIsRequired() == null) {
                serviceProduct.setIsRequired(true);
            }
            
            serviceProductService.save(serviceProduct);
        }
    }
    
    /**
     * Xử lý cập nhật service products khi update service
     */
    private void processUpdateServiceProducts(Service service, List<UpdateServiceProductRequest> serviceProductRequests) {
        log.info("Processing {} service product updates for service: {}", serviceProductRequests.size(), service.getServiceId());
        
        // Get existing service products
        List<ServiceProduct> existingServiceProducts = serviceProductService.findByServiceId(service.getServiceId());
        List<UUID> existingIds = existingServiceProducts.stream()
                .map(ServiceProduct::getServiceProductId)
                .collect(Collectors.toList());
        
        // Process each request
        List<UUID> updatedIds = new ArrayList<>();
        
        for (UpdateServiceProductRequest request : serviceProductRequests) {
            if (request.getServiceProductId() != null) {
                // Update existing service product
                ServiceProduct existingServiceProduct = serviceProductService.getById(request.getServiceProductId());
                
                // Validate product if changed
                if (request.getProductId() != null && !request.getProductId().equals(existingServiceProduct.getProduct().getProductId())) {
                    Product product = productService.getById(request.getProductId());
                    existingServiceProduct.setProduct(product);
                }
                
                ServiceProduct updatedServiceProduct = serviceProductMapper.updateEntity(existingServiceProduct, request);
                serviceProductService.update(updatedServiceProduct);
                updatedIds.add(request.getServiceProductId());
            } else if (request.getProductId() != null) {
                // Add new service product
                Product product = productService.getById(request.getProductId());
                
                // Check if product already exists in service
                if (serviceProductService.existsByServiceIdAndProductId(service.getServiceId(), request.getProductId())) {
                    throw new ClientSideException(ErrorCode.SERVICE_PRODUCT_ALREADY_EXISTS, 
                        "Product already exists in service: " + product.getProductName());
                }
                
                ServiceProduct newServiceProduct = ServiceProduct.builder()
                        .service(service)
                        .product(product)
                        .quantity(request.getQuantity())
                        .unitPrice(request.getUnitPrice())
                        .notes(request.getNotes())
                        .isRequired(request.getIsRequired() != null ? request.getIsRequired() : true)
                        .isActive(true)
                        .isDeleted(false)
                        .build();
                
                serviceProductService.save(newServiceProduct);
            }
        }
        
        // Remove service products that are not in the update list
        List<UUID> toRemove = existingIds.stream()
                .filter(id -> !updatedIds.contains(id))
                .collect(Collectors.toList());
        
        for (UUID serviceProductId : toRemove) {
            serviceProductService.softDeleteById(serviceProductId);
        }
    }
    
    /**
     * Lấy danh sách service products của một service
     */
    public List<ServiceProductDto> getServiceProducts(UUID serviceId) {
        log.info("Getting service products for service: {}", serviceId);
        List<ServiceProduct> serviceProducts = serviceProductService.findByServiceIdOrdered(serviceId);
        return serviceProducts.stream()
                .map(serviceProductMapper::toServiceProductDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Thêm sản phẩm vào service
     */
    @Transactional
    public ServiceProductDto addProductToService(UUID serviceId, CreateServiceProductRequest request) {
        log.info("Adding product {} to service {}", request.getProductId(), serviceId);
        
        Service service = serviceService.getById(serviceId);
        Product product = productService.getById(request.getProductId());
        
        // Check if product already exists in service
        if (serviceProductService.existsByServiceIdAndProductId(serviceId, request.getProductId())) {
            throw new ClientSideException(ErrorCode.SERVICE_PRODUCT_ALREADY_EXISTS, 
                "Product already exists in service: " + product.getProductName());
        }
        
        ServiceProduct serviceProduct = serviceProductMapper.toEntity(request);
        serviceProduct.setService(service);
        serviceProduct.setProduct(product);
        
        ServiceProduct savedServiceProduct = serviceProductService.save(serviceProduct);
        
        // Update service pricing
        service.updatePricing();
        serviceService.update(service);
        
        return serviceProductMapper.toServiceProductDto(savedServiceProduct);
    }
    
    /**
     * Cập nhật sản phẩm trong service
     */
    @Transactional
    public ServiceProductDto updateServiceProduct(UUID serviceProductId, UpdateServiceProductRequest request) {
        log.info("Updating service product: {}", serviceProductId);
        
        ServiceProduct existingServiceProduct = serviceProductService.getById(serviceProductId);
        
        // Validate product if changed
        if (request.getProductId() != null && !request.getProductId().equals(existingServiceProduct.getProduct().getProductId())) {
            Product product = productService.getById(request.getProductId());
            existingServiceProduct.setProduct(product);
        }
        
        ServiceProduct updatedServiceProduct = serviceProductMapper.updateEntity(existingServiceProduct, request);
        ServiceProduct savedServiceProduct = serviceProductService.update(updatedServiceProduct);
        
        // Update service pricing
        Service service = existingServiceProduct.getService();
        service.updatePricing();
        serviceService.update(service);
        
        return serviceProductMapper.toServiceProductDto(savedServiceProduct);
    }
    
    /**
     * Xóa sản phẩm khỏi service
     */
    @Transactional
    public void removeProductFromService(UUID serviceProductId) {
        log.info("Removing service product: {}", serviceProductId);
        
        ServiceProduct serviceProduct = serviceProductService.getById(serviceProductId);
        Service service = serviceProduct.getService();
        
        serviceProductService.softDeleteById(serviceProductId);
        
        // Update service pricing
        service.updatePricing();
        serviceService.update(service);
    }
}