package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.branchManagement.BranchFlatDto;
import com.kltn.scsms_api_service.core.dto.branchManagement.BranchInfoDto;
import com.kltn.scsms_api_service.core.dto.branchManagement.request.CreateBranchRequest;
import com.kltn.scsms_api_service.core.dto.branchManagement.request.UpdateBranchRequest;
import com.kltn.scsms_api_service.core.entity.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class}
)
public interface BranchMapper {
    
    @Mapping(target = "centerId", source = "center.centerId")
    @Mapping(target = "managerId", source = "manager.userId")
    @Named("toBranchInfoDto")
    BranchInfoDto toBranchInfoDto(Branch branch);
    
    @Mapping(target = "center", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "managerAssignedAt", ignore = true)
    @Mapping(target = "managerAssignedBy", ignore = true)
    Branch toEntity(CreateBranchRequest createBranchRequest);
    
    @Mapping(target = "centerId", source = "center.centerId")
    @Mapping(target = "managerId", source = "manager.userId")
    BranchFlatDto toBranchFlatDto(Branch branch);
    
    default Branch updateEntity(Branch existingBranch, UpdateBranchRequest updateRequest) {
        if (updateRequest == null) {
            return existingBranch;
        }
        
        if (updateRequest.getBranchName() != null) {
            existingBranch.setBranchName(updateRequest.getBranchName());
        }
        if (updateRequest.getBranchCode() != null) {
            existingBranch.setBranchCode(updateRequest.getBranchCode());
        }
        if (updateRequest.getDescription() != null) {
            existingBranch.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getAddress() != null) {
            existingBranch.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getPhone() != null) {
            existingBranch.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getEmail() != null) {
            existingBranch.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getServiceCapacity() != null) {
            existingBranch.setServiceCapacity(updateRequest.getServiceCapacity());
        }
        if (updateRequest.getAreaSqm() != null) {
            existingBranch.setAreaSqm(updateRequest.getAreaSqm());
        }
        if (updateRequest.getParkingSpaces() != null) {
            existingBranch.setParkingSpaces(updateRequest.getParkingSpaces());
        }
        if (updateRequest.getEstablishedDate() != null) {
            existingBranch.setEstablishedDate(updateRequest.getEstablishedDate());
        }
        if (updateRequest.getOperatingStatus() != null) {
            existingBranch.setOperatingStatus(updateRequest.getOperatingStatus());
        }
        if (updateRequest.getIsActive() != null) {
            existingBranch.setIsActive(updateRequest.getIsActive());
        }
        
        return existingBranch;
    }
    
    default BranchInfoDto toBranchInfoDtoWithRelations(Branch branch) {
        BranchInfoDto dto = toBranchInfoDto(branch);
        
        // Set center information
        if (branch.getCenter() != null) {
            dto.setCenterId(branch.getCenter().getCenterId());
        }
        
        // Set manager information
        if (branch.getManager() != null) {
            dto.setManagerId(branch.getManager().getUserId());
            dto.setManagerAssignedAt(branch.getManagerAssignedAt());
            dto.setManagerAssignedBy(branch.getManagerAssignedBy());
        }
        
        return dto;
    }
}
