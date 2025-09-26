package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.ServicePackageStep;
import com.kltn.scsms_api_service.core.repository.ServicePackageStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServicePackageStepService {

    private final ServicePackageStepRepository servicePackageStepRepository;

    public List<ServicePackageStep> getAllServicePackageSteps() {
        log.info("Getting all service package steps");
        return servicePackageStepRepository.findAll();
    }

    public Page<ServicePackageStep> getAllServicePackageSteps(Pageable pageable) {
        log.info("Getting all service package steps with pagination");
        return servicePackageStepRepository.findAll(pageable);
    }

    public Optional<ServicePackageStep> getServicePackageStepById(UUID stepId) {
        log.info("Getting service package step by ID: {}", stepId);
        return servicePackageStepRepository.findById(stepId);
    }

    public List<ServicePackageStep> getServicePackageStepsByPackageId(UUID packageId) {
        log.info("Getting service package steps by package ID: {}", packageId);
        return servicePackageStepRepository.findByServicePackage_PackageIdOrderByStepOrder(packageId);
    }

    public List<ServicePackageStep> getActiveServicePackageStepsByPackageId(UUID packageId) {
        log.info("Getting active service package steps by package ID: {}", packageId);
        return servicePackageStepRepository.findByServicePackage_PackageIdAndIsActiveTrueOrderByStepOrder(packageId);
    }

    public Optional<ServicePackageStep> getServicePackageStepByPackageIdAndStepOrder(UUID packageId, Integer stepOrder) {
        log.info("Getting service package step by package ID: {} and step order: {}", packageId, stepOrder);
        return servicePackageStepRepository.findByPackageIdAndStepOrder(packageId, stepOrder);
    }

    public Long countServicePackageStepsByPackageId(UUID packageId) {
        log.info("Counting service package steps by package ID: {}", packageId);
        return servicePackageStepRepository.countByPackageId(packageId);
    }

    public Integer getMaxStepOrderByPackageId(UUID packageId) {
        log.info("Getting max step order by package ID: {}", packageId);
        return servicePackageStepRepository.findMaxStepOrderByPackageId(packageId);
    }

    @Transactional
    public ServicePackageStep createServicePackageStep(ServicePackageStep servicePackageStep) {
        log.info("Creating service package step: {}", servicePackageStep.getStepName());
        return servicePackageStepRepository.save(servicePackageStep);
    }

    @Transactional
    public ServicePackageStep updateServicePackageStep(ServicePackageStep servicePackageStep) {
        log.info("Updating service package step with ID: {}", servicePackageStep.getPackageStepId());
        return servicePackageStepRepository.save(servicePackageStep);
    }

    @Transactional
    public void deleteServicePackageStep(UUID stepId) {
        log.info("Deleting service package step with ID: {}", stepId);
        servicePackageStepRepository.deleteById(stepId);
    }

    @Transactional
    public void softDeleteServicePackageStep(UUID stepId) {
        log.info("Soft deleting service package step with ID: {}", stepId);
        Optional<ServicePackageStep> stepOpt = servicePackageStepRepository.findById(stepId);
        if (stepOpt.isPresent()) {
            ServicePackageStep step = stepOpt.get();
            step.setIsActive(false);
            step.setIsDeleted(true);
            servicePackageStepRepository.save(step);
        }
    }

    @Transactional
    public void activateServicePackageStep(UUID stepId) {
        log.info("Activating service package step with ID: {}", stepId);
        Optional<ServicePackageStep> stepOpt = servicePackageStepRepository.findById(stepId);
        if (stepOpt.isPresent()) {
            ServicePackageStep step = stepOpt.get();
            step.setIsActive(true);
            step.setIsDeleted(false);
            servicePackageStepRepository.save(step);
        }
    }

    @Transactional
    public void deactivateServicePackageStep(UUID stepId) {
        log.info("Deactivating service package step with ID: {}", stepId);
        Optional<ServicePackageStep> stepOpt = servicePackageStepRepository.findById(stepId);
        if (stepOpt.isPresent()) {
            ServicePackageStep step = stepOpt.get();
            step.setIsActive(false);
            servicePackageStepRepository.save(step);
        }
    }
}
