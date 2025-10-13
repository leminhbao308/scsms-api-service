package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.ServicePackage;
import com.kltn.scsms_api_service.core.repository.ServicePackageRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServicePackageService {
    
    private final ServicePackageRepository servicePackageRepository;
    
    public List<ServicePackage> findAll() {
        log.info("Finding all service packages");
        return servicePackageRepository.findAll();
    }
    
    public Page<ServicePackage> findAll(Pageable pageable) {
        log.info("Finding all service packages with pagination: {}", pageable);
        return servicePackageRepository.findAll(pageable);
    }
    
    public Optional<ServicePackage> findById(UUID packageId) {
        log.info("Finding service package by ID: {}", packageId);
        return servicePackageRepository.findById(packageId);
    }
    
    public ServicePackage getById(UUID packageId) {
        log.info("Getting service package by ID: {}", packageId);
        return findById(packageId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND, "Service package not found with ID: " + packageId));
    }
    
    public Optional<ServicePackage> findByPackageUrl(String packageUrl) {
        log.info("Finding service package by URL: {}", packageUrl);
        return servicePackageRepository.findByPackageUrl(packageUrl);
    }
    
    public ServicePackage getByPackageUrl(String packageUrl) {
        log.info("Getting service package by URL: {}", packageUrl);
        return findByPackageUrl(packageUrl)
            .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND, "Service package not found with URL: " + packageUrl));
    }
    
    public List<ServicePackage> findByCategoryId(UUID categoryId) {
        log.info("Finding service packages by category ID: {}", categoryId);
        return servicePackageRepository.findByCategoryCategoryId(categoryId);
    }
    
    public List<ServicePackage> findByServicePackageTypeId(UUID servicePackageTypeId) {
        log.info("Finding service packages by service package type ID: {}", servicePackageTypeId);
        return servicePackageRepository.findByServicePackageTypeId(servicePackageTypeId);
    }
    
    public List<ServicePackage> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Finding service packages by price range: {} - {}", minPrice, maxPrice);
        return servicePackageRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    public List<ServicePackage> findByDurationRange(Integer minDuration, Integer maxDuration) {
        log.info("Finding service packages by duration range: {} - {}", minDuration, maxDuration);
        return servicePackageRepository.findByDurationRange(minDuration, maxDuration);
    }
    
    public List<ServicePackage> searchByKeyword(String keyword) {
        log.info("Searching service packages by keyword: {}", keyword);
        return servicePackageRepository.searchByKeyword(keyword);
    }
    
    public boolean existsByPackageUrl(String packageUrl) {
        log.info("Checking if service package exists by URL: {}", packageUrl);
        return servicePackageRepository.existsByPackageUrl(packageUrl);
    }
    
    public long countByCategoryId(UUID categoryId) {
        log.info("Counting service packages by category ID: {}", categoryId);
        return servicePackageRepository.countByCategoryCategoryId(categoryId);
    }
    
    public long countByServicePackageTypeId(UUID servicePackageTypeId) {
        log.info("Counting service packages by service package type ID: {}", servicePackageTypeId);
        return servicePackageRepository.countByServicePackageTypeId(servicePackageTypeId);
    }
    
    public BigDecimal getAveragePriceByPackageType(UUID servicePackageTypeId) {
        log.info("Getting average price by service package type ID: {}", servicePackageTypeId);
        return servicePackageRepository.getAveragePriceByPackageType(servicePackageTypeId);
    }
    
    public Double getAverageDurationByPackageType(UUID servicePackageTypeId) {
        log.info("Getting average duration by service package type ID: {}", servicePackageTypeId);
        return servicePackageRepository.getAverageDurationByPackageType(servicePackageTypeId);
    }
    
    
    @Transactional
    public ServicePackage save(ServicePackage servicePackage) {
        log.info("Saving service package: {}", servicePackage.getPackageName());
        return servicePackageRepository.save(servicePackage);
    }
    
    @Transactional
    public ServicePackage update(ServicePackage servicePackage) {
        log.info("Updating service package: {}", servicePackage.getPackageName());
        return servicePackageRepository.save(servicePackage);
    }
    
    @Transactional
    public void deleteById(UUID packageId) {
        log.info("Deleting service package by ID: {}", packageId);
        if (!servicePackageRepository.existsById(packageId)) {
            throw new ClientSideException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND, "Service package not found with ID: " + packageId);
        }
        servicePackageRepository.deleteById(packageId);
    }
    
    @Transactional
    public void softDeleteById(UUID packageId) {
        log.info("Soft deleting service package by ID: {}", packageId);
        ServicePackage servicePackage = getById(packageId);
        servicePackage.setIsActive(false);
        servicePackage.setIsDeleted(true);
        servicePackageRepository.save(servicePackage);
    }
    
    @Transactional
    public void activateById(UUID packageId) {
        log.info("Activating service package by ID: {}", packageId);
        ServicePackage servicePackage = getById(packageId);
        servicePackage.setIsActive(true);
        servicePackage.setIsDeleted(false);
        servicePackageRepository.save(servicePackage);
    }
    
    @Transactional
    public void deactivateById(UUID packageId) {
        log.info("Deactivating service package by ID: {}", packageId);
        ServicePackage servicePackage = getById(packageId);
        servicePackage.setIsActive(false);
        servicePackageRepository.save(servicePackage);
    }
    
    public ServicePackage getRefById(UUID servicePackageId) {
        return servicePackageRepository.getReferenceById(servicePackageId);
    }
}
