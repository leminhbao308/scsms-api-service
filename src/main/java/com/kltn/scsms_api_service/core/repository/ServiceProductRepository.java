package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServiceProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceProductRepository extends JpaRepository<ServiceProduct, UUID> {
    
    // Find by service
    List<ServiceProduct> findByServiceServiceId(UUID serviceId);
    List<ServiceProduct> findByServiceServiceIdAndIsActiveTrue(UUID serviceId);
    
    // Find by product
    List<ServiceProduct> findByProductProductId(UUID productId);
    List<ServiceProduct> findByProductProductIdAndIsActiveTrue(UUID productId);
    
    // Find specific service-product relationship
    Optional<ServiceProduct> findByServiceServiceIdAndProductProductId(UUID serviceId, UUID productId);
    
    // Check if relationship exists
    boolean existsByServiceServiceIdAndProductProductId(UUID serviceId, UUID productId);
    
    // Find by service with sorting
    @Query("SELECT sp FROM ServiceProduct sp WHERE sp.service.serviceId = :serviceId AND sp.isActive = true ORDER BY sp.createdDate ASC")
    List<ServiceProduct> findByServiceServiceIdOrdered(@Param("serviceId") UUID serviceId);
    
    // Find required products only
    @Query("SELECT sp FROM ServiceProduct sp WHERE sp.service.serviceId = :serviceId AND sp.isRequired = true AND sp.isActive = true ORDER BY sp.createdDate ASC")
    List<ServiceProduct> findRequiredProductsByServiceId(@Param("serviceId") UUID serviceId);
    
    // Find optional products only
    @Query("SELECT sp FROM ServiceProduct sp WHERE sp.service.serviceId = :serviceId AND sp.isRequired = false AND sp.isActive = true ORDER BY sp.createdDate ASC")
    List<ServiceProduct> findOptionalProductsByServiceId(@Param("serviceId") UUID serviceId);
    
    // Calculate total product cost for a service
    @Query("SELECT COALESCE(SUM(sp.totalPrice), 0) FROM ServiceProduct sp WHERE sp.service.serviceId = :serviceId AND sp.isActive = true")
    java.math.BigDecimal calculateTotalProductCostByServiceId(@Param("serviceId") UUID serviceId);
    
    // Count products by service
    long countByServiceServiceId(UUID serviceId);
    long countByServiceServiceIdAndIsActiveTrue(UUID serviceId);
    
    // Count services using a product
    long countByProductProductId(UUID productId);
    long countByProductProductIdAndIsActiveTrue(UUID productId);
    
    // Find services by product
    @Query("SELECT DISTINCT sp.service FROM ServiceProduct sp WHERE sp.product.productId = :productId AND sp.isActive = true")
    List<com.kltn.scsms_api_service.core.entity.Service> findServicesByProductId(@Param("productId") UUID productId);
    
    // Find products by service type
    @Query("SELECT DISTINCT sp.product FROM ServiceProduct sp WHERE sp.service.serviceType = :serviceType AND sp.isActive = true")
    List<com.kltn.scsms_api_service.core.entity.Product> findProductsByServiceType(@Param("serviceType") com.kltn.scsms_api_service.core.entity.Service.ServiceType serviceType);
}
