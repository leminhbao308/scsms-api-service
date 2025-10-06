package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.param.ServiceFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceRequest;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceRequest;
import com.kltn.scsms_api_service.core.entity.Category;
import com.kltn.scsms_api_service.core.entity.Service;
import com.kltn.scsms_api_service.core.service.entityService.CategoryService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final ServiceMapper serviceMapper;
    
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
        
        // Update pricing
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
}