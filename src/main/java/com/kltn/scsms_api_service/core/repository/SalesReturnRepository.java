package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.SalesReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SalesReturnRepository extends JpaRepository<SalesReturn, UUID> {
    @Query("SELECT DISTINCT sr FROM SalesReturn sr " +
        "LEFT JOIN FETCH sr.lines l " +
        "LEFT JOIN FETCH l.product p " +
        "LEFT JOIN FETCH sr.salesOrder so " +
        "LEFT JOIN FETCH so.customer c " +
        "LEFT JOIN FETCH sr.branch b " +
        "WHERE sr.createdDate >= :fromDate " +
        "AND sr.createdDate <= :toDate " +
        "ORDER BY sr.createdDate DESC")
    List<SalesReturn> findByDateRange(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate);
}
