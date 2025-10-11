package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.promotionManagement.PromotionInfoDto;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.CreatePromotionLineRequest;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.UpdatePromotionLineRequest;
import com.kltn.scsms_api_service.core.entity.PromotionLine;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapper.class})
public interface PromotionLineMapper {
    
    /**
     * Convert CreatePromotionLineRequest to PromotionLine entity
     */
    @Mapping(target = "promotionLineId", ignore = true)
    @Mapping(target = "promotion", ignore = true) // Will be set manually
    @Mapping(target = "branch", ignore = true) // Will be set manually
    @Mapping(target = "freeProduct", ignore = true) // Will be set manually
    PromotionLine toEntity(CreatePromotionLineRequest request);
    
    /**
     * Update PromotionLine entity from UpdatePromotionLineRequest
     */
    @Mapping(target = "promotionLineId", ignore = true)
    @Mapping(target = "promotion", ignore = true) // Will be set manually
    @Mapping(target = "branch", ignore = true) // Will be set manually
    @Mapping(target = "freeProduct", ignore = true) // Will be set manually
    void updateEntityFromRequest(UpdatePromotionLineRequest request, @MappingTarget PromotionLine promotionLine);
    
    /**
     * Convert PromotionLine entity to PromotionLineDto
     */
    @Mapping(source = "branch", target = "branch")
    @Mapping(source = "freeProduct", target = "freeProduct")
    PromotionInfoDto.PromotionLineDto toPromotionLineDto(PromotionLine promotionLine);
}
