package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.branchManagement.BranchInfoDto;
import com.kltn.scsms_api_service.core.dto.branchManagement.param.BranchFilterParam;
import com.kltn.scsms_api_service.core.dto.branchManagement.request.CreateBranchRequest;
import com.kltn.scsms_api_service.core.dto.branchManagement.request.UpdateBranchRequest;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.Center;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import com.kltn.scsms_api_service.core.service.entityService.CenterService;
import com.kltn.scsms_api_service.core.service.entityService.UserService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.BranchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchManagementService {
    
    private final BranchMapper branchMapper;
    private final BranchService branchService;
    private final CenterService centerService;
    private final UserService userService;
    
    public Page<BranchInfoDto> getAllBranches(BranchFilterParam branchFilterParam) {
        
        Page<Branch> branchPage = branchService.getAllBranchesWithFilters(branchFilterParam);
        
        return branchPage.map(branchMapper::toBranchInfoDtoWithRelations);
    }
    
    public BranchInfoDto createBranch(CreateBranchRequest createBranchRequest) {
        // Validate branch name not already in use
        if (branchService.existsByBranchName(createBranchRequest.getBranchName())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                "Branch with name " + createBranchRequest.getBranchName() + " already exists.");
        }
        
        // Validate branch code not already in use
        if (branchService.existsByBranchCode(createBranchRequest.getBranchCode())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                "Branch with code " + createBranchRequest.getBranchCode() + " already exists.");
        }
        
        // Validate center exists
        Center center = centerService.findById(createBranchRequest.getCenterId())
            .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST,
                "Center with ID " + createBranchRequest.getCenterId() + " not found."));
        
        // Validate manager exists (if provided)
        User manager = null;
        if (createBranchRequest.getManagerId() != null) {
            manager = userService.findById(createBranchRequest.getManagerId())
                .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Manager with ID " + createBranchRequest.getManagerId() + " not found."));
        }
        
        // Create new branch
        Branch newBranch = branchMapper.toEntity(createBranchRequest);
        newBranch.setIsActive(true);
        newBranch.setCenter(center);
        
        // Set manager if provided
        if (manager != null) {
            newBranch.setManager(manager);
        }
        
        Branch createdBranch = branchService.saveBranch(newBranch);
        
        // Update center statistics
        centerService.updateCenterStatistics(center.getCenterId());
        
        log.info("Created new branch with name: {} for center: {}",
            createdBranch.getBranchName(), center.getCenterName());
        
        return branchMapper.toBranchInfoDtoWithRelations(createdBranch);
    }
    
    public BranchInfoDto updateBranch(UUID branchId, UpdateBranchRequest updateBranchRequest) {
        // First get existing branch
        Branch existingBranch = branchService.findById(branchId).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "Branch with ID " + branchId + " not found."));
        
        // If branch name is being updated, validate new name doesn't exist
        if (updateBranchRequest.getBranchName() != null &&
            !updateBranchRequest.getBranchName().equals(existingBranch.getBranchName())) {
            if (branchService.existsByBranchName(updateBranchRequest.getBranchName())) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Branch with name " + updateBranchRequest.getBranchName() + " already exists.");
            }
        }
        
        // If branch code is being updated, validate new code doesn't exist
        if (updateBranchRequest.getBranchCode() != null &&
            !updateBranchRequest.getBranchCode().equals(existingBranch.getBranchCode())) {
            if (branchService.existsByBranchCode(updateBranchRequest.getBranchCode())) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Branch with code " + updateBranchRequest.getBranchCode() + " already exists.");
            }
        }
        
        // Validate center exists (if being updated)
        if (updateBranchRequest.getCenterId() != null) {
            Center center = centerService.findById(updateBranchRequest.getCenterId())
                .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Center with ID " + updateBranchRequest.getCenterId() + " not found."));
            existingBranch.setCenter(center);
        }
        
        // Validate manager exists (if being updated)
        if (updateBranchRequest.getManagerId() != null) {
            User manager = userService.findById(updateBranchRequest.getManagerId())
                .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Manager with ID " + updateBranchRequest.getManagerId() + " not found."));
            existingBranch.setManager(manager);
        }
        
        // Update branch using mapper
        Branch updatedBranch = branchMapper.updateEntity(existingBranch, updateBranchRequest);
        
        // Save updated branch
        Branch savedBranch = branchService.saveBranch(updatedBranch);
        
        // Update center statistics if center changed
        if (updateBranchRequest.getCenterId() != null) {
            centerService.updateCenterStatistics(savedBranch.getCenter().getCenterId());
        }
        
        return branchMapper.toBranchInfoDtoWithRelations(savedBranch);
    }
    
    public void deleteBranch(UUID branchId) {
        // Check branch exists
        Branch existingBranch = branchService.findById(branchId).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "Branch with ID " + branchId + " not found."));
        
        // Check if branch has active services or bookings
        if (existingBranch.getCurrentWorkload() > 0) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                "Cannot delete branch with active workload. Please complete or transfer services first.");
        }
        
        UUID centerId = existingBranch.getCenter().getCenterId();
        
        branchService.deleteBranch(existingBranch);
        
        // Update center statistics
        centerService.updateCenterStatistics(centerId);
    }
    
    public BranchInfoDto getBranchById(UUID branchId) {
        Branch branch = branchService.findByIdWithCenterAndManager(branchId).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "Branch with ID " + branchId + " not found."));
        
        return branchMapper.toBranchInfoDtoWithRelations(branch);
    }
    
    public List<BranchInfoDto> getBranchesByCenterId(UUID centerId) {
        // Validate center exists
        centerService.findById(centerId).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "Center with ID " + centerId + " not found."));
        
        List<Branch> branches = branchService.findBranchesByCenterId(centerId);
        
        return branches.stream()
            .map(branchMapper::toBranchInfoDtoWithRelations)
            .toList();
    }
    
    public List<BranchInfoDto> getAvailableBranches() {
        List<Branch> branches = branchService.findAvailableBranches();
        
        return branches.stream()
            .map(branchMapper::toBranchInfoDtoWithRelations)
            .toList();
    }
    
    public List<BranchInfoDto> getBranchesWithinRadius(Double latitude, Double longitude, Double radiusKm) {
        if (latitude == null || longitude == null || radiusKm == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                "Latitude, longitude, and radius are required for location-based search.");
        }
        
        if (radiusKm <= 0 || radiusKm > 100) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                "Radius must be between 0 and 100 kilometers.");
        }
        
        List<Branch> branches = branchService.findBranchesWithinRadius(latitude, longitude, radiusKm);
        
        return branches.stream()
            .map(branchMapper::toBranchInfoDtoWithRelations)
            .toList();
    }
}
