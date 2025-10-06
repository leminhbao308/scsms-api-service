package com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request;

import com.kltn.scsms_api_service.core.entity.ServiceProcessTracking;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO để cập nhật tracking quá trình thực hiện dịch vụ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceProcessTrackingRequest {
    
    @Positive(message = "Thời gian ước lượng phải lớn hơn 0")
    private Integer estimatedDuration;
    
    private ServiceProcessTracking.TrackingStatus status;
    
    private BigDecimal progressPercent;
    
    private String notes;
    
    private String evidenceMediaUrls;
}
