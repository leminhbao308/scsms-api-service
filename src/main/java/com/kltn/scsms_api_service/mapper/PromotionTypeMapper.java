package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.PromotionTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.request.CreatePromotionTypeRequest;
import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.request.UpdatePromotionTypeRequest;
import com.kltn.scsms_api_service.core.entity.PromotionType;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapper.class})
public interface PromotionTypeMapper {
    
    /**
     * Convert entity to DTO
     */
    @Mapping(source = "promotionTypeId", target = "promotionTypeId")
    @Mapping(source = "typeCode", target = "typeCode")
    @Mapping(source = "typeName", target = "typeName")
    @Mapping(source = "description", target = "description")
    PromotionTypeInfoDto toPromotionTypeInfoDto(PromotionType promotionType);
    
    /**
     * Convert create request to entity
     */
    @Mapping(source = "typeCode", target = "typeCode")
    @Mapping(source = "typeName", target = "typeName")
    @Mapping(source = "description", target = "description")
    PromotionType toEntity(CreatePromotionTypeRequest request);
    
    /**
     * Update entity from update request
     */
    @Mapping(source = "typeCode", target = "typeCode")
    @Mapping(source = "typeName", target = "typeName")
    @Mapping(source = "description", target = "description")
    void updateEntityFromRequest(UpdatePromotionTypeRequest request, @MappingTarget PromotionType promotionType);
}
