package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {
    
    Optional<Branch> findByBranchName(String branchName);
    
    Optional<Branch> findByBranchCode(String branchCode);
    
    boolean existsByBranchName(String branchName);
    
    boolean existsByBranchCode(String branchCode);
    
    @Query("SELECT b FROM Branch b WHERE b.branchName LIKE %:keyword% OR b.branchCode LIKE %:keyword% OR b.description LIKE %:keyword%")
    List<Branch> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT b FROM Branch b WHERE b.isActive = :isActive")
    List<Branch> findByIsActive(@Param("isActive") Boolean isActive);
    
    @Query("SELECT b FROM Branch b WHERE b.operatingStatus = :operatingStatus")
    List<Branch> findByOperatingStatus(@Param("operatingStatus") Branch.OperatingStatus operatingStatus);
    
    
    @Query("SELECT b FROM Branch b WHERE b.center.centerId = :centerId")
    List<Branch> findByCenterId(@Param("centerId") UUID centerId);
    
    @Query("SELECT b FROM Branch b WHERE b.manager.userId = :managerId")
    List<Branch> findByManagerId(@Param("managerId") UUID managerId);
    
    @Query("SELECT b FROM Branch b WHERE b.serviceSlots >= :minSlots")
    List<Branch> findByMinServiceSlots(@Param("minSlots") Integer minSlots);
    
    
    @Query("SELECT b FROM Branch b WHERE b.establishedDate >= :fromDate AND b.establishedDate <= :toDate")
    List<Branch> findByEstablishedDateRange(@Param("fromDate") java.time.LocalDate fromDate, 
                                           @Param("toDate") java.time.LocalDate toDate);
    
    
    @Query("SELECT b FROM Branch b LEFT JOIN FETCH b.center WHERE b.branchId = :branchId")
    Optional<Branch> findByIdWithCenter(@Param("branchId") UUID branchId);
    
    @Query("SELECT b FROM Branch b LEFT JOIN FETCH b.manager WHERE b.branchId = :branchId")
    Optional<Branch> findByIdWithManager(@Param("branchId") UUID branchId);
    
    @Query("SELECT b FROM Branch b LEFT JOIN FETCH b.center LEFT JOIN FETCH b.manager WHERE b.branchId = :branchId")
    Optional<Branch> findByIdWithCenterAndManager(@Param("branchId") UUID branchId);
    
    @Query("SELECT COUNT(b) FROM Branch b WHERE b.isActive = true")
    Long countActiveBranches();
    
    @Query("SELECT COUNT(b) FROM Branch b WHERE b.center.centerId = :centerId AND b.isActive = true")
    Long countActiveBranchesByCenter(@Param("centerId") UUID centerId);
    
    @Query("SELECT SUM(b.serviceSlots) FROM Branch b WHERE b.isActive = true")
    Long sumTotalServiceSlots();
    
    
    @Query("SELECT b FROM Branch b WHERE b.center.centerId = :centerId ORDER BY b.branchName")
    List<Branch> findByCenterIdOrderByBranchName(@Param("centerId") UUID centerId);
    
    /**
     * Tìm tất cả chi nhánh active
     */
    List<Branch> findByIsActiveTrueAndIsDeletedFalse();
    
}