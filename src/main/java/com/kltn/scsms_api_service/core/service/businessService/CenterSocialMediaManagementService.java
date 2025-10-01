package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.centerSocialMedia.CenterSocialMediaDto;
import com.kltn.scsms_api_service.core.dto.centerSocialMedia.request.CreateCenterSocialMediaRequest;
import com.kltn.scsms_api_service.core.dto.centerSocialMedia.request.UpdateCenterSocialMediaRequest;
import com.kltn.scsms_api_service.core.dto.centerSocialMedia.request.UpdateCenterSocialMediaStatusRequest;
import com.kltn.scsms_api_service.core.entity.Center;
import com.kltn.scsms_api_service.core.entity.CenterSocialMedia;
import com.kltn.scsms_api_service.core.service.entityService.CenterService;
import com.kltn.scsms_api_service.core.service.entityService.CenterSocialMediaService;
import com.kltn.scsms_api_service.mapper.CenterSocialMediaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CenterSocialMediaManagementService {
    
    private final CenterSocialMediaService centerSocialMediaService;
    private final CenterSocialMediaMapper centerSocialMediaMapper;
    private final CenterService centerService;
    
    public List<CenterSocialMediaDto> getAllSocialMediaByCenter(UUID centerId) {
        log.info("Getting all social media for center: {}", centerId);
        List<CenterSocialMedia> socialMedia = centerSocialMediaService.findByCenterId(centerId);
        return socialMedia.stream()
            .map(centerSocialMediaMapper::toDto)
            .collect(Collectors.toList());
    }
    
    
    public CenterSocialMediaDto getSocialMediaById(UUID id) {
        log.info("Getting social media by ID: {}", id);
        CenterSocialMedia socialMedia = centerSocialMediaService.findById(id);
        return centerSocialMediaMapper.toDto(socialMedia);
    }
    
    @Transactional
    public CenterSocialMediaDto createSocialMedia(CreateCenterSocialMediaRequest request) {
        log.info("Creating social media for center: {}", request.getCenterId());
        Center center = centerService.findById(request.getCenterId())
            .orElseThrow(() -> new RuntimeException("Center not found with ID: " + request.getCenterId()));
        CenterSocialMedia socialMedia = centerSocialMediaMapper.toEntity(request);
        socialMedia.setCenter(center);
        CenterSocialMedia savedSocialMedia = centerSocialMediaService.save(socialMedia);
        return centerSocialMediaMapper.toDto(savedSocialMedia);
    }
    
    @Transactional
    public CenterSocialMediaDto updateSocialMedia(UUID id, UpdateCenterSocialMediaRequest request) {
        log.info("Updating social media with ID: {}", id);
        CenterSocialMedia socialMedia = centerSocialMediaService.findById(id);
        centerSocialMediaMapper.updateEntity(request, socialMedia);
        CenterSocialMedia updatedSocialMedia = centerSocialMediaService.save(socialMedia);
        return centerSocialMediaMapper.toDto(updatedSocialMedia);
    }
    
    @Transactional
    public void deleteSocialMedia(UUID id) {
        log.info("Deleting social media with ID: {}", id);
        centerSocialMediaService.delete(id);
    }
    
    @Transactional
    public CenterSocialMediaDto updateSocialMediaStatus(UUID id, UpdateCenterSocialMediaStatusRequest request) {
        log.info("Updating social media status for ID: {}", id);
        CenterSocialMedia updatedSocialMedia = centerSocialMediaService.updateIsActive(id, request.getIsActive());
        return centerSocialMediaMapper.toDto(updatedSocialMedia);
    }
    
}
