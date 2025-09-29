package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageInfoDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageStepInfoDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageProductDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.param.ServicePackageFilterParam;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageStepRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageProductRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageServiceRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageStepRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageProductRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageServiceRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageServiceDto;
import com.kltn.scsms_api_service.core.entity.Category;
import com.kltn.scsms_api_service.core.entity.ServicePackage;
import com.kltn.scsms_api_service.core.entity.ServicePackageStep;
import com.kltn.scsms_api_service.core.entity.ServicePackageProduct;
import com.kltn.scsms_api_service.core.entity.ServicePackageService;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.service.entityService.CategoryService;
import com.kltn.scsms_api_service.core.service.entityService.ServicePackageStepService;
import com.kltn.scsms_api_service.core.service.entityService.ServicePackageProductService;
import com.kltn.scsms_api_service.core.service.entityService.ServicePackageServiceEntityService;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ServicePackageMapper;
import com.kltn.scsms_api_service.mapper.ServicePackageProductMapper;
import com.kltn.scsms_api_service.mapper.ServicePackageServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServicePackageManagementService {
    
    private final com.kltn.scsms_api_service.core.service.entityService.ServicePackageService servicePackageEntityService;
    private final ServicePackageStepService servicePackageStepService;
    private final ServicePackageProductService servicePackageProductService;
    private final ServicePackageServiceEntityService servicePackageServiceEntityService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final ServiceService serviceService;
    private final ServicePackageMapper servicePackageMapper;
    private final ServicePackageProductMapper servicePackageProductMapper;
    private final ServicePackageServiceMapper servicePackageServiceMapper;
    
    public List<ServicePackageInfoDto> getAllServicePackages() {
        log.info("Getting all service packages");
        List<ServicePackage> servicePackages = servicePackageEntityService.findAll();
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    public Page<ServicePackageInfoDto> getAllServicePackages(ServicePackageFilterParam filterParam) {
        log.info("Getting all service packages with filter: {}", filterParam);
        
        // Standardize filter
        filterParam = filterParam.standardizeFilterRequest(filterParam);
        
        // Create pageable
        Sort sort = Sort.by(
            filterParam.getDirection().equalsIgnoreCase("DESC") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, 
            filterParam.getSort()
        );
        Pageable pageable = PageRequest.of(filterParam.getPage(), filterParam.getSize(), sort);
        
        // Get service packages (simplified - in real implementation, you'd use custom repository methods)
        Page<ServicePackage> servicePackagePage = servicePackageEntityService.findAll(pageable);
        
        return servicePackagePage.map(servicePackageMapper::toServicePackageInfoDto);
    }
    
    public ServicePackageInfoDto getServicePackageById(UUID packageId) {
        log.info("Getting service package by ID: {}", packageId);
        ServicePackage servicePackage = servicePackageEntityService.getById(packageId);
        return servicePackageMapper.toServicePackageInfoDto(servicePackage);
    }
    
    public ServicePackageInfoDto getServicePackageByUrl(String packageUrl) {
        log.info("Getting service package by URL: {}", packageUrl);
        ServicePackage servicePackage = servicePackageEntityService.getByPackageUrl(packageUrl);
        return servicePackageMapper.toServicePackageInfoDto(servicePackage);
    }
    
    public List<ServicePackageInfoDto> getServicePackagesByCategory(UUID categoryId) {
        log.info("Getting service packages by category ID: {}", categoryId);
        List<ServicePackage> servicePackages = servicePackageEntityService.findByCategoryId(categoryId);
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServicePackageInfoDto> getServicePackagesByType(ServicePackage.PackageType packageType) {
        log.info("Getting service packages by type: {}", packageType);
        List<ServicePackage> servicePackages = servicePackageEntityService.findByPackageType(packageType);
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServicePackageInfoDto> searchServicePackages(String keyword) {
        log.info("Searching service packages by keyword: {}", keyword);
        List<ServicePackage> servicePackages = servicePackageEntityService.searchByKeyword(keyword);
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    
    @Transactional
    public ServicePackageInfoDto createServicePackage(CreateServicePackageRequest createServicePackageRequest) {
        log.info("Creating service package: {}", createServicePackageRequest.getPackageName());
        
        // Validate package URL uniqueness
        if (servicePackageEntityService.existsByPackageUrl(createServicePackageRequest.getPackageUrl())) {
            throw new ClientSideException(ErrorCode.SERVICE_PACKAGE_URL_EXISTS, 
                "Service package URL already exists: " + createServicePackageRequest.getPackageUrl());
        }
        
        // Validate category exists if provided
        Category category = null;
        if (createServicePackageRequest.getCategoryId() != null) {
            category = categoryService.getById(createServicePackageRequest.getCategoryId());
        }
        
        // Create service package
        ServicePackage servicePackage = servicePackageMapper.toEntity(createServicePackageRequest);
        servicePackage.setCategory(category);
        
        // Set default values
        if (servicePackage.getIsActive() == null) {
            servicePackage.setIsActive(true);
        }
        
        ServicePackage savedServicePackage = servicePackageEntityService.save(servicePackage);
        
        // Process package products if provided
        if (createServicePackageRequest.getPackageProducts() != null && !createServicePackageRequest.getPackageProducts().isEmpty()) {
            processServicePackageProducts(savedServicePackage, createServicePackageRequest.getPackageProducts());
        }
        
        // Process package steps if provided
        if (createServicePackageRequest.getPackageSteps() != null && !createServicePackageRequest.getPackageSteps().isEmpty()) {
            processServicePackageSteps(savedServicePackage, createServicePackageRequest.getPackageSteps());
        }
        
        // Process package services if provided
        if (createServicePackageRequest.getPackageServices() != null && !createServicePackageRequest.getPackageServices().isEmpty()) {
            processServicePackageServices(savedServicePackage, createServicePackageRequest.getPackageServices());
        }
        
        // Update pricing after products, steps and services are processed
        savedServicePackage.updatePricing();
        ServicePackage finalServicePackage = servicePackageEntityService.update(savedServicePackage);
        
        return servicePackageMapper.toServicePackageInfoDto(finalServicePackage);
    }
    
    @Transactional
    public ServicePackageInfoDto updateServicePackage(UUID packageId, UpdateServicePackageRequest updateServicePackageRequest) {
        log.info("Updating service package with ID: {}", packageId);
        
        // Get existing service package
        ServicePackage existingServicePackage = servicePackageEntityService.getById(packageId);
        
        // Validate package URL uniqueness if changed
        if (updateServicePackageRequest.getPackageUrl() != null && 
            !updateServicePackageRequest.getPackageUrl().equals(existingServicePackage.getPackageUrl()) &&
            servicePackageEntityService.existsByPackageUrl(updateServicePackageRequest.getPackageUrl())) {
            throw new ClientSideException(ErrorCode.SERVICE_PACKAGE_URL_EXISTS, 
                "Service package URL already exists: " + updateServicePackageRequest.getPackageUrl());
        }
        
        // Validate category exists if provided
        if (updateServicePackageRequest.getCategoryId() != null) {
            Category category = categoryService.getById(updateServicePackageRequest.getCategoryId());
            existingServicePackage.setCategory(category);
        }
        
        // Update service package
        ServicePackage updatedServicePackage = servicePackageMapper.updateEntity(existingServicePackage, updateServicePackageRequest);
        ServicePackage savedServicePackage = servicePackageEntityService.update(updatedServicePackage);
        
        // Process package products if provided
        if (updateServicePackageRequest.getPackageProducts() != null && !updateServicePackageRequest.getPackageProducts().isEmpty()) {
            processUpdateServicePackageProducts(savedServicePackage, updateServicePackageRequest.getPackageProducts());
        }
        
        // Process package steps if provided
        if (updateServicePackageRequest.getPackageSteps() != null && !updateServicePackageRequest.getPackageSteps().isEmpty()) {
            processUpdateServicePackageSteps(savedServicePackage, updateServicePackageRequest.getPackageSteps());
        }
        
        // Process package services if provided
        if (updateServicePackageRequest.getPackageServices() != null && !updateServicePackageRequest.getPackageServices().isEmpty()) {
            processUpdateServicePackageServices(savedServicePackage, updateServicePackageRequest.getPackageServices());
        }
        
        // Update pricing after products, steps and services are processed
        savedServicePackage.updatePricing();
        ServicePackage finalServicePackage = servicePackageEntityService.update(savedServicePackage);
        
        return servicePackageMapper.toServicePackageInfoDto(finalServicePackage);
    }
    
    @Transactional
    public void deleteServicePackage(UUID packageId) {
        log.info("Deleting service package with ID: {}", packageId);
        servicePackageEntityService.softDeleteById(packageId);
    }
    
    @Transactional
    public void updateServicePackageStatus(UUID packageId, Boolean isActive) {
        log.info("Updating service package status for ID: {} to active: {}", packageId, isActive);
        ServicePackage servicePackage = servicePackageEntityService.getById(packageId);
        servicePackage.setIsActive(isActive);
        servicePackageEntityService.update(servicePackage);
    }
    
    public long getServicePackageCountByCategory(UUID categoryId) {
        log.info("Getting service package count by category ID: {}", categoryId);
        return servicePackageEntityService.countByCategoryId(categoryId);
    }
    
    public long getServicePackageCountByType(ServicePackage.PackageType packageType) {
        log.info("Getting service package count by type: {}", packageType);
        return servicePackageEntityService.countByPackageType(packageType);
    }
    
    // ServicePackageStep methods
    public List<ServicePackageStepInfoDto> getServicePackageSteps(UUID packageId) {
        log.info("Getting service package steps for package ID: {}", packageId);
        List<ServicePackageStep> steps = servicePackageStepService.getActiveServicePackageStepsByPackageId(packageId);
        return servicePackageMapper.toServicePackageStepInfoDtoList(steps);
    }
    
    @Transactional
    public ServicePackageStepInfoDto addServicePackageStep(UUID packageId, CreateServicePackageStepRequest createRequest) {
        log.info("Adding service package step to package ID: {}", packageId);
        
        // Validate package exists
        ServicePackage servicePackage = servicePackageEntityService.getById(packageId);
        
        // Create step
        ServicePackageStep step = servicePackageMapper.toServicePackageStep(createRequest);
        step.setServicePackage(servicePackage);
        
        // Set step order if not provided
        if (step.getStepOrder() == null) {
            Integer maxOrder = servicePackageStepService.getMaxStepOrderByPackageId(packageId);
            step.setStepOrder(maxOrder != null ? maxOrder + 1 : 1);
        }
        
        ServicePackageStep savedStep = servicePackageStepService.createServicePackageStep(step);
        return servicePackageMapper.toServicePackageStepInfoDto(savedStep);
    }
    
    @Transactional
    public ServicePackageStepInfoDto updateServicePackageStep(UUID packageId, UUID stepId, UpdateServicePackageStepRequest updateRequest) {
        log.info("Updating service package step with ID: {} for package ID: {}", stepId, packageId);
        
        // Validate package exists
        servicePackageEntityService.getById(packageId);
        
        // Get existing step
        ServicePackageStep existingStep = servicePackageStepService.getServicePackageStepById(stepId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PACKAGE_STEP_NOT_FOUND, 
                "Service package step not found with ID: " + stepId));
        
        // Update step
        ServicePackageStep updatedStep = servicePackageMapper.updateServicePackageStep(existingStep, updateRequest);
        ServicePackageStep savedStep = servicePackageStepService.updateServicePackageStep(updatedStep);
        
        return servicePackageMapper.toServicePackageStepInfoDto(savedStep);
    }
    
    @Transactional
    public void deleteServicePackageStep(UUID packageId, UUID stepId) {
        log.info("Deleting service package step with ID: {} for package ID: {}", stepId, packageId);
        
        // Validate package exists
        servicePackageEntityService.getById(packageId);
        
        // Validate step exists
        servicePackageStepService.getServicePackageStepById(stepId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PACKAGE_STEP_NOT_FOUND, 
                "Service package step not found with ID: " + stepId));
        
        servicePackageStepService.softDeleteServicePackageStep(stepId);
    }
    
    // ServicePackageProduct methods
    private void processServicePackageProducts(ServicePackage servicePackage, List<CreateServicePackageProductRequest> productRequests) {
        log.info("Processing {} products for service package: {}", productRequests.size(), servicePackage.getPackageName());
        
        for (CreateServicePackageProductRequest productRequest : productRequests) {
            // Validate product exists
            Product product = productService.getById(productRequest.getProductId());
            
            // Check if product already exists in package
            if (servicePackageProductService.existsByPackageIdAndProductId(servicePackage.getPackageId(), productRequest.getProductId())) {
                throw new ClientSideException(ErrorCode.SERVICE_PACKAGE_PRODUCT_ALREADY_EXISTS,
                    "Product already exists in service package: " + product.getProductName());
            }
            
            // Create service package product
            ServicePackageProduct servicePackageProduct = servicePackageProductMapper.toEntity(productRequest);
            servicePackageProduct.setServicePackage(servicePackage);
            servicePackageProduct.setProduct(product);
            
            servicePackageProductService.save(servicePackageProduct);
        }
    }
    
    private void processUpdateServicePackageProducts(ServicePackage servicePackage, List<UpdateServicePackageProductRequest> productRequests) {
        log.info("Processing {} product updates for service package: {}", productRequests.size(), servicePackage.getPackageName());
        
        for (UpdateServicePackageProductRequest productRequest : productRequests) {
            if (productRequest.getServicePackageProductId() != null) {
                // Update existing product
                ServicePackageProduct existingProduct = servicePackageProductService.findByPackageIdAndProductId(
                    servicePackage.getPackageId(), productRequest.getProductId())
                    .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PACKAGE_PRODUCT_NOT_FOUND,
                        "Service package product not found"));
                
                ServicePackageProduct updatedProduct = servicePackageProductMapper.updateEntity(existingProduct, productRequest);
                servicePackageProductService.update(updatedProduct);
            } else if (productRequest.getProductId() != null) {
                // Add new product
                Product product = productService.getById(productRequest.getProductId());
                
                if (servicePackageProductService.existsByPackageIdAndProductId(servicePackage.getPackageId(), productRequest.getProductId())) {
                    throw new ClientSideException(ErrorCode.SERVICE_PACKAGE_PRODUCT_ALREADY_EXISTS,
                        "Product already exists in service package: " + product.getProductName());
                }
                
                ServicePackageProduct servicePackageProduct = new ServicePackageProduct();
                servicePackageProduct.setServicePackage(servicePackage);
                servicePackageProduct.setProduct(product);
                servicePackageProduct.setQuantity(productRequest.getQuantity());
                servicePackageProduct.setUnitPrice(productRequest.getUnitPrice());
                servicePackageProduct.setNotes(productRequest.getNotes());
                servicePackageProduct.setIsRequired(productRequest.getIsRequired());
                servicePackageProduct.setIsActive(true);
                servicePackageProduct.setIsDeleted(false);
                
                servicePackageProductService.save(servicePackageProduct);
            }
        }
    }
    
    public List<ServicePackageProductDto> getServicePackageProducts(UUID packageId) {
        log.info("Getting service package products for package ID: {}", packageId);
        List<ServicePackageProduct> products = servicePackageProductService.findByPackageIdOrdered(packageId);
        return servicePackageProductMapper.toServicePackageProductDtoList(products);
    }
    
    @Transactional
    public ServicePackageProductDto addProductToServicePackage(UUID packageId, CreateServicePackageProductRequest createRequest) {
        log.info("Adding product to service package with ID: {}", packageId);
        
        // Validate package exists
        ServicePackage servicePackage = servicePackageEntityService.getById(packageId);
        
        // Validate product exists
        Product product = productService.getById(createRequest.getProductId());
        
        // Check if product already exists in package
        if (servicePackageProductService.existsByPackageIdAndProductId(packageId, createRequest.getProductId())) {
            throw new ClientSideException(ErrorCode.SERVICE_PACKAGE_PRODUCT_ALREADY_EXISTS,
                "Product already exists in service package: " + product.getProductName());
        }
        
        // Create service package product
        ServicePackageProduct servicePackageProduct = servicePackageProductMapper.toEntity(createRequest);
        servicePackageProduct.setServicePackage(servicePackage);
        servicePackageProduct.setProduct(product);
        
        ServicePackageProduct savedProduct = servicePackageProductService.save(servicePackageProduct);
        
        // Update package pricing
        servicePackage.updatePricing();
        servicePackageEntityService.update(servicePackage);
        
        return servicePackageProductMapper.toServicePackageProductDto(savedProduct);
    }
    
    @Transactional
    public ServicePackageProductDto updateServicePackageProduct(UUID packageId, UUID productId, UpdateServicePackageProductRequest updateRequest) {
        log.info("Updating service package product for package ID: {} and product ID: {}", packageId, productId);
        
        // Validate package exists
        servicePackageEntityService.getById(packageId);
        
        // Get existing product
        ServicePackageProduct existingProduct = servicePackageProductService.getByPackageIdAndProductId(packageId, productId);
        
        // Update product
        ServicePackageProduct updatedProduct = servicePackageProductMapper.updateEntity(existingProduct, updateRequest);
        ServicePackageProduct savedProduct = servicePackageProductService.update(updatedProduct);
        
        // Update package pricing
        ServicePackage servicePackage = servicePackageEntityService.getById(packageId);
        servicePackage.updatePricing();
        servicePackageEntityService.update(servicePackage);
        
        return servicePackageProductMapper.toServicePackageProductDto(savedProduct);
    }
    
    @Transactional
    public void removeProductFromServicePackage(UUID packageId, UUID productId) {
        log.info("Removing product from service package with package ID: {} and product ID: {}", packageId, productId);
        
        // Validate package exists
        servicePackageEntityService.getById(packageId);
        
        // Validate product exists
        servicePackageProductService.getByPackageIdAndProductId(packageId, productId);
        
        // Soft delete the product
        ServicePackageProduct product = servicePackageProductService.getByPackageIdAndProductId(packageId, productId);
        servicePackageProductService.softDeleteById(product.getServicePackageProductId());
        
        // Update package pricing
        ServicePackage servicePackage = servicePackageEntityService.getById(packageId);
        servicePackage.updatePricing();
        servicePackageEntityService.update(servicePackage);
    }
    
    // ServicePackageStep methods
    private void processServicePackageSteps(ServicePackage servicePackage, List<CreateServicePackageStepRequest> stepRequests) {
        log.info("Processing {} steps for service package: {}", stepRequests.size(), servicePackage.getPackageName());
        
        for (CreateServicePackageStepRequest stepRequest : stepRequests) {
            // Validate service exists if referenced
            if (stepRequest.getReferencedServiceId() != null) {
                serviceService.getById(stepRequest.getReferencedServiceId());
            }
            
            // Create service package step
            ServicePackageStep servicePackageStep = servicePackageMapper.toServicePackageStep(stepRequest);
            servicePackageStep.setServicePackage(servicePackage);
            
            // Set referenced service if provided
            if (stepRequest.getReferencedServiceId() != null) {
                com.kltn.scsms_api_service.core.entity.Service referencedService = serviceService.getById(stepRequest.getReferencedServiceId());
                servicePackageStep.setReferencedService(referencedService);
            }
            
            servicePackageStepService.createServicePackageStep(servicePackageStep);
        }
    }
    
    private void processUpdateServicePackageSteps(ServicePackage servicePackage, List<UpdateServicePackageStepRequest> stepRequests) {
        log.info("Processing {} step updates for service package: {}", stepRequests.size(), servicePackage.getPackageName());
        
        for (UpdateServicePackageStepRequest stepRequest : stepRequests) {
            // For now, we'll only handle adding new steps
            // Update existing steps can be handled through separate API endpoints
            if (stepRequest.getReferencedServiceId() != null) {
                serviceService.getById(stepRequest.getReferencedServiceId());
            }
            
            ServicePackageStep servicePackageStep = new ServicePackageStep();
            servicePackageStep.setServicePackage(servicePackage);
            servicePackageStep.setStepName(stepRequest.getStepName());
            servicePackageStep.setDescription(stepRequest.getDescription());
            servicePackageStep.setStepOrder(stepRequest.getStepOrder());
            servicePackageStep.setStepType(stepRequest.getStepType());
            servicePackageStep.setEstimatedDuration(stepRequest.getEstimatedDuration());
            servicePackageStep.setInstructions(stepRequest.getInstructions());
            servicePackageStep.setIsOptional(stepRequest.getIsOptional());
            servicePackageStep.setIsActive(true);
            servicePackageStep.setIsDeleted(false);
            
            if (stepRequest.getReferencedServiceId() != null) {
                com.kltn.scsms_api_service.core.entity.Service referencedService = serviceService.getById(stepRequest.getReferencedServiceId());
                servicePackageStep.setReferencedService(referencedService);
            }
            
            servicePackageStepService.createServicePackageStep(servicePackageStep);
        }
    }
    
    // ServicePackageService methods
    private void processServicePackageServices(ServicePackage servicePackage, List<CreateServicePackageServiceRequest> serviceRequests) {
        log.info("Processing {} services for service package: {}", serviceRequests.size(), servicePackage.getPackageName());
        
        for (CreateServicePackageServiceRequest serviceRequest : serviceRequests) {
            // Validate service exists
            com.kltn.scsms_api_service.core.entity.Service service = serviceService.getById(serviceRequest.getServiceId());
            
            // Check if service already exists in package
            if (servicePackageServiceEntityService.existsByPackageIdAndServiceId(servicePackage.getPackageId(), serviceRequest.getServiceId())) {
                throw new ClientSideException(ErrorCode.SERVICE_PACKAGE_SERVICE_ALREADY_EXISTS,
                    "Service already exists in service package: " + service.getServiceName());
            }
            
            // Create service package service
            ServicePackageService servicePackageEntityService = servicePackageServiceMapper.toEntity(serviceRequest);
            servicePackageEntityService.setServicePackage(servicePackage);
            servicePackageEntityService.setService(service);
            
            // Set unit price from service base price if not provided
            if (servicePackageEntityService.getUnitPrice() == null) {
                servicePackageEntityService.setUnitPrice(service.getBasePrice());
            }
            
            servicePackageServiceEntityService.save(servicePackageEntityService);
        }
    }
    
    private void processUpdateServicePackageServices(ServicePackage servicePackage, List<UpdateServicePackageServiceRequest> serviceRequests) {
        log.info("Processing {} service updates for service package: {}", serviceRequests.size(), servicePackage.getPackageName());
        
        for (UpdateServicePackageServiceRequest serviceRequest : serviceRequests) {
            if (serviceRequest.getServicePackageServiceId() != null) {
                // Update existing service
                ServicePackageService existingService = servicePackageServiceEntityService.findByPackageIdAndServiceId(
                    servicePackage.getPackageId(), serviceRequest.getServiceId())
                    .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PACKAGE_SERVICE_NOT_FOUND,
                        "Service package service not found"));
                
                ServicePackageService updatedService = servicePackageServiceMapper.updateEntity(existingService, serviceRequest);
                servicePackageServiceEntityService.update(updatedService);
            } else if (serviceRequest.getServiceId() != null) {
                // Add new service
                com.kltn.scsms_api_service.core.entity.Service service = serviceService.getById(serviceRequest.getServiceId());
                
                if (servicePackageServiceEntityService.existsByPackageIdAndServiceId(servicePackage.getPackageId(), serviceRequest.getServiceId())) {
                    throw new ClientSideException(ErrorCode.SERVICE_PACKAGE_SERVICE_ALREADY_EXISTS,
                        "Service already exists in service package: " + service.getServiceName());
                }
                
                ServicePackageService servicePackageEntityService = new ServicePackageService();
                servicePackageEntityService.setServicePackage(servicePackage);
                servicePackageEntityService.setService(service);
                servicePackageEntityService.setQuantity(serviceRequest.getQuantity());
                servicePackageEntityService.setUnitPrice(serviceRequest.getUnitPrice() != null ? serviceRequest.getUnitPrice() : service.getBasePrice());
                servicePackageEntityService.setNotes(serviceRequest.getNotes());
                servicePackageEntityService.setIsRequired(serviceRequest.getIsRequired());
                servicePackageEntityService.setIsActive(true);
                servicePackageEntityService.setIsDeleted(false);
                
                servicePackageServiceEntityService.save(servicePackageEntityService);
            }
        }
    }
    
    public List<ServicePackageServiceDto> getServicePackageServices(UUID packageId) {
        log.info("Getting service package services for package ID: {}", packageId);
        List<ServicePackageService> services = servicePackageServiceEntityService.findByPackageIdOrdered(packageId);
        return servicePackageServiceMapper.toServicePackageServiceDtoList(services);
    }
    
    @Transactional
    public ServicePackageServiceDto addServiceToServicePackage(UUID packageId, CreateServicePackageServiceRequest createRequest) {
        log.info("Adding service to service package with ID: {}", packageId);
        
        // Validate package exists
        ServicePackage servicePackage = servicePackageEntityService.getById(packageId);
        
        // Validate service exists
        com.kltn.scsms_api_service.core.entity.Service service = serviceService.getById(createRequest.getServiceId());
        
        // Check if service already exists in package
        if (servicePackageServiceEntityService.existsByPackageIdAndServiceId(packageId, createRequest.getServiceId())) {
            throw new ClientSideException(ErrorCode.SERVICE_PACKAGE_SERVICE_ALREADY_EXISTS,
                "Service already exists in service package: " + service.getServiceName());
        }
        
        // Create service package service
        ServicePackageService servicePackageService = servicePackageServiceMapper.toEntity(createRequest);
        servicePackageService.setServicePackage(servicePackage);
        servicePackageService.setService(service);
        
        // Set unit price from service base price if not provided
        if (servicePackageService.getUnitPrice() == null) {
            servicePackageService.setUnitPrice(service.getBasePrice());
        }
        
        ServicePackageService savedService = servicePackageServiceEntityService.save(servicePackageService);
        
        // Update package pricing
        servicePackage.updatePricing();
        servicePackageEntityService.update(servicePackage);
        
        return servicePackageServiceMapper.toServicePackageServiceDto(savedService);
    }
    
    @Transactional
    public ServicePackageServiceDto updateServicePackageService(UUID packageId, UUID serviceId, UpdateServicePackageServiceRequest updateRequest) {
        log.info("Updating service package service for package ID: {} and service ID: {}", packageId, serviceId);
        
        // Validate package exists
        servicePackageEntityService.getById(packageId);
        
        // Get existing service
        ServicePackageService existingService = servicePackageServiceEntityService.getByPackageIdAndServiceId(packageId, serviceId);
        
        // Update service
        ServicePackageService updatedService = servicePackageServiceMapper.updateEntity(existingService, updateRequest);
        ServicePackageService savedService = servicePackageServiceEntityService.update(updatedService);
        
        // Update package pricing
        ServicePackage servicePackage = servicePackageEntityService.getById(packageId);
        servicePackage.updatePricing();
        servicePackageEntityService.update(servicePackage);
        
        return servicePackageServiceMapper.toServicePackageServiceDto(savedService);
    }
    
    @Transactional
    public void removeServiceFromServicePackage(UUID packageId, UUID serviceId) {
        log.info("Removing service from service package with package ID: {} and service ID: {}", packageId, serviceId);
        
        // Validate package exists
        servicePackageEntityService.getById(packageId);
        
        // Validate service exists
        servicePackageServiceEntityService.getByPackageIdAndServiceId(packageId, serviceId);
        
        // Soft delete the service
        servicePackageServiceEntityService.softDeleteByPackageIdAndServiceId(packageId, serviceId);
        
        // Update package pricing
        ServicePackage servicePackage = servicePackageEntityService.getById(packageId);
        servicePackage.updatePricing();
        servicePackageEntityService.update(servicePackage);
    }
}