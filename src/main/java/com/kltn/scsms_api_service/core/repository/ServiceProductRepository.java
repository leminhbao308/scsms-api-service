package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServiceProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceProductRepository extends JpaRepository<ServiceProduct, UUID> {
    
    /**
     * Tìm tất cả sản phẩm của một service
     */
    List<ServiceProduct> findByServiceServiceIdAndIsDeletedFalseOrderBySortOrderAsc(UUID serviceId);
    
    /**
     * Tìm sản phẩm theo service và product
     */
    @Query("SELECT sp FROM ServiceProduct sp WHERE " +
           "sp.service.serviceId = :serviceId AND " +
           "sp.product.productId = :productId AND " +
           "sp.isDeleted = false")
    Optional<ServiceProduct> findByServiceIdAndProductIdAndIsDeletedFalse(@Param("serviceId") UUID serviceId, 
                                                                          @Param("productId") UUID productId);
    
    /**
     * Tìm tất cả service sử dụng một product
     */
    @Query("SELECT sp FROM ServiceProduct sp WHERE " +
           "sp.product.productId = :productId AND " +
           "sp.isDeleted = false " +
           "ORDER BY sp.service.serviceName ASC")
    List<ServiceProduct> findByProductIdAndIsDeletedFalse(@Param("productId") UUID productId);
    
    /**
     * Tìm sản phẩm theo tên sản phẩm trong một service
     */
    @Query("SELECT sp FROM ServiceProduct sp WHERE " +
           "sp.service.serviceId = :serviceId AND " +
           "LOWER(sp.product.productName) LIKE LOWER(CONCAT('%', :productName, '%')) AND " +
           "sp.isDeleted = false " +
           "ORDER BY sp.sortOrder ASC")
    Page<ServiceProduct> findByServiceIdAndProductNameContainingIgnoreCase(@Param("serviceId") UUID serviceId, 
                                                                           @Param("productName") String productName, 
                                                                           Pageable pageable);
    
    /**
     * Tìm sản phẩm theo mã sản phẩm trong một service
     */
    @Query("SELECT sp FROM ServiceProduct sp WHERE " +
           "sp.service.serviceId = :serviceId AND " +
           "LOWER(sp.product.sku) LIKE LOWER(CONCAT('%', :productSku, '%')) AND " +
           "sp.isDeleted = false " +
           "ORDER BY sp.sortOrder ASC")
    Page<ServiceProduct> findByServiceIdAndProductSkuContainingIgnoreCase(@Param("serviceId") UUID serviceId, 
                                                                          @Param("productSku") String productSku, 
                                                                          Pageable pageable);
    
    /**
     * Tìm sản phẩm theo đơn vị tính
     */
    List<ServiceProduct> findByUnitAndServiceServiceIdAndIsDeletedFalse(String unit, UUID serviceId);
    
    /**
     * Tìm sản phẩm theo số lượng
     */
    @Query("SELECT sp FROM ServiceProduct sp WHERE " +
           "sp.service.serviceId = :serviceId AND " +
           "sp.quantity BETWEEN :minQuantity AND :maxQuantity AND " +
           "sp.isDeleted = false " +
           "ORDER BY sp.sortOrder ASC")
    List<ServiceProduct> findByServiceIdAndQuantityBetween(@Param("serviceId") UUID serviceId, 
                                                           @Param("minQuantity") Double minQuantity, 
                                                           @Param("maxQuantity") Double maxQuantity);
    
    /**
     * Tìm sản phẩm theo thương hiệu trong một service
     */
    @Query("SELECT sp FROM ServiceProduct sp WHERE " +
           "sp.service.serviceId = :serviceId AND " +
           "LOWER(sp.product.brand) LIKE LOWER(CONCAT('%', :brand, '%')) AND " +
           "sp.isDeleted = false " +
           "ORDER BY sp.sortOrder ASC")
    List<ServiceProduct> findByServiceIdAndProductBrandContainingIgnoreCase(@Param("serviceId") UUID serviceId, 
                                                                            @Param("brand") String brand);
    
    /**
     * Tìm tất cả sản phẩm được sử dụng trong các service
     */
    @Query("SELECT DISTINCT sp.product FROM ServiceProduct sp WHERE sp.isDeleted = false")
    List<Object> findDistinctProductsUsedInServices();
    
    /**
     * Tìm sản phẩm được sử dụng nhiều nhất
     */
    @Query("SELECT sp.product.productId, sp.product.productName, COUNT(sp) as usageCount " +
           "FROM ServiceProduct sp WHERE sp.isDeleted = false " +
           "GROUP BY sp.product.productId, sp.product.productName " +
           "ORDER BY usageCount DESC")
    List<Object[]> findMostUsedProducts();
    
    /**
     * Đếm số sản phẩm của một service
     */
    long countByServiceServiceIdAndIsDeletedFalse(UUID serviceId);
    
    /**
     * Tính tổng số lượng sản phẩm của một service
     */
    @Query("SELECT COALESCE(SUM(sp.quantity), 0) FROM ServiceProduct sp WHERE " +
           "sp.service.serviceId = :serviceId AND sp.isDeleted = false")
    Double sumQuantityByServiceId(@Param("serviceId") UUID serviceId);
    
    /**
     * Kiểm tra sản phẩm đã tồn tại trong service chưa (trừ id hiện tại)
     */
    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END FROM ServiceProduct sp WHERE " +
           "sp.service.serviceId = :serviceId AND " +
           "sp.product.productId = :productId AND " +
           "sp.id != :excludeId AND " +
           "sp.isDeleted = false")
    boolean existsByServiceIdAndProductIdAndIdNot(@Param("serviceId") UUID serviceId, 
                                                  @Param("productId") UUID productId, 
                                                  @Param("excludeId") UUID excludeId);
    
    /**
     * Tìm service product với product được load
     */
    @Query("SELECT sp FROM ServiceProduct sp " +
           "LEFT JOIN FETCH sp.product p " +
           "WHERE sp.service.serviceId = :serviceId AND sp.isDeleted = false " +
           "ORDER BY sp.sortOrder ASC")
    List<ServiceProduct> findByServiceIdWithProduct(@Param("serviceId") UUID serviceId);
}
