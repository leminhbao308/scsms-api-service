package com.kltn.scsms_api_service.core.dto.serviceBayManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO chứa thống kê của service bay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBayStatisticsDto {
    
    private UUID bayId;
    private String bayName;
    private String bayCode;
    private String bayType;
    private String status;
    private Long totalBookings;
    private Long completedBookings;
    private Long cancelledBookings;
    private Long activeBookings;
    private Double utilizationRate;
    private Long averageServiceTimeMinutes;
    private List<BayTypeStatisticsDto> bayTypeStatistics;
    private List<BayStatusStatisticsDto> bayStatusStatistics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BayTypeStatisticsDto {
        private String bayType;
        private Long count;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BayStatusStatisticsDto {
        private String status;
        private Long count;
    }
}
