package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    
    Optional<Supplier> findBySupplierName(String supplierName);
    
    Optional<Supplier> findByEmail(String email);
    
    Optional<Supplier> findByPhone(String phone);
    
    boolean existsBySupplierName(String supplierName);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    @Query("SELECT s FROM Supplier s WHERE s.supplierName LIKE %:keyword% OR s.contactPerson LIKE %:keyword% OR s.email LIKE %:keyword%")
    List<Supplier> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT s FROM Supplier s WHERE s.isActive = :isActive")
    List<Supplier> findByIsActive(@Param("isActive") Boolean isActive);
}
