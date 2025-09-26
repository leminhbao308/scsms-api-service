package com.kltn.scsms_api_service.core.dto.branchManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperatingHoursDto {
    
    @JsonProperty("monday")
    private DaySchedule monday;
    
    @JsonProperty("tuesday")
    private DaySchedule tuesday;
    
    @JsonProperty("wednesday")
    private DaySchedule wednesday;
    
    @JsonProperty("thursday")
    private DaySchedule thursday;
    
    @JsonProperty("friday")
    private DaySchedule friday;
    
    @JsonProperty("saturday")
    private DaySchedule saturday;
    
    @JsonProperty("sunday")
    private DaySchedule sunday;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DaySchedule {
        
        @JsonProperty("is_open")
        private Boolean isOpen;
        
        @JsonProperty("open_time")
        private LocalTime openTime;
        
        @JsonProperty("close_time")
        private LocalTime closeTime;
        
        @JsonProperty("break_start")
        private LocalTime breakStart;
        
        @JsonProperty("break_end")
        private LocalTime breakEnd;
        
        @JsonProperty("notes")
        private String notes;
    }
}
