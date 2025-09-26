package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Center;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CenterRepository extends JpaRepository<Center, UUID> {
    
    Optional<Center> findByCenterName(String centerName);
    
    Optional<Center> findByCenterCode(String centerCode);
    
    Optional<Center> findByTaxCode(String taxCode);
    
    Optional<Center> findByBusinessLicense(String businessLicense);
    
    boolean existsByCenterName(String centerName);
    
    boolean existsByCenterCode(String centerCode);
    
    boolean existsByTaxCode(String taxCode);
    
    boolean existsByBusinessLicense(String businessLicense);
    
    @Query("SELECT c FROM Center c WHERE c.centerName LIKE %:keyword% OR c.centerCode LIKE %:keyword% OR c.description LIKE %:keyword%")
    List<Center> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT c FROM Center c WHERE c.isActive = :isActive")
    List<Center> findByIsActive(@Param("isActive") Boolean isActive);
    
    @Query("SELECT c FROM Center c WHERE c.operatingStatus = :operatingStatus")
    List<Center> findByOperatingStatus(@Param("operatingStatus") Center.OperatingStatus operatingStatus);
    
    @Query("SELECT c FROM Center c WHERE c.manager.userId = :managerId")
    List<Center> findByManagerId(@Param("managerId") UUID managerId);
    
    @Query("SELECT c FROM Center c WHERE c.totalBranches >= :minBranches")
    List<Center> findByMinBranches(@Param("minBranches") Integer minBranches);
    
    @Query("SELECT c FROM Center c WHERE c.totalEmployees >= :minEmployees")
    List<Center> findByMinEmployees(@Param("minEmployees") Integer minEmployees);
    
    @Query("SELECT c FROM Center c WHERE c.establishedDate >= :fromDate AND c.establishedDate <= :toDate")
    List<Center> findByEstablishedDateRange(@Param("fromDate") java.time.LocalDate fromDate, 
                                           @Param("toDate") java.time.LocalDate toDate);
    
    @Query("SELECT c FROM Center c LEFT JOIN FETCH c.branches WHERE c.centerId = :centerId")
    Optional<Center> findByIdWithBranches(@Param("centerId") UUID centerId);
    
    @Query("SELECT c FROM Center c LEFT JOIN FETCH c.manager WHERE c.centerId = :centerId")
    Optional<Center> findByIdWithManager(@Param("centerId") UUID centerId);
    
    @Query("SELECT c FROM Center c LEFT JOIN FETCH c.branches LEFT JOIN FETCH c.manager WHERE c.centerId = :centerId")
    Optional<Center> findByIdWithBranchesAndManager(@Param("centerId") UUID centerId);
    
    @Query("SELECT COUNT(c) FROM Center c WHERE c.isActive = true")
    Long countActiveCenters();
    
    @Query("SELECT SUM(c.totalBranches) FROM Center c WHERE c.isActive = true")
    Long sumTotalBranches();
    
    @Query("SELECT SUM(c.totalEmployees) FROM Center c WHERE c.isActive = true")
    Long sumTotalEmployees();
    
    @Query("SELECT SUM(c.totalCustomers) FROM Center c WHERE c.isActive = true")
    Long sumTotalCustomers();
}
