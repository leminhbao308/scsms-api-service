package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.branchManagement.BranchInfoDto;
import com.kltn.scsms_api_service.core.dto.branchManagement.param.BranchFilterParam;
import com.kltn.scsms_api_service.core.dto.branchManagement.request.CreateBranchRequest;
import com.kltn.scsms_api_service.core.dto.branchManagement.request.UpdateBranchRequest;
import com.kltn.scsms_api_service.core.dto.branchManagement.request.UpdateBranchStatusRequest;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.Center;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import com.kltn.scsms_api_service.core.service.entityService.CenterService;
import com.kltn.scsms_api_service.core.service.entityService.UserService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceBayService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.BranchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ServiceBayService serviceBayService;
    
    public Page<BranchInfoDto> getAllBranches(BranchFilterParam branchFilterParam) {
        
        Page<Branch> branchPage = branchService.getAllBranchesWithFilters(branchFilterParam);
        
        return branchPage.map(branchMapper::toBranchInfoDtoWithRelations);
    }
    
    @Transactional
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
        
        // Tự động tạo 8 ServiceBay mặc định cho branch mới
        createDefaultServiceBays(createdBranch);
        
        log.info("Created new branch with name: {} for center: {} and 8 default service bays",
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
        
        return branchMapper.toBranchInfoDtoWithRelations(savedBranch);
    }
    
    public void deleteBranch(UUID branchId) {
        // Check branch exists
        Branch existingBranch = branchService.findById(branchId).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "Branch with ID " + branchId + " not found."));
        
        
        branchService.deleteBranch(existingBranch);
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
    
    public BranchInfoDto updateBranchStatus(UUID branchId, UpdateBranchStatusRequest updateBranchStatusRequest) {
        log.info("Updating branch status for ID: {}", branchId);
        
        Branch existingBranch = branchService.findById(branchId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                "Branch not found with ID: " + branchId));
        
        if (updateBranchStatusRequest.getIsActive() != null) {
            existingBranch.setIsActive(updateBranchStatusRequest.getIsActive());
        }
        
        Branch updatedBranch = branchService.saveBranch(existingBranch);
        
        return branchMapper.toBranchInfoDtoWithRelations(updatedBranch);
    }
    
    // Alternative method name for testing
    public BranchInfoDto updateBranchActiveStatus(UUID branchId, UpdateBranchStatusRequest updateBranchStatusRequest) {
        return updateBranchStatus(branchId, updateBranchStatusRequest);
    }
    
    /**
     * Tự động tạo 8 ServiceBay mặc định cho branch mới
     * Mỗi branch sẽ có 8 khu vực dịch vụ cố định
     */
    private void createDefaultServiceBays(Branch branch) {
        log.info("Creating 8 default service bays for branch: {}", branch.getBranchName());
        
        // Sử dụng branch code để tạo bay code unique cho mỗi branch
        String branchCodePrefix = branch.getBranchCode() != null ? branch.getBranchCode() : 
            branch.getBranchId().toString().substring(0, 8).toUpperCase();
        
        for (int i = 1; i <= 8; i++) {
            // Tạo bay code unique dựa trên branch code để tránh trùng lặp
            String uniqueBayCode = branchCodePrefix + "-BAY-" + String.format("%03d", i);
            
            ServiceBay serviceBay = ServiceBay.builder()
                .branch(branch)
                .bayName("Khu vực " + i)
                .bayCode(uniqueBayCode)
                .description("Khu vực dịch vụ mặc định " + i + " của " + branch.getBranchName())
                .status(ServiceBay.BayStatus.ACTIVE)
                .displayOrder(i)
                .notes("Tự động tạo khi khởi tạo branch")
                .build();
            
            try {
                serviceBayService.save(serviceBay);
                log.debug("Created service bay: {} (code: {}) for branch: {}", 
                    serviceBay.getBayName(), serviceBay.getBayCode(), branch.getBranchName());
            } catch (Exception e) {
                log.error("Failed to create service bay {} for branch {}: {}", 
                    i, branch.getBranchName(), e.getMessage());
                throw new ClientSideException(ErrorCode.SYSTEM_ERROR,
                    "Failed to create default service bays for branch: " + e.getMessage());
            }
        }
        
        log.info("Successfully created 8 default service bays for branch: {}", branch.getBranchName());
    }
}
