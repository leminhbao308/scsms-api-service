package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.Center;
import com.kltn.scsms_api_service.core.entity.CenterSocialMedia;
import com.kltn.scsms_api_service.core.repository.CenterRepository;
import com.kltn.scsms_api_service.core.repository.CenterSocialMediaRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CenterSocialMediaService {
    
    private final CenterSocialMediaRepository centerSocialMediaRepository;
    private final CenterRepository centerRepository;
    
    public List<CenterSocialMedia> findByCenterId(UUID centerId) {
        log.info("Finding social media for center: {}", centerId);
        return centerSocialMediaRepository.findByCenterCenterId(centerId);
    }
    
    public List<CenterSocialMedia> findActiveByCenterId(UUID centerId) {
        log.info("Finding active social media for center: {}", centerId);
        return centerSocialMediaRepository.findByCenterCenterIdAndIsActiveTrue(centerId);
    }
    
    public Optional<CenterSocialMedia> findByCenterAndPlatform(UUID centerId, String platform) {
        log.info("Finding social media for center: {} and platform: {}", centerId, platform);
        return centerSocialMediaRepository.findByCenterAndPlatform(centerId, platform);
    }
    
    public Optional<CenterSocialMedia> findActiveByCenterAndPlatform(UUID centerId, String platform) {
        log.info("Finding active social media for center: {} and platform: {}", centerId, platform);
        return centerSocialMediaRepository.findActiveByCenterAndPlatform(centerId, platform);
    }
    
    public CenterSocialMedia findById(UUID id) {
        log.info("Finding social media by ID: {}", id);
        return centerSocialMediaRepository.findById(id)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Social media not found with ID: " + id));
    }
    
    @Transactional
    public CenterSocialMedia save(CenterSocialMedia centerSocialMedia) {
        log.info("Saving social media for center: {}", centerSocialMedia.getCenter().getCenterId());
        
        // Validate center exists
        Center center = centerRepository.findById(centerSocialMedia.getCenter().getCenterId())
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Center not found"));
        
        centerSocialMedia.setCenter(center);
        
        // Check if social media for this platform already exists
        Optional<CenterSocialMedia> existing = findByCenterAndPlatform(
            center.getCenterId(), 
            centerSocialMedia.getPlatform()
        );
        
        if (existing.isPresent() && !existing.get().getSocialMediaId().equals(centerSocialMedia.getSocialMediaId())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Social media for platform " + centerSocialMedia.getPlatform() + " already exists for this center");
        }
        
        return centerSocialMediaRepository.save(centerSocialMedia);
    }
    
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting social media with ID: {}", id);
        CenterSocialMedia centerSocialMedia = findById(id);
        centerSocialMedia.setIsDeleted(true);
        centerSocialMediaRepository.save(centerSocialMedia);
    }
    
    @Transactional
    public CenterSocialMedia updateIsActive(UUID id, Boolean isActive) {
        log.info("Updating isActive status for social media ID: {} to {}", id, isActive);
        CenterSocialMedia centerSocialMedia = findById(id);
        centerSocialMedia.setIsActive(isActive);
        return centerSocialMediaRepository.save(centerSocialMedia);
    }
}
