package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.ServiceType;
import com.kltn.scsms_api_service.core.repository.ServiceTypeRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Entity service for ServiceType
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceTypeService {
    
    private final ServiceTypeRepository serviceTypeRepository;
    
    /**
     * Save service type
     */
    @Transactional
    public ServiceType save(ServiceType serviceType) {
        log.debug("Saving service type: {}", serviceType.getCode());
        return serviceTypeRepository.save(serviceType);
    }
    
    /**
     * Update service type
     */
    @Transactional
    public ServiceType update(ServiceType serviceType) {
        log.debug("Updating service type: {}", serviceType.getCode());
        return serviceTypeRepository.save(serviceType);
    }
    
    /**
     * Find service type by ID
     */
    public Optional<ServiceType> findById(UUID serviceTypeId) {
        return serviceTypeRepository.findById(serviceTypeId);
    }
    
    /**
     * Get service type by ID (throw exception if not found)
     */
    public ServiceType getById(UUID serviceTypeId) {
        return findById(serviceTypeId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                "Service type not found with ID: " + serviceTypeId));
    }
    
    /**
     * Find service type by code
     */
    public Optional<ServiceType> findByCode(String code) {
        return serviceTypeRepository.findByCode(code);
    }
    
    /**
     * Get service type by code (throw exception if not found)
     */
    public ServiceType getByCode(String code) {
        return findByCode(code)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                "Service type not found with code: " + code));
    }
    
    /**
     * Check if service type exists by code
     */
    public boolean existsByCode(String code) {
        return serviceTypeRepository.existsByCode(code);
    }
    
    /**
     * Check if service type exists by code excluding specific ID
     */
    public boolean existsByCodeAndIdNot(String code, UUID serviceTypeId) {
        return serviceTypeRepository.existsByCodeAndServiceTypeIdNot(code, serviceTypeId);
    }
    
    /**
     * Find all service types
     */
    public List<ServiceType> findAll() {
        return serviceTypeRepository.findAll();
    }
    
    /**
     * Find all service types with pagination
     */
    public Page<ServiceType> findAll(Pageable pageable) {
        return serviceTypeRepository.findAll(pageable);
    }
    
    /**
     * Find all active service types
     */
    public List<ServiceType> findActiveServiceTypes() {
        return serviceTypeRepository.findByIsActiveTrueAndIsDeletedFalse();
    }
    
    /**
     * Find all active service types ordered by name
     */
    public List<ServiceType> findActiveServiceTypesOrdered() {
        return serviceTypeRepository.findByIsActiveTrueAndIsDeletedFalseOrderByNameAsc();
    }
    
    /**
     * Search service types by keyword
     */
    public List<ServiceType> searchByKeyword(String keyword) {
        return serviceTypeRepository.searchByKeyword(keyword);
    }
    
    /**
     * Search service types by keyword with pagination
     */
    public Page<ServiceType> searchByKeyword(String keyword, Pageable pageable) {
        return serviceTypeRepository.searchByKeyword(keyword, pageable);
    }
    
    /**
     * Find service types with filters
     */
    public Page<ServiceType> findByFilters(Boolean isActive, String keyword, Pageable pageable) {
        return serviceTypeRepository.findByFilters(isActive, keyword, pageable);
    }
    
    /**
     * Count active service types
     */
    public long countActiveServiceTypes() {
        return serviceTypeRepository.countByIsActiveTrueAndIsDeletedFalse();
    }
    
    /**
     * Count inactive service types
     */
    public long countInactiveServiceTypes() {
        return serviceTypeRepository.countByIsActiveFalseAndIsDeletedFalse();
    }
    
    /**
     * Soft delete service type
     */
    @Transactional
    public void softDeleteById(UUID serviceTypeId) {
        ServiceType serviceType = getById(serviceTypeId);
        serviceType.setIsDeleted(true);
        serviceType.setIsActive(false);
        serviceTypeRepository.save(serviceType);
        log.info("Soft deleted service type: {}", serviceType.getCode());
    }
    
    /**
     * Soft delete service type
     */
    @Transactional
    public void softDelete(ServiceType serviceType) {
        serviceType.setIsDeleted(true);
        serviceType.setIsActive(false);
        serviceTypeRepository.save(serviceType);
        log.info("Soft deleted service type: {}", serviceType.getCode());
    }
    
    /**
     * Restore soft deleted service type
     */
    @Transactional
    public void restoreById(UUID serviceTypeId) {
        ServiceType serviceType = getById(serviceTypeId);
        serviceType.setIsDeleted(false);
        serviceTypeRepository.save(serviceType);
        log.info("Restored service type: {}", serviceType.getCode());
    }
    
    /**
     * Update service type status
     */
    @Transactional
    public void updateStatus(UUID serviceTypeId, Boolean isActive) {
        ServiceType serviceType = getById(serviceTypeId);
        serviceType.setIsActive(isActive);
        serviceTypeRepository.save(serviceType);
        log.info("Updated service type status: {} to {}", serviceType.getCode(), isActive);
    }
    
    /**
     * Activate service type
     */
    @Transactional
    public void activate(UUID serviceTypeId) {
        updateStatus(serviceTypeId, true);
    }
    
    /**
     * Deactivate service type
     */
    @Transactional
    public void deactivate(UUID serviceTypeId) {
        updateStatus(serviceTypeId, false);
    }
}
