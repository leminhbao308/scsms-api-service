package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.serviceTypeManagement.ServiceTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceTypeManagement.param.ServiceTypeFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceTypeManagement.request.CreateServiceTypeRequest;
import com.kltn.scsms_api_service.core.dto.serviceTypeManagement.request.UpdateServiceTypeRequest;
import com.kltn.scsms_api_service.core.entity.ServiceType;
import com.kltn.scsms_api_service.core.service.entityService.ServiceTypeService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ServiceTypeMapper;
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
 * Business service for ServiceType management
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceTypeManagementService {
    
    private final ServiceTypeService serviceTypeService;
    private final ServiceTypeMapper serviceTypeMapper;
    
    /**
     * Get all service types
     */
    public List<ServiceTypeInfoDto> getAllServiceTypes() {
        log.info("Getting all service types");
        List<ServiceType> serviceTypes = serviceTypeService.findAll();
        return serviceTypes.stream()
                .map(serviceTypeMapper::toServiceTypeInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all service types with pagination and filters
     */
    public Page<ServiceTypeInfoDto> getAllServiceTypes(ServiceTypeFilterParam filterParam) {
        log.info("Getting all service types with filters: {}", filterParam);
        
        // Standardize filter
        filterParam = ServiceTypeFilterParam.standardize(filterParam);
        
        // Create pageable
        Sort sort = Sort.by(
            filterParam.getDirection().equalsIgnoreCase("DESC") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, 
            filterParam.getSort()
        );
        Pageable pageable = PageRequest.of(filterParam.getPage(), filterParam.getSize(), sort);
        
        // Get service types with filtering
        Page<ServiceType> serviceTypePage = serviceTypeService.findByFilters(
            filterParam.getIsActive(), 
            filterParam.getKeyword(), 
            pageable
        );
        
        return serviceTypePage.map(serviceTypeMapper::toServiceTypeInfoDto);
    }
    
    /**
     * Get service type by ID
     */
    public ServiceTypeInfoDto getServiceTypeById(UUID serviceTypeId) {
        log.info("Getting service type by ID: {}", serviceTypeId);
        ServiceType serviceType = serviceTypeService.getById(serviceTypeId);
        return serviceTypeMapper.toServiceTypeInfoDto(serviceType);
    }
    
    /**
     * Get service type by code
     */
    public ServiceTypeInfoDto getServiceTypeByCode(String code) {
        log.info("Getting service type by code: {}", code);
        ServiceType serviceType = serviceTypeService.getByCode(code);
        return serviceTypeMapper.toServiceTypeInfoDto(serviceType);
    }
    
    /**
     * Get active service types
     */
    public List<ServiceTypeInfoDto> getActiveServiceTypes() {
        log.info("Getting active service types");
        List<ServiceType> serviceTypes = serviceTypeService.findActiveServiceTypesOrdered();
        return serviceTypes.stream()
                .map(serviceTypeMapper::toServiceTypeInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Search service types by keyword
     */
    public List<ServiceTypeInfoDto> searchServiceTypes(String keyword) {
        log.info("Searching service types by keyword: {}", keyword);
        List<ServiceType> serviceTypes = serviceTypeService.searchByKeyword(keyword);
        return serviceTypes.stream()
                .map(serviceTypeMapper::toServiceTypeInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Create new service type
     */
    @Transactional
    public ServiceTypeInfoDto createServiceType(CreateServiceTypeRequest createServiceTypeRequest) {
        log.info("Creating service type: {}", createServiceTypeRequest.getCode());
        
        // Validate service type code uniqueness
        if (serviceTypeService.existsByCode(createServiceTypeRequest.getCode())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Service type code already exists: " + createServiceTypeRequest.getCode());
        }
        
        // Create service type entity
        ServiceType newServiceType = serviceTypeMapper.toEntity(createServiceTypeRequest);
        
        // Save service type
        ServiceType createdServiceType = serviceTypeService.save(newServiceType);
        
        log.info("Created service type with ID: {}", createdServiceType.getServiceTypeId());
        
        return serviceTypeMapper.toServiceTypeInfoDto(createdServiceType);
    }
    
    /**
     * Update existing service type
     */
    @Transactional
    public ServiceTypeInfoDto updateServiceType(UUID serviceTypeId, UpdateServiceTypeRequest updateServiceTypeRequest) {
        log.info("Updating service type with ID: {}", serviceTypeId);
        
        // Get existing service type
        ServiceType existingServiceType = serviceTypeService.getById(serviceTypeId);
        
        // Validate service type code uniqueness if being updated
        if (updateServiceTypeRequest.getCode() != null && 
            !updateServiceTypeRequest.getCode().equals(existingServiceType.getCode())) {
            
            if (serviceTypeService.existsByCodeAndIdNot(updateServiceTypeRequest.getCode(), serviceTypeId)) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Service type code already exists: " + updateServiceTypeRequest.getCode());
            }
        }
        
        // Update service type entity
        ServiceType updatedServiceType = serviceTypeMapper.updateEntity(existingServiceType, updateServiceTypeRequest);
        
        // Save updated service type
        ServiceType savedServiceType = serviceTypeService.update(updatedServiceType);
        
        log.info("Updated service type with ID: {}", savedServiceType.getServiceTypeId());
        
        return serviceTypeMapper.toServiceTypeInfoDto(savedServiceType);
    }
    
    /**
     * Delete service type (soft delete)
     */
    @Transactional
    public void deleteServiceType(UUID serviceTypeId) {
        log.info("Deleting service type with ID: {}", serviceTypeId);
        serviceTypeService.softDeleteById(serviceTypeId);
    }
    
    /**
     * Restore service type (undo soft delete)
     */
    @Transactional
    public void restoreServiceType(UUID serviceTypeId) {
        log.info("Restoring service type with ID: {}", serviceTypeId);
        serviceTypeService.restoreById(serviceTypeId);
    }
    
    /**
     * Update service type status (activate/deactivate)
     */
    @Transactional
    public void updateServiceTypeStatus(UUID serviceTypeId, Boolean isActive) {
        log.info("Updating service type status for ID: {} to active: {}", serviceTypeId, isActive);
        serviceTypeService.updateStatus(serviceTypeId, isActive);
    }
    
    /**
     * Activate service type
     */
    @Transactional
    public void activateServiceType(UUID serviceTypeId) {
        log.info("Activating service type with ID: {}", serviceTypeId);
        serviceTypeService.activate(serviceTypeId);
    }
    
    /**
     * Deactivate service type
     */
    @Transactional
    public void deactivateServiceType(UUID serviceTypeId) {
        log.info("Deactivating service type with ID: {}", serviceTypeId);
        serviceTypeService.deactivate(serviceTypeId);
    }
    
    /**
     * Get service type statistics
     */
    public ServiceTypeStatisticsDto getServiceTypeStatistics() {
        log.info("Getting service type statistics");
        
        return ServiceTypeStatisticsDto.builder()
            .totalServiceTypes((long) serviceTypeService.findAll().size())
            .activeServiceTypes(serviceTypeService.countActiveServiceTypes())
            .inactiveServiceTypes(serviceTypeService.countInactiveServiceTypes())
            .build();
    }
    
    /**
     * Validate service type code
     */
    public boolean validateServiceTypeCode(String code) {
        return !serviceTypeService.existsByCode(code);
    }
    
    /**
     * Validate service type code excluding specific ID
     */
    public boolean validateServiceTypeCode(String code, UUID serviceTypeId) {
        return !serviceTypeService.existsByCodeAndIdNot(code, serviceTypeId);
    }
    
    /**
     * Service type statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ServiceTypeStatisticsDto {
        private Long totalServiceTypes;
        private Long activeServiceTypes;
        private Long inactiveServiceTypes;
    }
}
