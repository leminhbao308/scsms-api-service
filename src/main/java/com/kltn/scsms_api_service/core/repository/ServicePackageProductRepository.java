package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServicePackageProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicePackageProductRepository extends JpaRepository<ServicePackageProduct, UUID> {
    
    // Find by package ID
    @Query("SELECT spp FROM ServicePackageProduct spp WHERE spp.servicePackage.packageId = :packageId AND spp.isActive = true ORDER BY spp.createdDate ASC")
    List<ServicePackageProduct> findByPackageIdOrdered(@Param("packageId") UUID packageId);
    
    // Find by package ID and product ID
    @Query("SELECT spp FROM ServicePackageProduct spp WHERE spp.servicePackage.packageId = :packageId AND spp.product.productId = :productId AND spp.isActive = true")
    Optional<ServicePackageProduct> findByPackageIdAndProductId(@Param("packageId") UUID packageId, @Param("productId") UUID productId);
    
    // Check existence
    boolean existsByServicePackagePackageIdAndProductProductIdAndIsActiveTrue(UUID packageId, UUID productId);
    
    // Find by product ID
    @Query("SELECT spp FROM ServicePackageProduct spp WHERE spp.product.productId = :productId AND spp.isActive = true")
    List<ServicePackageProduct> findByProductId(@Param("productId") UUID productId);
    
    // Count by package ID
    long countByServicePackagePackageIdAndIsActiveTrue(UUID packageId);
}
