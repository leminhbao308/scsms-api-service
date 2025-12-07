package com.kltn.scsms_api_service.core.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for upcoming booking information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingBookingDto {
  private UUID bookingId;
  private String bookingCode;
  private String customerName;
  private String vehicleInfo;
  private String service;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime scheduledTime;

  private String status;
}
