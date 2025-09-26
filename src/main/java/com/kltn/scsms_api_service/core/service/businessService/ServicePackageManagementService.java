package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageInfoDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageStepInfoDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.param.ServicePackageFilterParam;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageStepRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageStepRequest;
import com.kltn.scsms_api_service.core.entity.Category;
import com.kltn.scsms_api_service.core.entity.ServicePackage;
import com.kltn.scsms_api_service.core.entity.ServicePackageStep;
import com.kltn.scsms_api_service.core.service.entityService.CategoryService;
import com.kltn.scsms_api_service.core.service.entityService.ServicePackageService;
import com.kltn.scsms_api_service.core.service.entityService.ServicePackageStepService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ServicePackageMapper;
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
    
    private final ServicePackageService servicePackageService;
    private final ServicePackageStepService servicePackageStepService;
    private final CategoryService categoryService;
    private final ServicePackageMapper servicePackageMapper;
    
    public List<ServicePackageInfoDto> getAllServicePackages() {
        log.info("Getting all service packages");
        List<ServicePackage> servicePackages = servicePackageService.findAll();
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
        Page<ServicePackage> servicePackagePage = servicePackageService.findAll(pageable);
        
        return servicePackagePage.map(servicePackageMapper::toServicePackageInfoDto);
    }
    
    public ServicePackageInfoDto getServicePackageById(UUID packageId) {
        log.info("Getting service package by ID: {}", packageId);
        ServicePackage servicePackage = servicePackageService.getById(packageId);
        return servicePackageMapper.toServicePackageInfoDto(servicePackage);
    }
    
    public ServicePackageInfoDto getServicePackageByUrl(String packageUrl) {
        log.info("Getting service package by URL: {}", packageUrl);
        ServicePackage servicePackage = servicePackageService.getByPackageUrl(packageUrl);
        return servicePackageMapper.toServicePackageInfoDto(servicePackage);
    }
    
    public List<ServicePackageInfoDto> getServicePackagesByCategory(UUID categoryId) {
        log.info("Getting service packages by category ID: {}", categoryId);
        List<ServicePackage> servicePackages = servicePackageService.findByCategoryId(categoryId);
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServicePackageInfoDto> getServicePackagesByType(ServicePackage.PackageType packageType) {
        log.info("Getting service packages by type: {}", packageType);
        List<ServicePackage> servicePackages = servicePackageService.findByPackageType(packageType);
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServicePackageInfoDto> searchServicePackages(String keyword) {
        log.info("Searching service packages by keyword: {}", keyword);
        List<ServicePackage> servicePackages = servicePackageService.searchByKeyword(keyword);
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServicePackageInfoDto> getPopularServicePackages() {
        log.info("Getting popular service packages");
        List<ServicePackage> servicePackages = servicePackageService.findPopularPackages();
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServicePackageInfoDto> getRecommendedServicePackages() {
        log.info("Getting recommended service packages");
        List<ServicePackage> servicePackages = servicePackageService.findRecommendedPackages();
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServicePackageInfoDto> getLimitedTimeServicePackages() {
        log.info("Getting limited time service packages");
        List<ServicePackage> servicePackages = servicePackageService.findLimitedTimePackages();
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServicePackageInfoDto> getCurrentlyActiveServicePackages() {
        log.info("Getting currently active service packages");
        List<ServicePackage> servicePackages = servicePackageService.findCurrentlyActivePackages();
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServicePackageInfoDto> getExpiredServicePackages() {
        log.info("Getting expired service packages");
        List<ServicePackage> servicePackages = servicePackageService.findExpiredPackages();
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServicePackageInfoDto> getUpcomingServicePackages() {
        log.info("Getting upcoming service packages");
        List<ServicePackage> servicePackages = servicePackageService.findUpcomingPackages();
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServicePackageInfoDto> getServicePackagesWithBestSavings() {
        log.info("Getting service packages with best savings");
        List<ServicePackage> servicePackages = servicePackageService.findPackagesWithBestSavings();
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ServicePackageInfoDto> getServicePackagesWithHighestDiscount() {
        log.info("Getting service packages with highest discount");
        List<ServicePackage> servicePackages = servicePackageService.findPackagesWithHighestDiscount();
        return servicePackages.stream()
                .map(servicePackageMapper::toServicePackageInfoDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ServicePackageInfoDto createServicePackage(CreateServicePackageRequest createServicePackageRequest) {
        log.info("Creating service package: {}", createServicePackageRequest.getPackageName());
        
        // Validate package URL uniqueness
        if (servicePackageService.existsByPackageUrl(createServicePackageRequest.getPackageUrl())) {
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
        if (servicePackage.getIsLimitedTime() == null) {
            servicePackage.setIsLimitedTime(false);
        }
        if (servicePackage.getIsPopular() == null) {
            servicePackage.setIsPopular(false);
        }
        if (servicePackage.getIsRecommended() == null) {
            servicePackage.setIsRecommended(false);
        }
        if (servicePackage.getSortOrder() == null) {
            servicePackage.setSortOrder(0);
        }
        
        ServicePackage savedServicePackage = servicePackageService.save(servicePackage);
        return servicePackageMapper.toServicePackageInfoDto(savedServicePackage);
    }
    
    @Transactional
    public ServicePackageInfoDto updateServicePackage(UUID packageId, UpdateServicePackageRequest updateServicePackageRequest) {
        log.info("Updating service package with ID: {}", packageId);
        
        // Get existing service package
        ServicePackage existingServicePackage = servicePackageService.getById(packageId);
        
        // Validate package URL uniqueness if changed
        if (updateServicePackageRequest.getPackageUrl() != null && 
            !updateServicePackageRequest.getPackageUrl().equals(existingServicePackage.getPackageUrl()) &&
            servicePackageService.existsByPackageUrl(updateServicePackageRequest.getPackageUrl())) {
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
        ServicePackage savedServicePackage = servicePackageService.update(updatedServicePackage);
        
        return servicePackageMapper.toServicePackageInfoDto(savedServicePackage);
    }
    
    @Transactional
    public void deleteServicePackage(UUID packageId) {
        log.info("Deleting service package with ID: {}", packageId);
        servicePackageService.softDeleteById(packageId);
    }
    
    @Transactional
    public void activateServicePackage(UUID packageId) {
        log.info("Activating service package with ID: {}", packageId);
        servicePackageService.activateById(packageId);
    }
    
    @Transactional
    public void deactivateServicePackage(UUID packageId) {
        log.info("Deactivating service package with ID: {}", packageId);
        servicePackageService.deactivateById(packageId);
    }
    
    public long getServicePackageCountByCategory(UUID categoryId) {
        log.info("Getting service package count by category ID: {}", categoryId);
        return servicePackageService.countByCategoryId(categoryId);
    }
    
    public long getServicePackageCountByType(ServicePackage.PackageType packageType) {
        log.info("Getting service package count by type: {}", packageType);
        return servicePackageService.countByPackageType(packageType);
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
        ServicePackage servicePackage = servicePackageService.getById(packageId);
        
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
        servicePackageService.getById(packageId);
        
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
        servicePackageService.getById(packageId);
        
        // Validate step exists
        servicePackageStepService.getServicePackageStepById(stepId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PACKAGE_STEP_NOT_FOUND, 
                "Service package step not found with ID: " + stepId));
        
        servicePackageStepService.softDeleteServicePackageStep(stepId);
    }
}