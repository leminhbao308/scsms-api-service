package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServicePackageStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicePackageStepRepository extends JpaRepository<ServicePackageStep, UUID> {
    
    List<ServicePackageStep> findByServicePackage_PackageIdOrderByStepOrder(UUID packageId);
    
    List<ServicePackageStep> findByServicePackage_PackageIdAndIsActiveTrueOrderByStepOrder(UUID packageId);
    
    List<ServicePackageStep> findByStepType(ServicePackageStep.StepType stepType);
    
    List<ServicePackageStep> findByReferencedService_ServiceId(UUID serviceId);
    
    @Query("SELECT sps FROM ServicePackageStep sps WHERE sps.servicePackage.packageId = :packageId AND sps.stepOrder = :stepOrder")
    Optional<ServicePackageStep> findByPackageIdAndStepOrder(@Param("packageId") UUID packageId, @Param("stepOrder") Integer stepOrder);
    
    @Query("SELECT COUNT(sps) FROM ServicePackageStep sps WHERE sps.servicePackage.packageId = :packageId")
    Long countByPackageId(@Param("packageId") UUID packageId);
    
    @Query("SELECT MAX(sps.stepOrder) FROM ServicePackageStep sps WHERE sps.servicePackage.packageId = :packageId")
    Integer findMaxStepOrderByPackageId(@Param("packageId") UUID packageId);
}
