package com.kltn.scsms_api_service.core.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for recent activity entries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDto {
  private String type; // BOOKING, PAYMENT, SERVICE, etc.
  private String description;
  private String status;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime timestamp;
}
