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
    
    @Query("SELECT b FROM Branch b WHERE b.branchType = :branchType")
    List<Branch> findByBranchType(@Param("branchType") Branch.BranchType branchType);
    
    @Query("SELECT b FROM Branch b WHERE b.center.centerId = :centerId")
    List<Branch> findByCenterId(@Param("centerId") UUID centerId);
    
    @Query("SELECT b FROM Branch b WHERE b.manager.userId = :managerId")
    List<Branch> findByManagerId(@Param("managerId") UUID managerId);
    
    @Query("SELECT b FROM Branch b WHERE b.serviceCapacity >= :minCapacity")
    List<Branch> findByMinServiceCapacity(@Param("minCapacity") Integer minCapacity);
    
    @Query("SELECT b FROM Branch b WHERE b.currentWorkload < b.serviceCapacity")
    List<Branch> findAvailableBranches();
    
    @Query("SELECT b FROM Branch b WHERE b.currentWorkload >= b.serviceCapacity")
    List<Branch> findAtCapacityBranches();
    
    @Query("SELECT b FROM Branch b WHERE b.establishedDate >= :fromDate AND b.establishedDate <= :toDate")
    List<Branch> findByEstablishedDateRange(@Param("fromDate") java.time.LocalDate fromDate, 
                                           @Param("toDate") java.time.LocalDate toDate);
    
    @Query("SELECT b FROM Branch b WHERE b.latitude IS NOT NULL AND b.longitude IS NOT NULL")
    List<Branch> findBranchesWithLocation();
    
    @Query("SELECT b FROM Branch b WHERE " +
           "6371 * acos(cos(radians(:latitude)) * cos(radians(b.latitude)) * " +
           "cos(radians(b.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(b.latitude))) <= :radiusKm")
    List<Branch> findBranchesWithinRadius(@Param("latitude") Double latitude, 
                                         @Param("longitude") Double longitude, 
                                         @Param("radiusKm") Double radiusKm);
    
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
    
    @Query("SELECT SUM(b.serviceCapacity) FROM Branch b WHERE b.isActive = true")
    Long sumTotalServiceCapacity();
    
    @Query("SELECT SUM(b.currentWorkload) FROM Branch b WHERE b.isActive = true")
    Long sumTotalCurrentWorkload();
    
    @Query("SELECT SUM(b.totalEmployees) FROM Branch b WHERE b.isActive = true")
    Long sumTotalEmployees();
    
    @Query("SELECT SUM(b.totalCustomers) FROM Branch b WHERE b.isActive = true")
    Long sumTotalCustomers();
    
    @Query("SELECT SUM(b.monthlyRevenue) FROM Branch b WHERE b.isActive = true")
    Double sumTotalMonthlyRevenue();
    
    @Query("SELECT b FROM Branch b WHERE b.center.centerId = :centerId ORDER BY b.branchName")
    List<Branch> findByCenterIdOrderByBranchName(@Param("centerId") UUID centerId);
    
    @Query("SELECT b FROM Branch b WHERE b.operatingStatus = 'ACTIVE' AND b.currentWorkload < b.serviceCapacity ORDER BY b.currentWorkload ASC")
    List<Branch> findAvailableBranchesOrderByWorkload();
}