package com.kltn.scsms_api_service.core.dto.serviceBayManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("bay_id")
    private UUID bayId;
    
    @JsonProperty("bay_name")
    private String bayName;
    
    @JsonProperty("bay_code")
    private String bayCode;
    
    @JsonProperty("bay_type")
    private String bayType;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("total_bookings")
    private Long totalBookings;
    
    @JsonProperty("completed_bookings")
    private Long completedBookings;
    
    @JsonProperty("cancelled_bookings")
    private Long cancelledBookings;
    
    @JsonProperty("active_bookings")
    private Long activeBookings;
    
    @JsonProperty("utilization_rate")
    private Double utilizationRate;
    
    @JsonProperty("average_service_time_minutes")
    private Long averageServiceTimeMinutes;
    
    @JsonProperty("bay_type_statistics")
    private List<BayTypeStatisticsDto> bayTypeStatistics;
    
    @JsonProperty("bay_status_statistics")
    private List<BayStatusStatisticsDto> bayStatusStatistics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BayTypeStatisticsDto {
        @JsonProperty("bay_type")
        private String bayType;
        
        @JsonProperty("count")
        private Long count;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BayStatusStatisticsDto {
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("count")
        private Long count;
    }
}
