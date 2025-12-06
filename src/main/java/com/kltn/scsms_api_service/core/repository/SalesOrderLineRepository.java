package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, UUID> {
    List<SalesOrderLine> findBySalesOrderId(UUID soId);
    
    /**
     * Tìm tất cả sales order lines có originalBookingId
     */
    @Query("SELECT sol FROM SalesOrderLine sol " +
        "WHERE sol.originalBookingId = :bookingId")
    List<SalesOrderLine> findByOriginalBookingId(@Param("bookingId") UUID bookingId);
}
