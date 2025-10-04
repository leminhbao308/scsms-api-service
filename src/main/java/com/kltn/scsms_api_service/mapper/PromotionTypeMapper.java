package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.PromotionTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.request.CreatePromotionTypeRequest;
import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.request.UpdatePromotionTypeRequest;
import com.kltn.scsms_api_service.core.entity.PromotionType;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromotionTypeMapper {
    
    /**
     * Convert entity to DTO
     */
    @Mapping(source = "promotionTypeId", target = "promotionTypeId")
    @Mapping(source = "typeCode", target = "typeCode")
    @Mapping(source = "typeName", target = "typeName")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "isActive", target = "isActive")
    @Mapping(source = "isDeleted", target = "isDeleted")
    @Mapping(source = "createdDate", target = "createdAt")
    @Mapping(source = "modifiedDate", target = "updatedAt")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "modifiedBy", target = "updatedBy")
    PromotionTypeInfoDto toPromotionTypeInfoDto(PromotionType promotionType);
    
    /**
     * Convert create request to entity
     */
    @Mapping(source = "typeCode", target = "typeCode")
    @Mapping(source = "typeName", target = "typeName")
    @Mapping(source = "description", target = "description")
    @Mapping(target = "promotionTypeId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    PromotionType toEntity(CreatePromotionTypeRequest request);
    
    /**
     * Update entity from update request
     */
    @Mapping(source = "typeCode", target = "typeCode")
    @Mapping(source = "typeName", target = "typeName")
    @Mapping(source = "description", target = "description")
    @Mapping(target = "promotionTypeId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(UpdatePromotionTypeRequest request, @MappingTarget PromotionType promotionType);
}
