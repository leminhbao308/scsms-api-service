package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServicePackageService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicePackageServiceRepository extends JpaRepository<ServicePackageService, UUID> {
    
    /**
     * Tìm tất cả service trong package theo package ID
     */
    @Query("SELECT sps FROM ServicePackageService sps " +
           "WHERE sps.servicePackage.packageId = :packageId " +
           "AND sps.isDeleted = false " +
           "ORDER BY sps.createdDate ASC")
    List<ServicePackageService> findByPackageIdOrdered(@Param("packageId") UUID packageId);
    
    /**
     * Tìm service trong package theo package ID và service ID
     */
    @Query("SELECT sps FROM ServicePackageService sps " +
           "WHERE sps.servicePackage.packageId = :packageId " +
           "AND sps.service.serviceId = :serviceId " +
           "AND sps.isDeleted = false")
    Optional<ServicePackageService> findByPackageIdAndServiceId(@Param("packageId") UUID packageId, 
                                                               @Param("serviceId") UUID serviceId);
    
    /**
     * Tìm service trong package theo package ID và service ID (bao gồm cả service đã bị soft delete)
     */
    @Query("SELECT sps FROM ServicePackageService sps " +
           "WHERE sps.servicePackage.packageId = :packageId " +
           "AND sps.service.serviceId = :serviceId")
    Optional<ServicePackageService> findByPackageIdAndServiceIdIncludingDeleted(@Param("packageId") UUID packageId, 
                                                                               @Param("serviceId") UUID serviceId);
    
    /**
     * Kiểm tra service đã tồn tại trong package chưa
     */
    @Query("SELECT COUNT(sps) > 0 FROM ServicePackageService sps " +
           "WHERE sps.servicePackage.packageId = :packageId " +
           "AND sps.service.serviceId = :serviceId " +
           "AND sps.isDeleted = false")
    boolean existsByPackageIdAndServiceId(@Param("packageId") UUID packageId, 
                                         @Param("serviceId") UUID serviceId);
    
    /**
     * Đếm số service trong package
     */
    @Query("SELECT COUNT(sps) FROM ServicePackageService sps " +
           "WHERE sps.servicePackage.packageId = :packageId " +
           "AND sps.isDeleted = false")
    long countByPackageId(@Param("packageId") UUID packageId);
}
