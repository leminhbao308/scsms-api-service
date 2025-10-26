package com.kltn.scsms_api_service.core.dto.speedsms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SpeedSmsResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("data")
    private SpeedSmsData data;
    
    @JsonProperty("message")
    private String message;
    
    @Data
    public static class SpeedSmsData {
        @JsonProperty("tranId")
        private Long tranId;
        
        @JsonProperty("totalSMS")
        private Integer totalSMS;
        
        @JsonProperty("totalPrice")
        private Integer totalPrice;
        
        @JsonProperty("invalidPhone")
        private String[] invalidPhone;
    }
}
