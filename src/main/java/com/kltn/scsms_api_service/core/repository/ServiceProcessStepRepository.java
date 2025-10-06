package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServiceProcessStep;
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
public interface ServiceProcessStepRepository extends JpaRepository<ServiceProcessStep, UUID> {
    
    /**
     * Tìm tất cả bước của một service process
     */
    List<ServiceProcessStep> findByServiceProcessIdAndIsDeletedFalseOrderByStepOrderAsc(UUID processId);
    
    /**
     * Tìm bước theo thứ tự trong một service process
     */
    Optional<ServiceProcessStep> findByServiceProcessIdAndStepOrderAndIsDeletedFalse(UUID processId, Integer stepOrder);
    
    /**
     * Tìm bước đầu tiên của một service process
     */
    @Query("SELECT sps FROM ServiceProcessStep sps WHERE " +
           "sps.serviceProcess.id = :processId AND " +
           "sps.stepOrder = (SELECT MIN(sps2.stepOrder) FROM ServiceProcessStep sps2 WHERE sps2.serviceProcess.id = :processId AND sps2.isDeleted = false) AND " +
           "sps.isDeleted = false")
    Optional<ServiceProcessStep> findFirstStepByProcessId(@Param("processId") UUID processId);
    
    /**
     * Tìm bước cuối cùng của một service process
     */
    @Query("SELECT sps FROM ServiceProcessStep sps WHERE " +
           "sps.serviceProcess.id = :processId AND " +
           "sps.stepOrder = (SELECT MAX(sps2.stepOrder) FROM ServiceProcessStep sps2 WHERE sps2.serviceProcess.id = :processId AND sps2.isDeleted = false) AND " +
           "sps.isDeleted = false")
    Optional<ServiceProcessStep> findLastStepByProcessId(@Param("processId") UUID processId);
    
    /**
     * Tìm bước tiếp theo của một bước
     */
    @Query("SELECT sps FROM ServiceProcessStep sps WHERE " +
           "sps.serviceProcess.id = :processId AND " +
           "sps.stepOrder = :nextStepOrder AND " +
           "sps.isDeleted = false")
    Optional<ServiceProcessStep> findNextStep(@Param("processId") UUID processId, @Param("nextStepOrder") Integer nextStepOrder);
    
    /**
     * Tìm bước trước đó của một bước
     */
    @Query("SELECT sps FROM ServiceProcessStep sps WHERE " +
           "sps.serviceProcess.id = :processId AND " +
           "sps.stepOrder = :previousStepOrder AND " +
           "sps.isDeleted = false")
    Optional<ServiceProcessStep> findPreviousStep(@Param("processId") UUID processId, @Param("previousStepOrder") Integer previousStepOrder);
    
    /**
     * Tìm bước theo tên trong một service process
     */
    @Query("SELECT sps FROM ServiceProcessStep sps WHERE " +
           "sps.serviceProcess.id = :processId AND " +
           "LOWER(sps.name) LIKE LOWER(CONCAT('%', :name, '%')) AND " +
           "sps.isDeleted = false")
    Page<ServiceProcessStep> findByNameContainingIgnoreCaseAndProcessId(@Param("processId") UUID processId, 
                                                                        @Param("name") String name, 
                                                                        Pageable pageable);
    
    /**
     * Tìm bước bắt buộc của một service process
     */
    @Query("SELECT sps FROM ServiceProcessStep sps WHERE " +
           "sps.serviceProcess.id = :processId AND " +
           "sps.isRequired = true AND " +
           "sps.isDeleted = false " +
           "ORDER BY sps.stepOrder ASC")
    List<ServiceProcessStep> findRequiredStepsByProcessId(@Param("processId") UUID processId);
    
    /**
     * Tìm bước không bắt buộc của một service process
     */
    @Query("SELECT sps FROM ServiceProcessStep sps WHERE " +
           "sps.serviceProcess.id = :processId AND " +
           "sps.isRequired = false AND " +
           "sps.isDeleted = false " +
           "ORDER BY sps.stepOrder ASC")
    List<ServiceProcessStep> findOptionalStepsByProcessId(@Param("processId") UUID processId);
    
    /**
     * Đếm số bước của một service process
     */
    @Query("SELECT COUNT(sps) FROM ServiceProcessStep sps WHERE " +
           "sps.serviceProcess.id = :processId AND " +
           "sps.isDeleted = false")
    long countStepsByProcessId(@Param("processId") UUID processId);
    
    /**
     * Tìm bước có sản phẩm
     */
    @Query("SELECT sps FROM ServiceProcessStep sps WHERE " +
           "sps.serviceProcess.id = :processId AND " +
           "sps.isDeleted = false AND " +
           "EXISTS (SELECT 1 FROM ServiceProcessStepProduct spsp WHERE spsp.serviceProcessStep = sps AND spsp.isDeleted = false)")
    List<ServiceProcessStep> findStepsWithProductsByProcessId(@Param("processId") UUID processId);
    
    /**
     * Tìm bước không có sản phẩm
     */
    @Query("SELECT sps FROM ServiceProcessStep sps WHERE " +
           "sps.serviceProcess.id = :processId AND " +
           "sps.isDeleted = false AND " +
           "NOT EXISTS (SELECT 1 FROM ServiceProcessStepProduct spsp WHERE spsp.serviceProcessStep = sps AND spsp.isDeleted = false)")
    List<ServiceProcessStep> findStepsWithoutProductsByProcessId(@Param("processId") UUID processId);
    
    /**
     * Kiểm tra step order đã tồn tại trong process chưa (trừ id hiện tại)
     */
    @Query("SELECT COUNT(sps) > 0 FROM ServiceProcessStep sps WHERE " +
           "sps.serviceProcess.id = :processId AND " +
           "sps.stepOrder = :stepOrder AND " +
           "sps.id != :id AND " +
           "sps.isDeleted = false")
    boolean existsByProcessIdAndStepOrderAndIdNot(@Param("processId") UUID processId, 
                                                  @Param("stepOrder") Integer stepOrder, 
                                                  @Param("id") UUID id);
    
    /**
     * Tìm bước theo thời gian dự kiến
     */
    @Query("SELECT sps FROM ServiceProcessStep sps WHERE " +
           "sps.serviceProcess.id = :processId AND " +
           "sps.estimatedTime BETWEEN :minTime AND :maxTime AND " +
           "sps.isDeleted = false " +
           "ORDER BY sps.stepOrder ASC")
    List<ServiceProcessStep> findByEstimatedTimeBetweenAndProcessId(@Param("processId") UUID processId,
                                                                    @Param("minTime") Integer minTime,
                                                                    @Param("maxTime") Integer maxTime);
}
