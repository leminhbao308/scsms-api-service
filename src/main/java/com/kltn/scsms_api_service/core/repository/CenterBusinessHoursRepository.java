package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.CenterBusinessHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CenterBusinessHoursRepository extends JpaRepository<CenterBusinessHours, UUID> {
    
    List<CenterBusinessHours> findByCenter_CenterIdAndIsDeletedFalse(UUID centerId);
    
    List<CenterBusinessHours> findByCenter_CenterIdAndIsClosedFalseAndIsDeletedFalse(UUID centerId);
    
    @Query("SELECT cbh FROM CenterBusinessHours cbh WHERE cbh.center.centerId = :centerId AND cbh.dayOfWeek = :dayOfWeek AND cbh.isDeleted = false")
    CenterBusinessHours findByCenterAndDayOfWeek(@Param("centerId") UUID centerId, @Param("dayOfWeek") String dayOfWeek);
    
    @Query("SELECT cbh FROM CenterBusinessHours cbh WHERE cbh.center.centerId = :centerId AND cbh.isDeleted = false ORDER BY " +
           "CASE cbh.dayOfWeek " +
           "WHEN 'MONDAY' THEN 1 " +
           "WHEN 'TUESDAY' THEN 2 " +
           "WHEN 'WEDNESDAY' THEN 3 " +
           "WHEN 'THURSDAY' THEN 4 " +
           "WHEN 'FRIDAY' THEN 5 " +
           "WHEN 'SATURDAY' THEN 6 " +
           "WHEN 'SUNDAY' THEN 7 " +
           "END")
    List<CenterBusinessHours> findByCenterCenterIdOrderByDayOfWeek(@Param("centerId") UUID centerId);
}
