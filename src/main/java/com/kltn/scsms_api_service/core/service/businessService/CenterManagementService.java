package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.centerManagement.param.CenterFilterParam;
import com.kltn.scsms_api_service.core.dto.centerManagement.request.CreateCenterRequest;
import com.kltn.scsms_api_service.core.dto.centerManagement.CenterInfoDto;
import com.kltn.scsms_api_service.core.dto.centerManagement.request.UpdateCenterRequest;
import com.kltn.scsms_api_service.core.entity.Center;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.core.service.entityService.CenterService;
import com.kltn.scsms_api_service.core.service.entityService.UserService;
import com.kltn.scsms_api_service.mapper.CenterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CenterManagementService {
    
    private final CenterMapper centerMapper;
    private final CenterService centerService;
    private final UserService userService;
    
    public Page<CenterInfoDto> getAllCenters(CenterFilterParam centerFilterParam) {
        
        Page<Center> centerPage = centerService.getAllCentersWithFilters(centerFilterParam);
        
        return centerPage.map(centerMapper::toCenterInfoDtoWithManager);
    }
    
    public CenterInfoDto createCenter(CreateCenterRequest createCenterRequest) {
        // Validate center name not already in use
        if (centerService.existsByCenterName(createCenterRequest.getCenterName())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Center with name " + createCenterRequest.getCenterName() + " already exists.");
        }
        
        // Validate center code not already in use
        if (centerService.existsByCenterCode(createCenterRequest.getCenterCode())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Center with code " + createCenterRequest.getCenterCode() + " already exists.");
        }
        
        // Validate tax code not already in use (if provided)
        if (createCenterRequest.getTaxCode() != null && 
            centerService.existsByTaxCode(createCenterRequest.getTaxCode())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Center with tax code " + createCenterRequest.getTaxCode() + " already exists.");
        }
        
        // Validate business license not already in use (if provided)
        if (createCenterRequest.getBusinessLicense() != null && 
            centerService.existsByBusinessLicense(createCenterRequest.getBusinessLicense())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Center with business license " + createCenterRequest.getBusinessLicense() + " already exists.");
        }
        
        // Validate manager exists (if provided)
        User manager = null;
        if (createCenterRequest.getManagerId() != null) {
            manager = userService.findById(createCenterRequest.getManagerId())
                .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Manager with ID " + createCenterRequest.getManagerId() + " not found."));
        }
        
        // Create new center
        Center newCenter = centerMapper.toEntity(createCenterRequest);
        newCenter.setIsActive(true);
        newCenter.setOperatingStatus(Center.OperatingStatus.ACTIVE);
        
        // Set manager if provided
        if (manager != null) {
            newCenter.setManager(manager);
        }
        
        Center createdCenter = centerService.saveCenter(newCenter);
        
        log.info("Created new center with name: {}", createdCenter.getCenterName());
        
        return centerMapper.toCenterInfoDtoWithManager(createdCenter);
    }
    
    public CenterInfoDto updateCenter(UUID centerId, UpdateCenterRequest updateCenterRequest) {
        // First get existing center
        Center existingCenter = centerService.findById(centerId).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "Center with ID " + centerId + " not found."));
        
        // If center name is being updated, validate new name doesn't exist
        if (updateCenterRequest.getCenterName() != null && 
            !updateCenterRequest.getCenterName().equals(existingCenter.getCenterName())) {
            if (centerService.existsByCenterName(updateCenterRequest.getCenterName())) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Center with name " + updateCenterRequest.getCenterName() + " already exists.");
            }
        }
        
        // If center code is being updated, validate new code doesn't exist
        if (updateCenterRequest.getCenterCode() != null && 
            !updateCenterRequest.getCenterCode().equals(existingCenter.getCenterCode())) {
            if (centerService.existsByCenterCode(updateCenterRequest.getCenterCode())) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Center with code " + updateCenterRequest.getCenterCode() + " already exists.");
            }
        }
        
        // If tax code is being updated, validate new tax code doesn't exist
        if (updateCenterRequest.getTaxCode() != null && 
            !updateCenterRequest.getTaxCode().equals(existingCenter.getTaxCode())) {
            if (centerService.existsByTaxCode(updateCenterRequest.getTaxCode())) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Center with tax code " + updateCenterRequest.getTaxCode() + " already exists.");
            }
        }
        
        // If business license is being updated, validate new license doesn't exist
        if (updateCenterRequest.getBusinessLicense() != null && 
            !updateCenterRequest.getBusinessLicense().equals(existingCenter.getBusinessLicense())) {
            if (centerService.existsByBusinessLicense(updateCenterRequest.getBusinessLicense())) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Center with business license " + updateCenterRequest.getBusinessLicense() + " already exists.");
            }
        }
        
        // Validate manager exists (if being updated)
        if (updateCenterRequest.getManagerId() != null) {
            User manager = userService.findById(updateCenterRequest.getManagerId())
                .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Manager with ID " + updateCenterRequest.getManagerId() + " not found."));
            existingCenter.setManager(manager);
        }
        
        // Update center using mapper
        Center updatedCenter = centerMapper.updateEntity(existingCenter, updateCenterRequest);
        
        // Save updated center
        Center savedCenter = centerService.saveCenter(updatedCenter);
        
        return centerMapper.toCenterInfoDtoWithManager(savedCenter);
    }
    
    public void deleteCenter(UUID centerId) {
        // Check center exists
        Center existingCenter = centerService.findById(centerId).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "Center with ID " + centerId + " not found."));
        
        // Check if center has active branches
        if (!existingCenter.getBranches().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Cannot delete center with active branches. Please delete or transfer branches first.");
        }
        
        centerService.deleteCenter(existingCenter);
    }
    
    public CenterInfoDto getCenterById(UUID centerId) {
        Center center = centerService.findByIdWithBranchesAndManager(centerId).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "Center with ID " + centerId + " not found."));
        
        return centerMapper.toCenterInfoDtoWithManager(center);
    }
    
    public CenterInfoDto getCenterWithBranches(UUID centerId) {
        Center center = centerService.findByIdWithBranches(centerId).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "Center with ID " + centerId + " not found."));
        
        return centerMapper.toCenterInfoDtoWithManager(center);
    }
}
