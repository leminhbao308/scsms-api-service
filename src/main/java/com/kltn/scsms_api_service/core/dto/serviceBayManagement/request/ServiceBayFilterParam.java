package com.kltn.scsms_api_service.core.dto.serviceBayManagement.request;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Filter parameters cho service bay
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ServiceBayFilterParam extends BaseFilterParam<ServiceBayFilterParam> {
    
    private UUID branchId;
    private ServiceBay.BayStatus status;
    private String keyword;
    private Boolean isActive;
}
