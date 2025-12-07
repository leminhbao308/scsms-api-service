package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.Service;
import com.kltn.scsms_api_service.core.repository.ServiceRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServiceService {
    
    private final ServiceRepository serviceRepository;
    
    public List<Service> findAll() {
        log.info("Finding all services");
        return serviceRepository.findAll();
    }
    
    public Page<Service> findAll(Pageable pageable) {
        log.info("Finding all services with pagination: {}", pageable);
        return serviceRepository.findAll(pageable);
    }
    
    public Page<Service> findAll(Specification<Service> spec, Pageable pageable) {
        log.info("Finding all services with specification and pagination: {}", pageable);
        return serviceRepository.findAll(spec, pageable);
    }
    
    public Optional<Service> findById(UUID serviceId) {
        log.info("Finding service by ID: {}", serviceId);
        return serviceRepository.findById(serviceId);
    }
    
    public Service getById(UUID serviceId) {
        log.info("Getting service by ID: {}", serviceId);
        return findById(serviceId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_NOT_FOUND, "Service not found with ID: " + serviceId));
    }
    
    public Optional<Service> findByServiceUrl(String serviceUrl) {
        log.info("Finding service by URL: {}", serviceUrl);
        return serviceRepository.findByServiceUrl(serviceUrl);
    }
    
    public Service getByServiceUrl(String serviceUrl) {
        log.info("Getting service by URL: {}", serviceUrl);
        return findByServiceUrl(serviceUrl)
            .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_NOT_FOUND, "Service not found with URL: " + serviceUrl));
    }
    
    public List<Service> findByCategoryId(UUID categoryId) {
        log.info("Finding services by category ID: {}", categoryId);
        return serviceRepository.findByCategoryCategoryId(categoryId);
    }
    
    public List<Service> findByServiceTypeId(UUID serviceTypeId) {
        log.info("Finding services by service type ID: {}", serviceTypeId);
        return serviceRepository.findByServiceTypeId(serviceTypeId);
    }
    
    public List<Service> findBySkillLevel(Service.SkillLevel skillLevel) {
        log.info("Finding services by skill level: {}", skillLevel);
        return serviceRepository.findByRequiredSkillLevel(skillLevel);
    }
    
    
    public List<Service> findByDurationRange(Integer minDuration, Integer maxDuration) {
        log.info("Finding services by duration range: {} - {}", minDuration, maxDuration);
        return serviceRepository.findByDurationRange(minDuration, maxDuration);
    }
    
    
    
    
    public List<Service> findFeaturedServices() {
        log.info("Finding featured services");
        return serviceRepository.findByIsFeaturedTrueAndIsActiveTrue();
    }
    
    public List<Service> searchByKeyword(String keyword) {
        log.info("Searching services by keyword: {}", keyword);
        return serviceRepository.searchByKeyword(keyword);
    }
    
    
    
    public boolean existsByServiceUrl(String serviceUrl) {
        log.info("Checking if service exists by URL: {}", serviceUrl);
        return serviceRepository.existsByServiceUrl(serviceUrl);
    }
    
    public long countByCategoryId(UUID categoryId) {
        log.info("Counting services by category ID: {}", categoryId);
        return serviceRepository.countByCategoryCategoryId(categoryId);
    }
    
    public long countByServiceTypeId(UUID serviceTypeId) {
        log.info("Counting services by service type ID: {}", serviceTypeId);
        return serviceRepository.countByServiceTypeId(serviceTypeId);
    }
    
    public long countBySkillLevel(Service.SkillLevel skillLevel) {
        log.info("Counting services by skill level: {}", skillLevel);
        return serviceRepository.countByRequiredSkillLevel(skillLevel);
    }
    
    public Double getAverageDurationByServiceType(UUID serviceTypeId) {
        log.info("Getting average duration by service type ID: {}", serviceTypeId);
        return serviceRepository.getAverageDurationByServiceType(serviceTypeId);
    }
    
    
    @Transactional
    public Service save(Service service) {
        log.info("Saving service: {}", service.getServiceName());
        return serviceRepository.save(service);
    }
    
    @Transactional
    public Service update(Service service) {
        log.info("Updating service: {}", service.getServiceName());
        return serviceRepository.save(service);
    }
    
    @Transactional
    public void deleteById(UUID serviceId) {
        log.info("Deleting service by ID: {}", serviceId);
        if (!serviceRepository.existsById(serviceId)) {
            throw new ClientSideException(ErrorCode.SERVICE_NOT_FOUND, "Service not found with ID: " + serviceId);
        }
        serviceRepository.deleteById(serviceId);
    }
    
    @Transactional
    public void softDeleteById(UUID serviceId) {
        log.info("Soft deleting service by ID: {}", serviceId);
        Service service = getById(serviceId);
        service.setIsActive(false);
        service.setIsDeleted(true);
        serviceRepository.save(service);
    }
    
    @Transactional
    public void activateById(UUID serviceId) {
        log.info("Activating service by ID: {}", serviceId);
        Service service = getById(serviceId);
        service.setIsActive(true);
        service.setIsDeleted(false);
        serviceRepository.save(service);
    }
    
    @Transactional
    public void deactivateById(UUID serviceId) {
        log.info("Deactivating service by ID: {}", serviceId);
        Service service = getById(serviceId);
        service.setIsActive(false);
        serviceRepository.save(service);
    }
    
    public Service getRefById(UUID serviceId) {
        return serviceRepository.getReferenceById(serviceId);
    }
}
