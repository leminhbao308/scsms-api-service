package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServiceProcessStepProduct;
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
public interface ServiceProcessStepProductRepository extends JpaRepository<ServiceProcessStepProduct, UUID> {
    
    /**
     * Tìm tất cả sản phẩm của một bước
     */
    List<ServiceProcessStepProduct> findByServiceProcessStepIdAndIsDeletedFalseOrderByCreatedDateAsc(UUID stepId);
    
    /**
     * Tìm sản phẩm theo bước và sản phẩm
     */
    @Query("SELECT spsp FROM ServiceProcessStepProduct spsp WHERE " +
           "spsp.serviceProcessStep.id = :stepId AND " +
           "spsp.product.productId = :productId AND " +
           "spsp.isDeleted = false")
    Optional<ServiceProcessStepProduct> findByServiceProcessStepIdAndProductIdAndIsDeletedFalse(@Param("stepId") UUID stepId, 
                                                                                                @Param("productId") UUID productId);
    
    /**
     * Tìm tất cả sản phẩm của một service process
     */
    @Query("SELECT spsp FROM ServiceProcessStepProduct spsp WHERE " +
           "spsp.serviceProcessStep.serviceProcess.id = :processId AND " +
           "spsp.isDeleted = false " +
           "ORDER BY spsp.serviceProcessStep.stepOrder ASC, spsp.createdDate ASC")
    List<ServiceProcessStepProduct> findByServiceProcessId(@Param("processId") UUID processId);
    
    /**
     * Tìm sản phẩm theo tên sản phẩm trong một bước
     */
    @Query("SELECT spsp FROM ServiceProcessStepProduct spsp WHERE " +
           "spsp.serviceProcessStep.id = :stepId AND " +
           "LOWER(spsp.product.productName) LIKE LOWER(CONCAT('%', :productName, '%')) AND " +
           "spsp.isDeleted = false")
    Page<ServiceProcessStepProduct> findByProductNameContainingIgnoreCaseAndStepId(@Param("stepId") UUID stepId, 
                                                                                   @Param("productName") String productName, 
                                                                                   Pageable pageable);
    
    /**
     * Tìm sản phẩm theo mã sản phẩm trong một bước
     */
    @Query("SELECT spsp FROM ServiceProcessStepProduct spsp WHERE " +
           "spsp.serviceProcessStep.id = :stepId AND " +
           "LOWER(spsp.product.sku) LIKE LOWER(CONCAT('%', :productSku, '%')) AND " +
           "spsp.isDeleted = false")
    Page<ServiceProcessStepProduct> findByProductSkuContainingIgnoreCaseAndStepId(@Param("stepId") UUID stepId, 
                                                                                  @Param("productSku") String productSku, 
                                                                                  Pageable pageable);
    
    /**
     * Tìm sản phẩm theo đơn vị tính
     */
    @Query("SELECT spsp FROM ServiceProcessStepProduct spsp WHERE " +
           "spsp.serviceProcessStep.id = :stepId AND " +
           "spsp.unit = :unit AND " +
           "spsp.isDeleted = false")
    List<ServiceProcessStepProduct> findByUnitAndStepId(@Param("stepId") UUID stepId, @Param("unit") String unit);
    
    /**
     * Tìm sản phẩm theo số lượng
     */
    @Query("SELECT spsp FROM ServiceProcessStepProduct spsp WHERE " +
           "spsp.serviceProcessStep.id = :stepId AND " +
           "spsp.quantity BETWEEN :minQuantity AND :maxQuantity AND " +
           "spsp.isDeleted = false")
    List<ServiceProcessStepProduct> findByQuantityBetweenAndStepId(@Param("stepId") UUID stepId,
                                                                  @Param("minQuantity") Double minQuantity,
                                                                  @Param("maxQuantity") Double maxQuantity);
    
