package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.mediaManagement.MediaInfoDto;
import com.kltn.scsms_api_service.core.dto.mediaManagement.param.MediaFilterParam;
import com.kltn.scsms_api_service.core.dto.mediaManagement.request.CreateMediaRequest;
import com.kltn.scsms_api_service.core.dto.mediaManagement.request.UpdateMediaRequest;
import com.kltn.scsms_api_service.core.entity.Media;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MediaMapper {
    
    // ===== ENTITY TO DTO MAPPINGS =====
    
    /**
     * Map Media entity to MediaInfoDto
     */
    @Mapping(target = "entityType", source = "entityType", qualifiedByName = "entityTypeToString")
    @Mapping(target = "mediaType", source = "mediaType", qualifiedByName = "mediaTypeToString")
    MediaInfoDto toInfoDto(Media media);
    
    /**
     * Map list of Media entities to list of MediaInfoDto
     */
    List<MediaInfoDto> toInfoDtoList(List<Media> mediaList);
    
    // ===== DTO TO ENTITY MAPPINGS =====
    
    /**
     * Map CreateMediaRequest to Media entity
     */
    @Mapping(target = "mediaId", ignore = true)
    @Mapping(target = "entityType", source = "entityType", qualifiedByName = "stringToEntityType")
    @Mapping(target = "mediaType", source = "mediaType", qualifiedByName = "stringToMediaType")
    Media toEntity(CreateMediaRequest createRequest);
    
    /**
     * Update existing Media entity from UpdateMediaRequest
     */
    @Mapping(target = "mediaId", ignore = true)
    @Mapping(target = "entityType", ignore = true)
    @Mapping(target = "entityId", ignore = true)
    @Mapping(target = "isMain", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "mediaType", source = "mediaType", qualifiedByName = "stringToMediaType")
    void updateEntityFromRequest(UpdateMediaRequest updateRequest, @MappingTarget Media media);
    
    // ===== ENUM CONVERSION METHODS =====
    
    @Named("entityTypeToString")
    default String entityTypeToString(Media.EntityType entityType) {
        return entityType != null ? entityType.name() : null;
    }
    
    @Named("stringToEntityType")
    default Media.EntityType stringToEntityType(String entityType) {
        if (entityType == null || entityType.trim().isEmpty()) {
            return null;
        }
        try {
            return Media.EntityType.valueOf(entityType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    @Named("mediaTypeToString")
    default String mediaTypeToString(Media.MediaType mediaType) {
        return mediaType != null ? mediaType.name() : null;
    }
    
    @Named("stringToMediaType")
    default Media.MediaType stringToMediaType(String mediaType) {
        if (mediaType == null || mediaType.trim().isEmpty()) {
            return Media.MediaType.IMAGE;
        }
        try {
            return Media.MediaType.valueOf(mediaType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Media.MediaType.IMAGE;
        }
    }
    
    // ===== FILTER MAPPINGS =====
    
    /**
     * Map MediaFilterParam to Specification (if needed)
     */
    default MediaFilterParam toFilterParam(MediaFilterParam filterParam) {
        return filterParam;
    }
}
