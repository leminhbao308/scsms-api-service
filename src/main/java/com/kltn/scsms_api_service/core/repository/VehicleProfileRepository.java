package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.VehicleProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface VehicleProfileRepository extends JpaRepository<VehicleProfile, UUID> {

  /**
   * Count vehicle profiles created after date
   */
  @Query("SELECT COUNT(v) FROM VehicleProfile v WHERE v.createdDate >= :date")
  Long countCreatedAfter(@Param("date") LocalDateTime date);

  /**
   * Count vehicle profiles created between dates
   */
  @Query("SELECT COUNT(v) FROM VehicleProfile v WHERE v.createdDate >= :start AND v.createdDate < :end")
  Long countCreatedBetween(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}
