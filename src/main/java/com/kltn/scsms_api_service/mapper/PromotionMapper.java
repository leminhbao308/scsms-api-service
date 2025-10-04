package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.promotionManagement.PromotionInfoDto;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.CreatePromotionRequest;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.UpdatePromotionRequest;
import com.kltn.scsms_api_service.core.entity.Promotion;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromotionMapper {
    
    /**
     * Convert CreatePromotionRequest to Promotion entity
     */
    @Mapping(target = "promotionId", ignore = true)
    @Mapping(target = "promotionType", ignore = true) // Will be set manually
    @Mapping(target = "branch", ignore = true) // Will be set manually
    @Mapping(target = "usages", ignore = true)
    @Mapping(target = "promotionLines", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Promotion toEntity(CreatePromotionRequest request);
    
    /**
     * Update Promotion entity from UpdatePromotionRequest
     */
    @Mapping(target = "promotionId", ignore = true)
    @Mapping(target = "promotionType", ignore = true) // Will be set manually
    @Mapping(target = "branch", ignore = true) // Will be set manually
    @Mapping(target = "usages", ignore = true)
    @Mapping(target = "promotionLines", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(UpdatePromotionRequest request, @MappingTarget Promotion promotion);
    
    /**
     * Convert Promotion entity to PromotionInfoDto
     */
    @Mapping(source = "promotionType", target = "promotionType")
    @Mapping(source = "branch", target = "branch")
    @Mapping(source = "promotionLines", target = "promotionLines")
    @Mapping(target = "totalUsageCount", expression = "java(promotion.getUsages() != null ? (long) promotion.getUsages().size() : 0L)")
    @Mapping(target = "isExpired", expression = "java(promotion.getEndAt() != null && promotion.getEndAt().isBefore(java.time.LocalDateTime.now()))")
    @Mapping(target = "isAvailable", expression = "java(promotion.getIsActive() && !promotion.getIsDeleted() && (promotion.getEndAt() == null || promotion.getEndAt().isAfter(java.time.LocalDateTime.now())))")
    PromotionInfoDto toPromotionInfoDto(Promotion promotion);
}