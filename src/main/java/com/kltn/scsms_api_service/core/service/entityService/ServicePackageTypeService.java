package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.ServicePackageType;
import com.kltn.scsms_api_service.core.repository.ServicePackageTypeRepository;
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
 * Entity service for ServicePackageType
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicePackageTypeService {

    private final ServicePackageTypeRepository servicePackageTypeRepository;

    /**
     * Save service package type
     */
    @Transactional
    public ServicePackageType save(ServicePackageType servicePackageType) {
        log.debug("Saving service package type: {}", servicePackageType.getCode());
        return servicePackageTypeRepository.save(servicePackageType);
    }

    /**
     * Update service package type
     */
    @Transactional
    public ServicePackageType update(ServicePackageType servicePackageType) {
        log.debug("Updating service package type: {}", servicePackageType.getCode());
        return servicePackageTypeRepository.save(servicePackageType);
    }

    /**
     * Find service package type by ID
     */
    public Optional<ServicePackageType> findById(UUID servicePackageTypeId) {
        return servicePackageTypeRepository.findById(servicePackageTypeId);
    }

    /**
     * Get service package type by ID (throw exception if not found)
     */
    public ServicePackageType getById(UUID servicePackageTypeId) {
        return findById(servicePackageTypeId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                        "Service package type not found with ID: " + servicePackageTypeId));
    }

    /**
     * Find service package type by code
     */
    public Optional<ServicePackageType> findByCode(String code) {
        return servicePackageTypeRepository.findByCode(code);
    }

    /**
     * Get service package type by code (throw exception if not found)
     */
    public ServicePackageType getByCode(String code) {
        return findByCode(code)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                        "Service package type not found with code: " + code));
    }

    /**
     * Check if service package type exists by code
     */
    public boolean existsByCode(String code) {
        return servicePackageTypeRepository.existsByCode(code);
    }

    /**
     * Check if service package type exists by code excluding specific ID
     */
    public boolean existsByCodeAndIdNot(String code, UUID servicePackageTypeId) {
        return servicePackageTypeRepository.existsByCodeAndServicePackageTypeIdNot(code, servicePackageTypeId);
    }

    /**
     * Find all service package types
     */
    public List<ServicePackageType> findAll() {
        return servicePackageTypeRepository.findAll();
    }

    /**
     * Find all service package types with pagination
     */
    public Page<ServicePackageType> findAll(Pageable pageable) {
        return servicePackageTypeRepository.findAll(pageable);
    }

    /**
     * Find all active service package types
     */
    public List<ServicePackageType> findActiveServicePackageTypes() {
        return servicePackageTypeRepository.findByIsActiveTrueAndIsDeletedFalse();
    }

    /**
     * Find all active service package types ordered by name
     */
    public List<ServicePackageType> findActiveServicePackageTypesOrdered() {
        return servicePackageTypeRepository.findByIsActiveTrueAndIsDeletedFalseOrderByNameAsc();
    }

    /**
     * Find default service package type
     */
    public Optional<ServicePackageType> findDefaultServicePackageType() {
        return servicePackageTypeRepository.findByIsDefaultTrueAndIsActiveTrueAndIsDeletedFalse();
    }

    /**
     * Get default service package type (throw exception if not found)
     */
    public ServicePackageType getDefaultServicePackageType() {
        return findDefaultServicePackageType()
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                        "Default service package type not found"));
    }

    /**
     * Find service package types by applicable customer type
     */
    public List<ServicePackageType> findByApplicableCustomerType(String customerType) {
        return servicePackageTypeRepository.findByApplicableCustomerTypeAndIsActiveTrueAndIsDeletedFalse(customerType);
    }

    /**
     * Find service package types applicable for customer type
     */
    public List<ServicePackageType> findApplicableForCustomerType(String customerType) {
        return servicePackageTypeRepository.findApplicableForCustomerType(customerType);
    }

    /**
     * Search service package types by keyword
     */
    public List<ServicePackageType> searchByKeyword(String keyword) {
        return servicePackageTypeRepository.searchByKeyword(keyword);
    }

    /**
     * Search service package types by keyword with pagination
     */
    public Page<ServicePackageType> searchByKeyword(String keyword, Pageable pageable) {
        return servicePackageTypeRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * Find service package types with filters
     */
    public Page<ServicePackageType> findByFilters(Boolean isActive, Boolean isDefault,
            String customerType, String keyword, Pageable pageable) {
        return servicePackageTypeRepository.findByFilters(isActive, isDefault, customerType, keyword, pageable);
    }

    /**
     * Count active service package types
     */
    public long countActiveServicePackageTypes() {
        return servicePackageTypeRepository.countByIsActiveTrueAndIsDeletedFalse();
    }

    /**
     * Count inactive service package types
     */
    public long countInactiveServicePackageTypes() {
        return servicePackageTypeRepository.countByIsActiveFalseAndIsDeletedFalse();
    }

    /**
     * Count default service package types
     */
    public long countDefaultServicePackageTypes() {
        return servicePackageTypeRepository.countByIsDefaultTrueAndIsDeletedFalse();
    }

    /**
     * Soft delete service package type
     */
    @Transactional
    public void softDeleteById(UUID servicePackageTypeId) {
        ServicePackageType servicePackageType = getById(servicePackageTypeId);
        servicePackageType.setIsDeleted(true);
        servicePackageType.setIsActive(false);
        servicePackageType.setIsDefault(false);
        servicePackageTypeRepository.save(servicePackageType);
        log.info("Soft deleted service package type: {}", servicePackageType.getCode());
    }

    /**
     * Soft delete service package type
     */
    @Transactional
    public void softDelete(ServicePackageType servicePackageType) {
        servicePackageType.setIsDeleted(true);
        servicePackageType.setIsActive(false);
        servicePackageType.setIsDefault(false);
        servicePackageTypeRepository.save(servicePackageType);
        log.info("Soft deleted service package type: {}", servicePackageType.getCode());
    }

    /**
     * Restore soft deleted service package type
     */
    @Transactional
    public void restoreById(UUID servicePackageTypeId) {
        ServicePackageType servicePackageType = getById(servicePackageTypeId);
        servicePackageType.setIsDeleted(false);
        servicePackageTypeRepository.save(servicePackageType);
        log.info("Restored service package type: {}", servicePackageType.getCode());
    }

    /**
     * Update service package type status
     */
    @Transactional
    public void updateStatus(UUID servicePackageTypeId, Boolean isActive) {
        ServicePackageType servicePackageType = getById(servicePackageTypeId);
        servicePackageType.setIsActive(isActive);
        servicePackageTypeRepository.save(servicePackageType);
        log.info("Updated service package type status: {} to {}", servicePackageType.getCode(), isActive);
    }

    /**
     * Activate service package type
     */
    @Transactional
    public void activate(UUID servicePackageTypeId) {
        updateStatus(servicePackageTypeId, true);
    }

    /**
     * Deactivate service package type
     */
    @Transactional
    public void deactivate(UUID servicePackageTypeId) {
        updateStatus(servicePackageTypeId, false);
    }

    /**
     * Set as default service package type
     */
    @Transactional
    public void setAsDefault(UUID servicePackageTypeId) {
        // First, remove default status from all other service package types
        List<ServicePackageType> allTypes = servicePackageTypeRepository.findByIsDefaultTrueAndIsDeletedFalse();
        for (ServicePackageType type : allTypes) {
            type.setIsDefault(false);   
            servicePackageTypeRepository.save(type);
        }

        // Then set the specified one as default
        ServicePackageType servicePackageType = getById(servicePackageTypeId);
        servicePackageType.setIsDefault(true);
        servicePackageType.setIsActive(true); // Default type should be active
        servicePackageTypeRepository.save(servicePackageType);
        log.info("Set service package type as default: {}", servicePackageType.getCode());
    }

    /**
     * Remove default status from service package type
     */
    @Transactional
    public void removeDefaultStatus(UUID servicePackageTypeId) {
        ServicePackageType servicePackageType = getById(servicePackageTypeId);
        servicePackageType.setIsDefault(false);
        servicePackageTypeRepository.save(servicePackageType);
        log.info("Removed default status from service package type: {}", servicePackageType.getCode());
    }
}
