package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.priceManagement.request.PriceListHeaderRequest;
import com.kltn.scsms_api_service.core.dto.priceManagement.response.PriceListHeaderResponse;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.Center;
import com.kltn.scsms_api_service.core.entity.PriceListHeader;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {PriceListDetailMapper.class})
public interface PriceListHeaderMapper {
    
    /**
     * Maps a PriceListHeaderRequest to a new PriceListHeader entity,
     * explicitly setting audit fields to prevent null constraint violations.
     */
    @Mapping(target = "priceListId", ignore = true)
    @Mapping(target = "centers", ignore = true)
    @Mapping(target = "branches", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedDate", ignore = true)
    @Mapping(source = "effectiveDate", target = "effectiveDate")
    @Mapping(source = "expirationDate", target = "expirationDate")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "scope", target = "scope")
    @Mapping(source = "priority", target = "priority", defaultValue = "0")
    @Mapping(source = "currency", target = "currency", defaultValue = "VND")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "internalNotes", target = "internalNotes")
    @Mapping(source = "customerRanks", target = "customerRanks")
    PriceListHeader toEntity(PriceListHeaderRequest request);

    /**
     * Updates an existing PriceListHeader entity with data from the request
     */
    @Mapping(target = "priceListId", ignore = true)
    @Mapping(target = "centers", ignore = true)
    @Mapping(target = "branches", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedDate", ignore = true)
    void updateEntityFromRequest(PriceListHeaderRequest request, @MappingTarget PriceListHeader header);

    /**
     * Maps a PriceListHeader entity to a response DTO
     */
    @Mapping(source = "createdDate", target = "createdAt")
    @Mapping(source = "modifiedDate", target = "updatedAt")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "modifiedBy", target = "updatedBy")
    @Mapping(target = "centers", expression = "java(mapCenters(header.getCenters()))")
    @Mapping(target = "branches", expression = "java(mapBranches(header.getBranches()))")
    @Mapping(target = "details", ignore = true)
    PriceListHeaderResponse toResponse(PriceListHeader header);

    /**
     * Maps a list of Center entities to CenterItemResponse objects
     */
    default List<PriceListHeaderResponse.CenterItemResponse> mapCenters(List<Center> centers) {
        if (centers == null) {
            return java.util.Collections.emptyList();
        }
        return centers.stream()
            .map(center -> PriceListHeaderResponse.CenterItemResponse.builder()
                .centerId(center.getCenterId())
                .centerName(center.getCenterName())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Maps a list of Branch entities to BranchItemResponse objects
     */
    default List<PriceListHeaderResponse.BranchItemResponse> mapBranches(List<Branch> branches) {
        if (branches == null) {
            return java.util.Collections.emptyList();
        }
        return branches.stream()
            .map(branch -> PriceListHeaderResponse.BranchItemResponse.builder()
                .branchId(branch.getBranchId())
                .branchName(branch.getBranchName())
                .build())
            .collect(Collectors.toList());
    }
}
