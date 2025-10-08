package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.PriceBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PriceBookRepository extends JpaRepository<PriceBook, UUID> {
    List<PriceBook> findByIsActiveAndValidFromLessThanEqualAndValidToGreaterThanEqual(
        boolean isActive, LocalDateTime from, LocalDateTime to);
    
    List<PriceBook> findByIsActiveAndValidFromLessThanEqual(boolean isActive, LocalDateTime from);
    
    List<PriceBook> findAllByIsActiveAndValidFromAfterAndValidToBefore(Boolean isActive, LocalDateTime validFromAfter, LocalDateTime validToBefore);
    
    List<PriceBook> findByIsActiveAndValidToLessThanEqual(Boolean isActive, LocalDateTime validToIsLessThan);
    
    List<PriceBook> findByIsActiveAndValidFromGreaterThanEqual(Boolean isActive, LocalDateTime validFromIsGreaterThan);
    
    List<PriceBook> findByIsActiveAndValidToGreaterThanEqual(Boolean isActive, LocalDateTime validToIsGreaterThan);
}
