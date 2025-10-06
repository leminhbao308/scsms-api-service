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
    
    /**
     * Tìm service process theo thời gian dự kiến
     */
    @Query("SELECT sp FROM ServiceProcess sp WHERE " +
           "sp.estimatedDuration BETWEEN :minDuration AND :maxDuration AND " +
           "sp.isDeleted = false")
    Page<ServiceProcess> findByEstimatedDurationBetween(@Param("minDuration") Integer minDuration, 
                                                        @Param("maxDuration") Integer maxDuration, 
                                                        Pageable pageable);
    
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
     * Tìm service process được sử dụng bởi service package
     */
    @Query("SELECT sp FROM ServiceProcess sp WHERE " +
           "sp.isDeleted = false AND " +
           "EXISTS (SELECT 1 FROM ServicePackage spkg WHERE spkg.serviceProcess = sp AND spkg.isDeleted = false)")
    List<ServiceProcess> findProcessesUsedByServicePackages();
}
