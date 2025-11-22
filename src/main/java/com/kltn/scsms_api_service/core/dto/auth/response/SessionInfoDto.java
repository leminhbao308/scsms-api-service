package com.kltn.scsms_api_service.core.dto.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfoDto {
    
    @JsonProperty("device_id")
    private String deviceId;
    
    @JsonProperty("device_name")
    private String deviceName;
    
    @JsonProperty("ip_address")
    private String ipAddress;
    
    @JsonProperty("user_agent")
    private String userAgent;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("last_activity")
    private LocalDateTime lastActivity;
    
    @JsonProperty("is_current_device")
    private Boolean isCurrentDevice;
}

