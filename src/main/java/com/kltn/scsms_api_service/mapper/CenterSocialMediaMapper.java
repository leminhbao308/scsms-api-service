package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.centerSocialMedia.CenterSocialMediaDto;
import com.kltn.scsms_api_service.core.dto.centerSocialMedia.request.CreateCenterSocialMediaRequest;
import com.kltn.scsms_api_service.core.dto.centerSocialMedia.request.UpdateCenterSocialMediaRequest;
import com.kltn.scsms_api_service.core.entity.CenterSocialMedia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {AuditMapper.class}, unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface CenterSocialMediaMapper {
    
    @Mapping(target = "centerId", source = "center.centerId")
    CenterSocialMediaDto toDto(CenterSocialMedia entity);
    
    @Mapping(target = "socialMediaId", ignore = true)
    @Mapping(target = "center", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    CenterSocialMedia toEntity(CreateCenterSocialMediaRequest request);
    
    @Mapping(target = "socialMediaId", ignore = true)
    @Mapping(target = "center", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateCenterSocialMediaRequest request, @MappingTarget CenterSocialMedia entity);
}
