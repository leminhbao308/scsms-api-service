package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.ServicePackageService;
import com.kltn.scsms_api_service.core.repository.ServicePackageServiceRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServicePackageServiceEntityService {
    
    private final ServicePackageServiceRepository servicePackageServiceRepository;
    
    public List<ServicePackageService> findByPackageIdOrdered(UUID packageId) {
        log.info("Finding service package services by package ID: {}", packageId);
        return servicePackageServiceRepository.findByPackageIdOrdered(packageId);
    }
    
    public Optional<ServicePackageService> findByPackageIdAndServiceId(UUID packageId, UUID serviceId) {
        log.info("Finding service package service by package ID: {} and service ID: {}", packageId, serviceId);
        return servicePackageServiceRepository.findByPackageIdAndServiceId(packageId, serviceId);
    }
    
    public Optional<ServicePackageService> findByPackageIdAndServiceIdIncludingDeleted(UUID packageId, UUID serviceId) {
        log.info("Finding service package service by package ID: {} and service ID: {} (including deleted)", packageId, serviceId);
        return servicePackageServiceRepository.findByPackageIdAndServiceIdIncludingDeleted(packageId, serviceId);
    }
    
    public ServicePackageService getByPackageIdAndServiceId(UUID packageId, UUID serviceId) {
        log.info("Getting service package service by package ID: {} and service ID: {}", packageId, serviceId);
        return findByPackageIdAndServiceId(packageId, serviceId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PACKAGE_SERVICE_NOT_FOUND,
                        "Service package service not found for package ID: " + packageId + " and service ID: " + serviceId));
    }
    
    public boolean existsByPackageIdAndServiceId(UUID packageId, UUID serviceId) {
        log.info("Checking if service package service exists by package ID: {} and service ID: {}", packageId, serviceId);
        return servicePackageServiceRepository.existsByPackageIdAndServiceId(packageId, serviceId);
    }
    
    public long countByPackageId(UUID packageId) {
        log.info("Counting service package services by package ID: {}", packageId);
        return servicePackageServiceRepository.countByPackageId(packageId);
    }
    
    @Transactional
    public ServicePackageService save(ServicePackageService servicePackageService) {
        log.info("Saving service package service for package ID: {} and service ID: {}", 
                servicePackageService.getServicePackage().getPackageId(), 
                servicePackageService.getService().getServiceId());
        
        // Update total price before saving
        servicePackageService.updateTotalPrice();
        
        return servicePackageServiceRepository.save(servicePackageService);
    }
    
    @Transactional
    public ServicePackageService update(ServicePackageService servicePackageService) {
        log.info("Updating service package service with ID: {}", servicePackageService.getServicePackageServiceId());
        
        // Update total price before saving
        servicePackageService.updateTotalPrice();
        
        return servicePackageServiceRepository.save(servicePackageService);
    }
    
    @Transactional
    public void softDeleteById(UUID servicePackageServiceId) {
        log.info("Soft deleting service package service with ID: {}", servicePackageServiceId);
        
        ServicePackageService servicePackageService = servicePackageServiceRepository.findById(servicePackageServiceId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PACKAGE_SERVICE_NOT_FOUND,
                        "Service package service not found with ID: " + servicePackageServiceId));
        
        servicePackageService.setIsDeleted(true);
        servicePackageService.setIsActive(false);
        servicePackageServiceRepository.save(servicePackageService);
    }
    
    @Transactional
    public void softDeleteByPackageIdAndServiceId(UUID packageId, UUID serviceId) {
        log.info("Soft deleting service package service by package ID: {} and service ID: {}", packageId, serviceId);
        
        ServicePackageService servicePackageService = getByPackageIdAndServiceId(packageId, serviceId);
        servicePackageService.setIsDeleted(true);
        servicePackageService.setIsActive(false);
        servicePackageServiceRepository.save(servicePackageService);
    }
    
    @Transactional
    public void hardDeleteById(UUID servicePackageServiceId) {
        log.info("Hard deleting service package service with ID: {}", servicePackageServiceId);
        
        if (!servicePackageServiceRepository.existsById(servicePackageServiceId)) {
            throw new ClientSideException(ErrorCode.SERVICE_PACKAGE_SERVICE_NOT_FOUND,
                    "Service package service not found with ID: " + servicePackageServiceId);
        }
        
        servicePackageServiceRepository.deleteById(servicePackageServiceId);
    }
    
    @Transactional
    public void hardDeleteByPackageIdAndServiceId(UUID packageId, UUID serviceId) {
        log.info("Hard deleting service package service by package ID: {} and service ID: {}", packageId, serviceId);
        
        ServicePackageService servicePackageService = getByPackageIdAndServiceId(packageId, serviceId);
        servicePackageServiceRepository.deleteById(servicePackageService.getServicePackageServiceId());
    }
}
