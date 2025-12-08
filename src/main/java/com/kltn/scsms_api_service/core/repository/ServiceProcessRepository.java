package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServiceProcess;
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
public interface ServiceProcessRepository extends JpaRepository<ServiceProcess, UUID> {
    
    /**
     * Tìm service process theo code
     */
    Optional<ServiceProcess> findByCodeAndIsDeletedFalse(String code);
    
    /**
     * Tìm service process theo service ID
     */
    @Query("SELECT sp FROM ServiceProcess sp WHERE " +
           "sp.isDeleted = false AND " +
           "EXISTS (SELECT 1 FROM Service s WHERE s.serviceProcess = sp AND s.id = :serviceId AND s.isDeleted = false)")
    Optional<ServiceProcess> findByServiceIdAndIsDeletedFalse(@Param("serviceId") UUID serviceId);
    
    /**
     * Tìm service process mặc định
     */
    Optional<ServiceProcess> findByIsDefaultTrueAndIsActiveTrueAndIsDeletedFalse();
    
    /**
     * Tìm tất cả service process đang hoạt động
     */
    List<ServiceProcess> findByIsActiveTrueAndIsDeletedFalseOrderByNameAsc();
    
    /**
     * Tìm service process theo tên (tìm kiếm gần đúng)
     */
    @Query("SELECT sp FROM ServiceProcess sp WHERE " +
           "LOWER(sp.name) LIKE LOWER(CONCAT('%', :name, '%')) AND " +
           "sp.isDeleted = false")
    Page<ServiceProcess> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    /**
     * Tìm service process theo code (tìm kiếm gần đúng)
     */
    @Query("SELECT sp FROM ServiceProcess sp WHERE " +
           "LOWER(sp.code) LIKE LOWER(CONCAT('%', :code, '%')) AND " +
           "sp.isDeleted = false")
    Page<ServiceProcess> findByCodeContainingIgnoreCase(@Param("code") String code, Pageable pageable);
    
    /**
     * Tìm service process có bước
     */
    @Query("SELECT sp FROM ServiceProcess sp WHERE " +
           "sp.isDeleted = false AND " +
           "EXISTS (SELECT 1 FROM ServiceProcessStep sps WHERE sps.serviceProcess = sp AND sps.isDeleted = false)")
    Page<ServiceProcess> findProcessesWithSteps(Pageable pageable);
    
    /**
     * Tìm service process không có bước
     */
    @Query("SELECT sp FROM ServiceProcess sp WHERE " +
           "sp.isDeleted = false AND " +
           "NOT EXISTS (SELECT 1 FROM ServiceProcessStep sps WHERE sps.serviceProcess = sp AND sps.isDeleted = false)")
    Page<ServiceProcess> findProcessesWithoutSteps(Pageable pageable);
    
    // Loại bỏ method tìm theo estimatedDuration - thời gian được quản lý ở Service level
    
    /**
     * Kiểm tra code đã tồn tại chưa (trừ id hiện tại)
     */
    @Query("SELECT COUNT(sp) > 0 FROM ServiceProcess sp WHERE " +
           "sp.code = :code AND sp.id != :id AND sp.isDeleted = false")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") UUID id);
    
    /**
     * Đếm số service process đang hoạt động
     */
    @Query("SELECT COUNT(sp) FROM ServiceProcess sp WHERE " +
           "sp.isActive = true AND sp.isDeleted = false")
    long countActiveProcesses();
    
    /**
     * Tìm service process được sử dụng bởi service
     */
    @Query("SELECT sp FROM ServiceProcess sp WHERE " +
           "sp.isDeleted = false AND " +
           "EXISTS (SELECT 1 FROM Service s WHERE s.serviceProcess = sp AND s.isDeleted = false)")
    List<ServiceProcess> findProcessesUsedByServices();
    
    
    /**
     * Tìm tất cả service process với processSteps được load
     */
    @Query("SELECT DISTINCT sp FROM ServiceProcess sp " +
           "LEFT JOIN FETCH sp.processSteps sps " +
           "LEFT JOIN FETCH sps.serviceProcess " +
           "WHERE sp.isDeleted = false " +
           "ORDER BY sp.name ASC")
    List<ServiceProcess> findAllWithProcessSteps();
    
    /**
     * Tìm tất cả service process với processSteps được load (có phân trang)
     */
    @Query(value = "SELECT DISTINCT sp FROM ServiceProcess sp " +
                   "LEFT JOIN FETCH sp.processSteps sps " +
                   "LEFT JOIN FETCH sps.serviceProcess " +
                   "WHERE sp.isDeleted = false " +
                   "ORDER BY sp.name ASC",
           countQuery = "SELECT COUNT(DISTINCT sp) FROM ServiceProcess sp WHERE sp.isDeleted = false")
    Page<ServiceProcess> findAllWithProcessSteps(Pageable pageable);
    
    /**
     * Tìm service process theo ID với processSteps được load
     */
    @Query("SELECT DISTINCT sp FROM ServiceProcess sp " +
           "LEFT JOIN FETCH sp.processSteps sps " +
           "WHERE sp.id = :id AND sp.isDeleted = false")
    Optional<ServiceProcess> findByIdWithProcessSteps(@Param("id") UUID id);
}
