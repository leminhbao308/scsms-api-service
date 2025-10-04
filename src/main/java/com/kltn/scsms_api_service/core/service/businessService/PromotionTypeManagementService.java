package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.PromotionTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.param.PromotionTypeFilterParam;
import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.request.CreatePromotionTypeRequest;
import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.request.UpdatePromotionTypeRequest;
import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.request.PromotionTypeStatusUpdateRequest;
import com.kltn.scsms_api_service.core.entity.PromotionType;
import com.kltn.scsms_api_service.core.service.entityService.PromotionTypeService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.PromotionTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PromotionTypeManagementService {
    
    private final PromotionTypeService promotionTypeService;
    private final PromotionTypeMapper promotionTypeMapper;
    
    /**
     * Get all promotion types
     */
    public List<PromotionTypeInfoDto> getAllPromotionTypes() {
        log.info("Getting all promotion types");
        List<PromotionType> promotionTypes = promotionTypeService.findAll();
        return promotionTypes.stream()
                .map(promotionTypeMapper::toPromotionTypeInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all promotion types with pagination and filtering
     */
    public Page<PromotionTypeInfoDto> getAllPromotionTypes(PromotionTypeFilterParam filterParam) {
        log.info("Getting all promotion types with filter: {}", filterParam);
        
        // Standardize filter
        filterParam = filterParam.standardizeFilterRequest(filterParam);
        
        // Create pageable
        Sort sort = Sort.by(
                filterParam.getDirection().equalsIgnoreCase("DESC") ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                filterParam.getSort()
        );
        Pageable pageable = PageRequest.of(filterParam.getPage(), filterParam.getSize(), sort);
        
        // Get promotion types (simplified - in real implementation, you'd use custom repository methods)
        Page<PromotionType> promotionTypePage = promotionTypeService.findAll(pageable);
        
        return promotionTypePage.map(promotionTypeMapper::toPromotionTypeInfoDto);
    }
    
    /**
     * Get promotion type by ID
     */
    public PromotionTypeInfoDto getPromotionTypeById(UUID promotionTypeId) {
        log.info("Getting promotion type by ID: {}", promotionTypeId);
        PromotionType promotionType = promotionTypeService.getById(promotionTypeId);
        return promotionTypeMapper.toPromotionTypeInfoDto(promotionType);
    }
    
    /**
     * Get promotion type by type code
     */
    public PromotionTypeInfoDto getPromotionTypeByTypeCode(String typeCode) {
        log.info("Getting promotion type by type code: {}", typeCode);
        PromotionType promotionType = promotionTypeService.findByTypeCode(typeCode)
                .orElseThrow(() -> new ClientSideException(ErrorCode.ENTITY_NOT_FOUND, 
                        "PromotionType not found with type code: " + typeCode));
        return promotionTypeMapper.toPromotionTypeInfoDto(promotionType);
    }
    
    /**
     * Get all active promotion types
     */
    public List<PromotionTypeInfoDto> getActivePromotionTypes() {
        log.info("Getting all active promotion types");
        List<PromotionType> promotionTypes = promotionTypeService.findAllActive();
        return promotionTypes.stream()
                .map(promotionTypeMapper::toPromotionTypeInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Search promotion types by keyword
     */
    public List<PromotionTypeInfoDto> searchPromotionTypes(String keyword) {
        log.info("Searching promotion types by keyword: {}", keyword);
        List<PromotionType> promotionTypes = promotionTypeService.searchByKeyword(keyword);
        return promotionTypes.stream()
                .map(promotionTypeMapper::toPromotionTypeInfoDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Create new promotion type
     */
    @Transactional
    public PromotionTypeInfoDto createPromotionType(CreatePromotionTypeRequest createRequest) {
        log.info("Creating promotion type: {}", createRequest.getTypeName());
        
        // Validate type code uniqueness
        if (promotionTypeService.existsByTypeCode(createRequest.getTypeCode())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "PromotionType with type code already exists: " + createRequest.getTypeCode());
        }
        
        // Create promotion type
        PromotionType promotionType = promotionTypeMapper.toEntity(createRequest);
        PromotionType savedPromotionType = promotionTypeService.save(promotionType);
        
        return promotionTypeMapper.toPromotionTypeInfoDto(savedPromotionType);
    }
    
    /**
     * Update existing promotion type
     */
    @Transactional
    public PromotionTypeInfoDto updatePromotionType(UUID promotionTypeId, UpdatePromotionTypeRequest updateRequest) {
        log.info("Updating promotion type with ID: {}", promotionTypeId);
        
        // Get existing promotion type
        PromotionType existingPromotionType = promotionTypeService.getById(promotionTypeId);
        
        // Validate type code uniqueness if changed
        if (updateRequest.getTypeCode() != null &&
                !updateRequest.getTypeCode().equals(existingPromotionType.getTypeCode()) &&
                promotionTypeService.existsByTypeCodeExcludingId(updateRequest.getTypeCode(), promotionTypeId)) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "PromotionType with type code already exists: " + updateRequest.getTypeCode());
        }
        
        // Update promotion type
        promotionTypeMapper.updateEntityFromRequest(updateRequest, existingPromotionType);
        PromotionType savedPromotionType = promotionTypeService.update(existingPromotionType);
        
        return promotionTypeMapper.toPromotionTypeInfoDto(savedPromotionType);
    }
    
    /**
     * Delete promotion type (soft delete)
     */
    @Transactional
    public void deletePromotionType(UUID promotionTypeId) {
        log.info("Deleting promotion type with ID: {}", promotionTypeId);
        promotionTypeService.softDeleteById(promotionTypeId);
    }
    
    /**
     * Activate promotion type
     */
    @Transactional
    public void activatePromotionType(UUID promotionTypeId) {
        log.info("Activating promotion type with ID: {}", promotionTypeId);
        promotionTypeService.activateById(promotionTypeId);
    }
    
    /**
     * Deactivate promotion type
     */
    @Transactional
    public void deactivatePromotionType(UUID promotionTypeId) {
        log.info("Deactivating promotion type with ID: {}", promotionTypeId);
        promotionTypeService.deactivateById(promotionTypeId);
    }
    
    /**
     * Update promotion type status
     */
    @Transactional
    public PromotionTypeInfoDto updatePromotionTypeStatus(UUID promotionTypeId, PromotionTypeStatusUpdateRequest statusRequest) {
        log.info("Updating promotion type status for ID: {} to {}", promotionTypeId, statusRequest.getIsActive());
        
        // Get existing promotion type
        PromotionType existingPromotionType = promotionTypeService.getById(promotionTypeId);
        
        // Update status
        existingPromotionType.setIsActive(statusRequest.getIsActive());
        
        // Save updated promotion type
        PromotionType updatedPromotionType = promotionTypeService.update(existingPromotionType);
        
        return promotionTypeMapper.toPromotionTypeInfoDto(updatedPromotionType);
    }
    
    /**
     * Get promotion type statistics
     */
    public PromotionTypeStatsDto getPromotionTypeStatistics() {
        log.debug("Getting promotion type statistics");
        
        long totalCount = promotionTypeService.countAll();
        long activeCount = promotionTypeService.countActive();
        
        return PromotionTypeStatsDto.builder()
                .totalPromotionTypes(totalCount)
                .activePromotionTypes(activeCount)
                .inactivePromotionTypes(totalCount - activeCount)
                .build();
    }
    
    /**
     * Statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PromotionTypeStatsDto {
        private long totalPromotionTypes;
        private long activePromotionTypes;
        private long inactivePromotionTypes;
    }
}
