package com.kltn.scsms_api_service.core.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for booking status statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusStatsDto {
  private Long pending;
  private Long confirmed;
  private Long checkedIn;
  private Long inProgress;
  private Long completed;
  private Long cancelled;
  private Long noShow;
}
