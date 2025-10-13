package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageInfoDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.param.ServicePackageFilterParam;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageServiceRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageServiceRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageServiceDto;
import com.kltn.scsms_api_service.core.entity.Category;
import com.kltn.scsms_api_service.core.entity.ServicePackage;
import com.kltn.scsms_api_service.core.entity.ServicePackageService;
import com.kltn.scsms_api_service.core.entity.ServiceProcess;
import com.kltn.scsms_api_service.core.service.entityService.CategoryService;
import com.kltn.scsms_api_service.core.service.entityService.ServicePackageServiceEntityService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceProcessService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
import com.kltn.scsms_api_service.core.repository.ServicePackageServiceRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ServicePackageMapper;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServicePackageManagementService {
    
    private final com.kltn.scsms_api_service.core.service.entityService.ServicePackageService servicePackageEntityService;
    private final ServicePackageServiceEntityService servicePackageServiceEntityService;
    private final ServicePackageServiceRepository servicePackageServiceRepository;
    private final CategoryService categoryService;
    private final ServiceService serviceService;
    private final ServiceProcessService serviceProcessService;
    private final ServicePackageMapper servicePackageMapper;
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
    
    public List<ServicePackageInfoDto> getServicePackagesByTypeId(UUID servicePackageTypeId) {
        log.info("Getting service packages by type ID: {}", servicePackageTypeId);
        List<ServicePackage> servicePackages = servicePackageEntityService.findByServicePackageTypeId(servicePackageTypeId);
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
        
        // Validate service process exists if provided
        ServiceProcess serviceProcess = null;
        if (createServicePackageRequest.getServiceProcessId() != null) {
            serviceProcess = serviceProcessService.findByIdOrThrow(createServicePackageRequest.getServiceProcessId());
        }
        
        // Create service package
        ServicePackage servicePackage = servicePackageMapper.toEntity(createServicePackageRequest);
        servicePackage.setCategory(category);
        servicePackage.setServiceProcess(serviceProcess);
        
        // Set branchId if provided
        if (createServicePackageRequest.getBranchId() != null) {
            servicePackage.setBranchId(createServicePackageRequest.getBranchId());
        }
        
        // Set default values
        if (servicePackage.getIsActive() == null) {
            servicePackage.setIsActive(true);
        }
        
        ServicePackage savedServicePackage = servicePackageEntityService.save(servicePackage);
        
        
        // Process package services if provided
        if (createServicePackageRequest.getPackageServices() != null && !createServicePackageRequest.getPackageServices().isEmpty()) {
            processServicePackageServices(savedServicePackage, createServicePackageRequest.getPackageServices());
        }
        
        // Update pricing after products and services are processed
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
        
        // Validate service process exists if provided
        if (updateServicePackageRequest.getServiceProcessId() != null) {
            ServiceProcess serviceProcess = serviceProcessService.findByIdOrThrow(updateServicePackageRequest.getServiceProcessId());
            existingServicePackage.setServiceProcess(serviceProcess);
        }
        
        // Update service package
        ServicePackage updatedServicePackage = servicePackageMapper.updateEntity(existingServicePackage, updateServicePackageRequest);
        ServicePackage savedServicePackage = servicePackageEntityService.update(updatedServicePackage);
        
        
        // Process package services
        if (updateServicePackageRequest.getPackageServices() == null) {
            // No package_services in payload means delete all existing services
            log.info("No package_services in payload - deleting all existing services from package: {}", packageId);
            List<ServicePackageService> existingServices = servicePackageServiceEntityService.findByPackageIdOrdered(packageId);
            for (ServicePackageService existingService : existingServices) {
                servicePackageServiceEntityService.softDeleteById(existingService.getServicePackageServiceId());
            }
            log.info("Deleted {} existing services from package (no package_services in payload)", existingServices.size());
        } else if (updateServicePackageRequest.getPackageServices().isEmpty()) {
            // Empty array means delete all existing services
            log.info("Empty package_services array - deleting all existing services from package: {}", packageId);
            List<ServicePackageService> existingServices = servicePackageServiceEntityService.findByPackageIdOrdered(packageId);
            for (ServicePackageService existingService : existingServices) {
                servicePackageServiceEntityService.softDeleteById(existingService.getServicePackageServiceId());
            }
            log.info("Deleted {} existing services from package", existingServices.size());
        } else {
            // First, get all existing services BEFORE processing the request
            List<ServicePackageService> existingServices = servicePackageServiceEntityService.findByPackageIdOrdered(packageId);
            Set<UUID> existingServiceIds = existingServices.stream()
                .map(ServicePackageService::getServicePackageServiceId)
                .collect(Collectors.toSet());
            
            log.info("Found {} existing services in package: {}", existingServiceIds.size(), existingServiceIds);
            
            // Get the service IDs that are being kept (not deleted) from the request
            Set<UUID> keptServiceIds = new java.util.HashSet<>();
            
            // Process each service in the request to determine which ones to keep
            for (UpdateServicePackageServiceRequest serviceRequest : updateServicePackageRequest.getPackageServices()) {
                // Skip DELETE operations as they are handled separately
                if (serviceRequest.getOperation() != null && "DELETE".equalsIgnoreCase(serviceRequest.getOperation())) {
                    continue;
                }
                
                // If service_package_service_id is provided, keep this service
                if (serviceRequest.getServicePackageServiceId() != null) {
                    keptServiceIds.add(serviceRequest.getServicePackageServiceId());
                    log.debug("Keeping service by service_package_service_id: {}", serviceRequest.getServicePackageServiceId());
                }
                // If only service_id is provided, find the corresponding service_package_service_id and keep it
                else if (serviceRequest.getServiceId() != null) {
                    Optional<ServicePackageService> existing = servicePackageServiceEntityService.findByPackageIdAndServiceId(
                        packageId, serviceRequest.getServiceId());
                    if (existing.isPresent()) {
                        keptServiceIds.add(existing.get().getServicePackageServiceId());
                        log.debug("Keeping service by service_id: {} (service_package_service_id: {})", 
                            serviceRequest.getServiceId(), existing.get().getServicePackageServiceId());
                    }
                }
            }
            
            log.info("Services to keep: {}", keptServiceIds);
            
            // Delete services that are not in the kept list
            Set<UUID> servicesToDelete = existingServiceIds.stream()
                .filter(id -> !keptServiceIds.contains(id))
                .collect(Collectors.toSet());
            
            log.info("Services to delete: {}", servicesToDelete);
            
            // Hard delete services that are not in the request
            for (UUID serviceIdToDelete : servicesToDelete) {
                servicePackageServiceEntityService.hardDeleteById(serviceIdToDelete);
                log.info("Hard deleted service from package (not in request): {}", serviceIdToDelete);
            }
            
            // Process the services in the request (including DELETE operations)
            processUpdateServicePackageServices(savedServicePackage, updateServicePackageRequest.getPackageServices());
            
            if (!servicesToDelete.isEmpty()) {
                log.info("Deleted {} services that were not in the request", servicesToDelete.size());
            }
        }
        
        // Update pricing after products and services are processed
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
        // Note: Only update isActive, do not touch isDeleted field
        // isDeleted should only be updated when explicitly deleting the service package
        servicePackageEntityService.update(servicePackage);
    }
    
    public long getServicePackageCountByCategory(UUID categoryId) {
        log.info("Getting service package count by category ID: {}", categoryId);
        return servicePackageEntityService.countByCategoryId(categoryId);
    }
    
    public long getServicePackageCountByTypeId(UUID servicePackageTypeId) {
        log.info("Getting service package count by type ID: {}", servicePackageTypeId);
        return servicePackageEntityService.countByServicePackageTypeId(servicePackageTypeId);
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
            String operation = serviceRequest.getOperation();
            log.info("Processing service request - service_id: {}, service_package_service_id: {}, operation: {}", 
                serviceRequest.getServiceId(), serviceRequest.getServicePackageServiceId(), operation);
            
            // Handle DELETE operation
            if (operation != null && "DELETE".equalsIgnoreCase(operation)) {
                log.info("Processing DELETE operation for service_id: {}, service_package_service_id: {}", 
                    serviceRequest.getServiceId(), serviceRequest.getServicePackageServiceId());
                
                if (serviceRequest.getServicePackageServiceId() != null) {
                    // Delete by servicePackageServiceId
                    servicePackageServiceEntityService.hardDeleteById(serviceRequest.getServicePackageServiceId());
                    log.info("Hard deleted service from service package by ID: {}", serviceRequest.getServicePackageServiceId());
                } else if (serviceRequest.getServiceId() != null) {
                    // Delete by serviceId
                    Optional<ServicePackageService> existingServiceOpt = servicePackageServiceEntityService.findByPackageIdAndServiceId(
                        servicePackage.getPackageId(), serviceRequest.getServiceId());
                    if (existingServiceOpt.isPresent()) {
                        servicePackageServiceEntityService.hardDeleteById(existingServiceOpt.get().getServicePackageServiceId());
                        log.info("Hard deleted service from service package by serviceId: {}", serviceRequest.getServiceId());
                    } else {
                        log.warn("Service not found in package for deletion: {}", serviceRequest.getServiceId());
                    }
                } else {
                    log.warn("DELETE operation requires either service_package_service_id or service_id");
                }
                continue;
            }
            
            // Handle CREATE/UPDATE operations (existing logic)
            if (serviceRequest.getServiceId() != null && (operation == null || "CREATE".equalsIgnoreCase(operation) || "UPDATE".equalsIgnoreCase(operation))) {
                log.info("Processing CREATE/UPDATE operation for service_id: {}, operation: {}", serviceRequest.getServiceId(), operation);
                Optional<ServicePackageService> existingServiceOpt = Optional.empty();
                
                // First, try to find by service_package_service_id if provided
                if (serviceRequest.getServicePackageServiceId() != null) {
                    existingServiceOpt = servicePackageServiceRepository.findById(serviceRequest.getServicePackageServiceId());
                    log.info("Looking for existing service by service_package_service_id: {}", serviceRequest.getServicePackageServiceId());
                }
                
                // If not found by service_package_service_id, try by package_id and service_id
                if (existingServiceOpt.isEmpty()) {
                    existingServiceOpt = servicePackageServiceEntityService.findByPackageIdAndServiceId(
                        servicePackage.getPackageId(), serviceRequest.getServiceId());
                    log.info("Looking for existing service by package_id and service_id: {} - {}", 
                        servicePackage.getPackageId(), serviceRequest.getServiceId());
                }
                
                if (existingServiceOpt.isPresent()) {
                    // Update existing service
                    ServicePackageService existingService = existingServiceOpt.get();
                    ServicePackageService updatedService = servicePackageServiceMapper.updateEntity(existingService, serviceRequest);
                    servicePackageServiceEntityService.update(updatedService);
                    log.info("Updated existing service in service package: {} (service_package_service_id: {})", 
                        serviceRequest.getServiceId(), existingService.getServicePackageServiceId());
                } else {
                    // Add new service
                    com.kltn.scsms_api_service.core.entity.Service service = serviceService.getById(serviceRequest.getServiceId());
                    
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
                    log.info("Added new service to service package: {}", serviceRequest.getServiceId());
                }
            } else {
                log.warn("Skipping service request - service_id: {}, operation: {} (not supported or missing service_id)", 
                    serviceRequest.getServiceId(), operation);
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
        ServicePackage servicePackage = servicePackageEntityService.getById(packageId);
        
        // Find service (including soft deleted ones)
        Optional<ServicePackageService> serviceOpt = servicePackageServiceEntityService.findByPackageIdAndServiceIdIncludingDeleted(packageId, serviceId);
        
        if (serviceOpt.isEmpty()) {
            throw new ClientSideException(ErrorCode.SERVICE_PACKAGE_SERVICE_NOT_FOUND,
                "Service package service not found for package ID: " + packageId + " and service ID: " + serviceId);
        }
        
        ServicePackageService servicePackageService = serviceOpt.get();
        
        // Remove from package's service list first to avoid cascade issues
        servicePackage.getPackageServices().removeIf(sps -> 
            sps.getServicePackageServiceId().equals(servicePackageService.getServicePackageServiceId()));
        
        // Hard delete the service
        servicePackageServiceEntityService.hardDeleteById(servicePackageService.getServicePackageServiceId());
        log.info("Hard deleted service from service package: {}", servicePackageService.getServicePackageServiceId());
        
        // Update package pricing
        servicePackage.updatePricing();
        servicePackageEntityService.update(servicePackage);
        
        log.info("Successfully removed service from service package");
    }
}