    /**
     * Đếm số sản phẩm của một bước
     */
    @Query("SELECT COUNT(spsp) FROM ServiceProcessStepProduct spsp WHERE " +
           "spsp.serviceProcessStep.id = :stepId AND " +
           "spsp.isDeleted = false")
    long countProductsByStepId(@Param("stepId") UUID stepId);
    
    /**
     * Đếm số sản phẩm của một service process
     */
    @Query("SELECT COUNT(spsp) FROM ServiceProcessStepProduct spsp WHERE " +
           "spsp.serviceProcessStep.serviceProcess.id = :processId AND " +
           "spsp.isDeleted = false")
    long countProductsByProcessId(@Param("processId") UUID processId);
    
    /**
     * Tìm tất cả sản phẩm được sử dụng trong các bước
     */
    @Query("SELECT DISTINCT spsp.product FROM ServiceProcessStepProduct spsp WHERE " +
           "spsp.isDeleted = false " +
           "ORDER BY spsp.product.productName ASC")
    List<Object> findDistinctProductsUsedInSteps();
    
    /**
     * Tìm sản phẩm được sử dụng nhiều nhất
     */
    @Query("SELECT spsp.product, COUNT(spsp) as usageCount FROM ServiceProcessStepProduct spsp WHERE " +
           "spsp.isDeleted = false " +
           "GROUP BY spsp.product " +
           "ORDER BY usageCount DESC")
    List<Object[]> findMostUsedProducts();
    
    /**
     * Tìm sản phẩm theo thương hiệu trong một bước
     */
    @Query("SELECT spsp FROM ServiceProcessStepProduct spsp WHERE " +
           "spsp.serviceProcessStep.id = :stepId AND " +
           "LOWER(spsp.product.brand) LIKE LOWER(CONCAT('%', :brand, '%')) AND " +
           "spsp.isDeleted = false")
    List<ServiceProcessStepProduct> findByProductBrandContainingIgnoreCaseAndStepId(@Param("stepId") UUID stepId, 
                                                                                    @Param("brand") String brand);
    
    /**
     * Kiểm tra sản phẩm đã tồn tại trong bước chưa (trừ id hiện tại)
     */
    @Query("SELECT COUNT(spsp) > 0 FROM ServiceProcessStepProduct spsp WHERE " +
           "spsp.serviceProcessStep.id = :stepId AND " +
           "spsp.product.productId = :productId AND " +
           "spsp.id != :id AND " +
           "spsp.isDeleted = false")
    boolean existsByStepIdAndProductIdAndIdNot(@Param("stepId") UUID stepId, 
                                               @Param("productId") UUID productId, 
                                               @Param("id") UUID id);
    
    /**
     * Tính tổng số lượng sản phẩm của một bước
     */
    @Query("SELECT SUM(spsp.quantity) FROM ServiceProcessStepProduct spsp WHERE " +
           "spsp.serviceProcessStep.id = :stepId AND " +
           "spsp.isDeleted = false")
    Double sumQuantityByStepId(@Param("stepId") UUID stepId);
    
    /**
     * Tính tổng số lượng sản phẩm của một service process
     */
    @Query("SELECT SUM(spsp.quantity) FROM ServiceProcessStepProduct spsp WHERE " +
           "spsp.serviceProcessStep.serviceProcess.id = :processId AND " +
           "spsp.isDeleted = false")
    Double sumQuantityByProcessId(@Param("processId") UUID processId);
    
    /**
     * Tìm tất cả stepProducts với product được load cho một service process
     */
    @Query("SELECT spsp FROM ServiceProcessStepProduct spsp " +
           "LEFT JOIN FETCH spsp.product p " +
           "WHERE spsp.serviceProcessStep.serviceProcess.id = :processId AND " +
           "spsp.isDeleted = false " +
           "ORDER BY spsp.serviceProcessStep.stepOrder ASC, spsp.createdDate ASC")
    List<ServiceProcessStepProduct> findByProcessIdWithProduct(@Param("processId") UUID processId);
}
