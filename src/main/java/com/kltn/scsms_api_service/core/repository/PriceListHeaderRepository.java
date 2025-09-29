package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.PriceListHeader;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CustomerRank;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PriceListScope;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PriceListStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PriceListHeaderRepository extends JpaRepository<PriceListHeader, UUID> {

    Optional<PriceListHeader> findByPriceListCode(String priceListCode);
    
    @Query("SELECT p FROM PriceListHeader p WHERE " +
           "(:priceListCode IS NULL OR p.priceListCode LIKE %:priceListCode%) AND " +
           "(:priceListName IS NULL OR p.priceListName LIKE %:priceListName%) AND " +
           "(:scope IS NULL OR p.scope = :scope) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:currency IS NULL OR p.currency = :currency) AND " +
           "(:effectiveDate IS NULL OR p.effectiveDate <= :effectiveDate) AND " +
           "(:expirationDate IS NULL OR p.expirationDate >= :expirationDate OR p.expirationDate IS NULL) AND " +
           "(:isActive IS NULL OR (:isActive = true AND p.status = 'ACTIVE' AND " +
           "(p.effectiveDate <= CURRENT_DATE AND (p.expirationDate >= CURRENT_DATE OR p.expirationDate IS NULL))))")
    Page<PriceListHeader> findPriceListHeadersByFilters(
            @Param("priceListCode") String priceListCode,
            @Param("priceListName") String priceListName,
            @Param("scope") PriceListScope scope,
            @Param("status") PriceListStatus status,
            @Param("currency") String currency,
            @Param("effectiveDate") LocalDate effectiveDate,
            @Param("expirationDate") LocalDate expirationDate,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
    
    @Query("SELECT p FROM PriceListHeader p " +
           "JOIN p.centers c WHERE c.centerId = :centerId AND " +
           "p.status = 'ACTIVE' AND p.effectiveDate <= CURRENT_DATE AND " +
           "(p.expirationDate >= CURRENT_DATE OR p.expirationDate IS NULL)")
    List<PriceListHeader> findActiveByCenter(@Param("centerId") UUID centerId);
    
    @Query("SELECT p FROM PriceListHeader p " +
           "JOIN p.branches b WHERE b.branchId = :branchId AND " +
           "p.status = 'ACTIVE' AND p.effectiveDate <= CURRENT_DATE AND " +
           "(p.expirationDate >= CURRENT_DATE OR p.expirationDate IS NULL)")
    List<PriceListHeader> findActiveByBranch(@Param("branchId") UUID branchId);
    
    @Query("SELECT p FROM PriceListHeader p " +
           "JOIN p.branches b " +
           "JOIN p.customerRanks cr " +
           "WHERE b.branchId = :branchId AND " +
           "cr = :customerRank AND " +
           "p.status = 'ACTIVE' AND " +
           "p.effectiveDate <= CURRENT_DATE AND " +
           "(p.expirationDate >= CURRENT_DATE OR p.expirationDate IS NULL)")
    List<PriceListHeader> findActiveByBranchAndCustomerRank(
            @Param("branchId") UUID branchId,
            @Param("customerRank") CustomerRank customerRank);
}
