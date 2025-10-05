package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement.ServicePackageTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement.param.ServicePackageTypeFilterParam;
import com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement.request.CreateServicePackageTypeRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement.request.UpdateServicePackageTypeRequest;
import com.kltn.scsms_api_service.core.entity.ServicePackageType;
import com.kltn.scsms_api_service.core.service.entityService.ServicePackageTypeService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ServicePackageTypeMapper;
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

/**
 * Business service for ServicePackageType management
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicePackageTypeManagementService {
    
    private final ServicePackageTypeService servicePackageTypeService;
    private final ServicePackageTypeMapper servicePackageTypeMapper;
    
    /**
     * Get all service package types
     */
    public List<ServicePackageTypeInfoDto> getAllServicePackageTypes() {
        log.info("Getting all service package types");
        List<ServicePackageType> servicePackageTypes = servicePackageTypeService.findAll();
        return servicePackageTypes.stream()
                .map(servicePackageTypeMapper::toServicePackageTypeInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all service package types with pagination and filters
     */
    public Page<ServicePackageTypeInfoDto> getAllServicePackageTypes(ServicePackageTypeFilterParam filterParam) {
        log.info("Getting all service package types with filters: {}", filterParam);
        
        // Standardize filter
        filterParam = ServicePackageTypeFilterParam.standardize(filterParam);
        
        // Create pageable
        Sort sort = Sort.by(
            filterParam.getDirection().equalsIgnoreCase("DESC") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, 
            filterParam.getSort()
        );
        Pageable pageable = PageRequest.of(filterParam.getPage(), filterParam.getSize(), sort);
        
        // Get service package types with filtering
        Page<ServicePackageType> servicePackageTypePage = servicePackageTypeService.findByFilters(
            filterParam.getIsActive(), 
            filterParam.getIsDefault(),
            filterParam.getCustomerType(),
            filterParam.getKeyword(), 
            pageable
        );
        
        return servicePackageTypePage.map(servicePackageTypeMapper::toServicePackageTypeInfoDto);
    }
    
    /**
     * Get service package type by ID
     */
    public ServicePackageTypeInfoDto getServicePackageTypeById(UUID servicePackageTypeId) {
        log.info("Getting service package type by ID: {}", servicePackageTypeId);
        ServicePackageType servicePackageType = servicePackageTypeService.getById(servicePackageTypeId);
        return servicePackageTypeMapper.toServicePackageTypeInfoDto(servicePackageType);
    }
    
    /**
     * Get service package type by code
     */
    public ServicePackageTypeInfoDto getServicePackageTypeByCode(String code) {
        log.info("Getting service package type by code: {}", code);
        ServicePackageType servicePackageType = servicePackageTypeService.getByCode(code);
        return servicePackageTypeMapper.toServicePackageTypeInfoDto(servicePackageType);
    }
    
    /**
     * Get active service package types
     */
    public List<ServicePackageTypeInfoDto> getActiveServicePackageTypes() {
        log.info("Getting active service package types");
        List<ServicePackageType> servicePackageTypes = servicePackageTypeService.findActiveServicePackageTypesOrdered();
        return servicePackageTypes.stream()
                .map(servicePackageTypeMapper::toServicePackageTypeInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get default service package type
     */
    public ServicePackageTypeInfoDto getDefaultServicePackageType() {
        log.info("Getting default service package type");
        ServicePackageType servicePackageType = servicePackageTypeService.getDefaultServicePackageType();
        return servicePackageTypeMapper.toServicePackageTypeInfoDto(servicePackageType);
    }
    
    /**
     * Get service package types applicable for customer type
     */
    public List<ServicePackageTypeInfoDto> getServicePackageTypesForCustomerType(String customerType) {
        log.info("Getting service package types for customer type: {}", customerType);
        List<ServicePackageType> servicePackageTypes = servicePackageTypeService.findApplicableForCustomerType(customerType);
        return servicePackageTypes.stream()
                .map(servicePackageTypeMapper::toServicePackageTypeInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Search service package types by keyword
     */
    public List<ServicePackageTypeInfoDto> searchServicePackageTypes(String keyword) {
        log.info("Searching service package types by keyword: {}", keyword);
        List<ServicePackageType> servicePackageTypes = servicePackageTypeService.searchByKeyword(keyword);
        return servicePackageTypes.stream()
                .map(servicePackageTypeMapper::toServicePackageTypeInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Create new service package type
     */
    @Transactional
    public ServicePackageTypeInfoDto createServicePackageType(CreateServicePackageTypeRequest createServicePackageTypeRequest) {
        log.info("Creating service package type: {}", createServicePackageTypeRequest.getCode());
        
        // Validate service package type code uniqueness
        if (servicePackageTypeService.existsByCode(createServicePackageTypeRequest.getCode())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Service package type code already exists: " + createServicePackageTypeRequest.getCode());
        }
        
        // If setting as default, ensure only one default exists
        if (createServicePackageTypeRequest.getIsDefault() != null && createServicePackageTypeRequest.getIsDefault()) {
            // Remove default status from all other service package types
            servicePackageTypeService.setAsDefault(null); // This will be handled in the service
        }
        
        // Create service package type entity
        ServicePackageType newServicePackageType = servicePackageTypeMapper.toEntity(createServicePackageTypeRequest);
        
        // Save service package type
        ServicePackageType createdServicePackageType = servicePackageTypeService.save(newServicePackageType);
        
        // If this is set as default, update the default status
        if (createServicePackageTypeRequest.getIsDefault() != null && createServicePackageTypeRequest.getIsDefault()) {
            servicePackageTypeService.setAsDefault(createdServicePackageType.getServicePackageTypeId());
        }
        
        log.info("Created service package type with ID: {}", createdServicePackageType.getServicePackageTypeId());
        
        return servicePackageTypeMapper.toServicePackageTypeInfoDto(createdServicePackageType);
    }
    
    /**
     * Update existing service package type
     */
    @Transactional
    public ServicePackageTypeInfoDto updateServicePackageType(UUID servicePackageTypeId, UpdateServicePackageTypeRequest updateServicePackageTypeRequest) {
        log.info("Updating service package type with ID: {}", servicePackageTypeId);
        
        // Get existing service package type
        ServicePackageType existingServicePackageType = servicePackageTypeService.getById(servicePackageTypeId);
        
        // Validate service package type code uniqueness if being updated
        if (updateServicePackageTypeRequest.getCode() != null && 
            !updateServicePackageTypeRequest.getCode().equals(existingServicePackageType.getCode())) {
            
            if (servicePackageTypeService.existsByCodeAndIdNot(updateServicePackageTypeRequest.getCode(), servicePackageTypeId)) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Service package type code already exists: " + updateServicePackageTypeRequest.getCode());
            }
        }
        
        // If setting as default, ensure only one default exists
        if (updateServicePackageTypeRequest.getIsDefault() != null && updateServicePackageTypeRequest.getIsDefault()) {
            // Remove default status from all other service package types
            servicePackageTypeService.setAsDefault(servicePackageTypeId);
        }
        
        // Update service package type entity
        ServicePackageType updatedServicePackageType = servicePackageTypeMapper.updateEntity(existingServicePackageType, updateServicePackageTypeRequest);
        
        // Save updated service package type
        ServicePackageType savedServicePackageType = servicePackageTypeService.update(updatedServicePackageType);
        
        log.info("Updated service package type with ID: {}", savedServicePackageType.getServicePackageTypeId());
        
        return servicePackageTypeMapper.toServicePackageTypeInfoDto(savedServicePackageType);
    }
    
    /**
     * Delete service package type (soft delete)
     */
    @Transactional
    public void deleteServicePackageType(UUID servicePackageTypeId) {
        log.info("Deleting service package type with ID: {}", servicePackageTypeId);
        servicePackageTypeService.softDeleteById(servicePackageTypeId);
    }
    
    /**
     * Restore service package type (undo soft delete)
     */
    @Transactional
    public void restoreServicePackageType(UUID servicePackageTypeId) {
        log.info("Restoring service package type with ID: {}", servicePackageTypeId);
        servicePackageTypeService.restoreById(servicePackageTypeId);
    }
    
    /**
     * Update service package type status (activate/deactivate)
     */
    @Transactional
    public void updateServicePackageTypeStatus(UUID servicePackageTypeId, Boolean isActive) {
        log.info("Updating service package type status for ID: {} to active: {}", servicePackageTypeId, isActive);
        servicePackageTypeService.updateStatus(servicePackageTypeId, isActive);
    }
    
    /**
     * Activate service package type
     */
    @Transactional
    public void activateServicePackageType(UUID servicePackageTypeId) {
        log.info("Activating service package type with ID: {}", servicePackageTypeId);
        servicePackageTypeService.activate(servicePackageTypeId);
    }
    
    /**
     * Deactivate service package type
     */
    @Transactional
    public void deactivateServicePackageType(UUID servicePackageTypeId) {
        log.info("Deactivating service package type with ID: {}", servicePackageTypeId);
        servicePackageTypeService.deactivate(servicePackageTypeId);
    }
    
    /**
     * Set service package type as default
     */
    @Transactional
    public void setAsDefault(UUID servicePackageTypeId) {
        log.info("Setting service package type as default with ID: {}", servicePackageTypeId);
        servicePackageTypeService.setAsDefault(servicePackageTypeId);
    }
    
    /**
     * Remove default status from service package type
     */
    @Transactional
    public void removeDefaultStatus(UUID servicePackageTypeId) {
        log.info("Removing default status from service package type with ID: {}", servicePackageTypeId);
        servicePackageTypeService.removeDefaultStatus(servicePackageTypeId);
    }
    
    /**
     * Get service package type statistics
     */
    public ServicePackageTypeStatisticsDto getServicePackageTypeStatistics() {
        log.info("Getting service package type statistics");
        
        return ServicePackageTypeStatisticsDto.builder()
            .totalServicePackageTypes((long) servicePackageTypeService.findAll().size())
            .activeServicePackageTypes(servicePackageTypeService.countActiveServicePackageTypes())
            .inactiveServicePackageTypes(servicePackageTypeService.countInactiveServicePackageTypes())
            .defaultServicePackageTypes(servicePackageTypeService.countDefaultServicePackageTypes())
            .build();
    }
    
    /**
     * Validate service package type code
     */
    public boolean validateServicePackageTypeCode(String code) {
        return !servicePackageTypeService.existsByCode(code);
    }
    
    /**
     * Validate service package type code excluding specific ID
     */
    public boolean validateServicePackageTypeCode(String code, UUID servicePackageTypeId) {
        return !servicePackageTypeService.existsByCodeAndIdNot(code, servicePackageTypeId);
    }
    
    /**
     * Service package type statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ServicePackageTypeStatisticsDto {
        private Long totalServicePackageTypes;
        private Long activeServicePackageTypes;
        private Long inactiveServicePackageTypes;
        private Long defaultServicePackageTypes;
    }
}
