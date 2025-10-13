package com.kltn.scsms_api_service.core.dto.promotionTypeManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionTypeInfoDto extends AuditDto {
    
    private UUID promotionTypeId;
    private String typeCode;
    private String typeName;
    private String description;
}
