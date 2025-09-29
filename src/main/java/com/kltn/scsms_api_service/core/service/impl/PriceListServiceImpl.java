package com.kltn.scsms_api_service.core.service.impl;

import com.kltn.scsms_api_service.core.dto.priceManagement.param.PriceListFilterParam;
import com.kltn.scsms_api_service.core.dto.priceManagement.request.PriceListDetailRequest;
import com.kltn.scsms_api_service.core.dto.priceManagement.request.PriceListHeaderRequest;
import com.kltn.scsms_api_service.core.dto.priceManagement.response.PriceListDetailResponse;
import com.kltn.scsms_api_service.core.dto.priceManagement.response.PriceListHeaderResponse;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.Center;
import com.kltn.scsms_api_service.core.entity.PriceListDetail;
import com.kltn.scsms_api_service.core.entity.PriceListHeader;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CustomerRank;
import com.kltn.scsms_api_service.core.entity.enumAttribute.ItemType;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PriceListStatus;
import com.kltn.scsms_api_service.core.repository.*;
import com.kltn.scsms_api_service.core.service.PriceListService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.PriceListDetailMapper;
import com.kltn.scsms_api_service.mapper.PriceListHeaderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceListServiceImpl implements PriceListService {
    
    private final PriceListHeaderRepository priceListHeaderRepository;
    private final PriceListDetailRepository priceListDetailRepository;
    private final CenterRepository centerRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final ServiceRepository serviceRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final PriceListHeaderMapper priceListHeaderMapper;
    private final PriceListDetailMapper priceListDetailMapper;
    
    @Override
    @Transactional
    public PriceListHeaderResponse createPriceList(PriceListHeaderRequest request) {
        // Convert request to entity using mapper (which ensures audit fields are set)
        PriceListHeader header = priceListHeaderMapper.toEntity(request);
        
        // Set centers based on scope and centerIds
        if (request.getScope() != null &&
            request.getScope().toString().contains("CENTER") &&
            request.getCenterIds() != null &&
            !request.getCenterIds().isEmpty()) {
            List<Center> centers = new ArrayList<>();
            for (UUID centerId : request.getCenterIds()) {
                Center center = centerRepository.findById(centerId)
                    .orElseThrow(() -> ClientSideException.builder()
                        .code(ErrorCode.NOT_FOUND)
                        .message("Center not found with ID: " + centerId)
                        .build());
                centers.add(center);
            }
            header.setCenters(centers);
        }
        
        // Set branches based on scope and branchIds
        if (request.getScope() != null &&
            request.getScope().toString().contains("BRANCH") &&
            request.getBranchIds() != null &&
            !request.getBranchIds().isEmpty()) {
            List<Branch> branches = new ArrayList<>();
            for (UUID branchId : request.getBranchIds()) {
                Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> ClientSideException.builder()
                        .code(ErrorCode.NOT_FOUND).message("Branch not found with ID: " + branchId)
                        .build());
                branches.add(branch);
            }
            header.setBranches(branches);
        }
        
        // Save header first to get ID
        PriceListHeader savedHeader = priceListHeaderRepository.save(header);
        
        // Process details if provided
        List<PriceListDetail> details = new ArrayList<>();
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            details = createPriceListDetails(savedHeader, request.getDetails());
        }
        
        // Create the response using mapper
        PriceListHeaderResponse response = priceListHeaderMapper.toResponse(savedHeader);
        // Add details to response
        response.setDetails(priceListDetailMapper.toResponseList(details));
        
        return response;
    }
    
    @Override
    public PriceListHeaderResponse getPriceListById(UUID priceListId) {
        PriceListHeader header = priceListHeaderRepository.findById(priceListId)
            .orElseThrow(() -> ClientSideException.builder()
                .code(ErrorCode.NOT_FOUND)
                .message("Price list not found")
                .build());
        
        List<PriceListDetail> details = priceListDetailRepository.findByPriceListHeaderPriceListId(priceListId);
        return mapToHeaderResponse(header, details);
    }
    
    @Override
    public PriceListHeaderResponse getPriceListByCode(String priceListCode) {
        PriceListHeader header = priceListHeaderRepository.findByPriceListCode(priceListCode)
            .orElseThrow(() -> ClientSideException.builder()
                .code(ErrorCode.NOT_FOUND)
                .message("Price list not found")
                .build());
        
        List<PriceListDetail> details = priceListDetailRepository.findByPriceListHeaderPriceListId(header.getPriceListId());
        return mapToHeaderResponse(header, details);
    }
    
    @Override
    @Transactional
    public PriceListHeaderResponse updatePriceList(UUID priceListId, PriceListHeaderRequest request) {
        // Find existing price list
        PriceListHeader header = priceListHeaderRepository.findById(priceListId)
            .orElseThrow(() -> ClientSideException.builder()
                .code(ErrorCode.NOT_FOUND)
                .message("Price list not found")
                .build());
        
        // Validate if price list can be updated based on status
        if (header.getStatus() != PriceListStatus.DRAFT && header.getStatus() != PriceListStatus.INACTIVE) {
            throw ClientSideException.builder()
                .code(ErrorCode.FORBIDDEN)
                .message("Cannot update price list with status: " + header.getStatus())
                .build();
        }
        
        // Update header fields
        header.setPriceListName(request.getPriceListName() != null ?
            request.getPriceListName() : header.getPriceListName());
        header.setDescription(request.getDescription() != null ?
            request.getDescription() : header.getDescription());
        header.setEffectiveDate(request.getEffectiveDate() != null ?
            request.getEffectiveDate() : header.getEffectiveDate());
        header.setExpirationDate(request.getExpirationDate() != null ?
            request.getExpirationDate() : header.getExpirationDate());
        header.setStatus(request.getStatus() != null ?
            request.getStatus() : header.getStatus());
        header.setCustomerRanks(request.getCustomerRanks() != null ?
            request.getCustomerRanks() : header.getCustomerRanks());
        header.setPriority(request.getPriority() != null ?
            request.getPriority() : header.getPriority());
        header.setCurrency(request.getCurrency() != null ?
            request.getCurrency() : header.getCurrency());
        header.setInternalNotes(request.getInternalNotes() != null ?
            request.getInternalNotes() : header.getInternalNotes());
        
        // Update scope and related entities if needed
        if (request.getScope() != null && !request.getScope().equals(header.getScope())) {
            header.setScope(request.getScope());
            // Reset centers and branches
            header.setCenters(new ArrayList<>());
            header.setBranches(new ArrayList<>());
        }
        
        // Update centers if scope is center-related
        if (header.getScope().toString().contains("CENTER") &&
            request.getCenterIds() != null &&
            !request.getCenterIds().isEmpty()) {
            List<Center> centers = new ArrayList<>();
            for (UUID centerId : request.getCenterIds()) {
                Center center = centerRepository.findById(centerId)
                    .orElseThrow(() -> ClientSideException.builder()
                        .code(ErrorCode.NOT_FOUND)                        .message("Center not found with ID: " + centerId)
                        .build());
                centers.add(center);
            }
            header.setCenters(centers);
        }
        
        // Update branches if scope is branch-related
        if (header.getScope().toString().contains("BRANCH") &&
            request.getBranchIds() != null &&
            !request.getBranchIds().isEmpty()) {
            List<Branch> branches = new ArrayList<>();
            for (UUID branchId : request.getBranchIds()) {
                Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> ClientSideException.builder()
                        .code(ErrorCode.NOT_FOUND)                        .message("Branch not found with ID: " + branchId)
                        .build());
                branches.add(branch);
            }
            header.setBranches(branches);
        }
        
        // Handle details - delete old ones and create new ones
        if (request.getDetails() != null) {
            priceListDetailRepository.deleteByPriceListHeaderPriceListId(priceListId);
            List<PriceListDetail> newDetails = createPriceListDetails(header, request.getDetails());
            PriceListHeader updatedHeader = priceListHeaderRepository.save(header);
            return mapToHeaderResponse(updatedHeader, newDetails);
        }
        
        PriceListHeader updatedHeader = priceListHeaderRepository.save(header);
        List<PriceListDetail> details = priceListDetailRepository.findByPriceListHeaderPriceListId(updatedHeader.getPriceListId());
        return mapToHeaderResponse(updatedHeader, details);
    }
    
    @Override
    @Transactional
    public void deletePriceList(UUID priceListId) {
        PriceListHeader header = priceListHeaderRepository.findById(priceListId)
            .orElseThrow(() -> ClientSideException.builder()
                .code(ErrorCode.NOT_FOUND)
                .message("Price list not found")
                .build());
        
        // Validate if price list can be deleted based on status
        if (header.getStatus() != PriceListStatus.DRAFT && header.getStatus() != PriceListStatus.INACTIVE) {
            throw ClientSideException.builder()
                .code(ErrorCode.FORBIDDEN)
                .message("Cannot delete price list with status: " + header.getStatus())
                .build();
        }
        
        // Delete details first
        priceListDetailRepository.deleteByPriceListHeaderPriceListId(priceListId);
        
        // Delete header
        priceListHeaderRepository.delete(header);
    }
    
    @Override
    public Page<PriceListHeaderResponse> searchPriceLists(PriceListFilterParam filterParam) {
        // Sửa cách tạo Sort - sử dụng direction làm hướng sắp xếp, và sort làm tên thuộc tính
        Sort.Direction sortDirection = Sort.Direction.fromString(filterParam.getDirection());
        
        // Đảm bảo trang được xử lý chính xác (trang bắt đầu từ 0 trong Spring Data)
        // filterParam.getPage() sẽ trả về trang đã được chuẩn hóa (0-based) nếu BaseFilterParam đã xử lý
        Pageable pageable = PageRequest.of(
            Math.min(filterParam.getPage() - 1, 0), // Chuyển đổi sang 0-based index
            filterParam.getSize(),
            Sort.by(sortDirection, filterParam.getSort())
        );
        
        Page<PriceListHeader> pricePage = priceListHeaderRepository.findPriceListHeadersByFilters(
            filterParam.getPriceListCode(),
            filterParam.getPriceListName(),
            filterParam.getScope(),
            filterParam.getStatus(),
            filterParam.getCurrency(),
            filterParam.getEffectiveDate(),
            filterParam.getExpirationDate(),
            filterParam.getActive(),
            pageable
        );
        
        List<PriceListHeaderResponse> responseList = pricePage.getContent().stream()
            .map(header -> {
                List<PriceListDetail> details = priceListDetailRepository.findByPriceListHeaderPriceListId(header.getPriceListId());
                return mapToHeaderResponse(header, details);
            })
            .collect(Collectors.toList());
        
        return new PageImpl<>(responseList, pageable, pricePage.getTotalElements());
    }
    
    @Override
    @Transactional
    public PriceListHeaderResponse updatePriceListStatus(UUID priceListId, String status) {
        PriceListHeader header = priceListHeaderRepository.findById(priceListId)
            .orElseThrow(() -> ClientSideException.builder()
                .code(ErrorCode.NOT_FOUND)
                .message("Price list not found")
                .build());
        
        try {
            PriceListStatus newStatus = PriceListStatus.valueOf(status.toUpperCase());
            
            // Validate status transition
            validateStatusTransition(header.getStatus(), newStatus);
            
            header.setStatus(newStatus);
            
            // Set additional information based on status
            if (newStatus == PriceListStatus.ACTIVE) {
                header.setApprovedDate(LocalDateTime.now());
                // Typically, you'd set approvedBy from authenticated user
                header.setApprovedBy("System");
            }
            
            PriceListHeader updatedHeader = priceListHeaderRepository.save(header);
            List<PriceListDetail> details = priceListDetailRepository.findByPriceListHeaderPriceListId(updatedHeader.getPriceListId());
            
            return mapToHeaderResponse(updatedHeader, details);
        } catch (IllegalArgumentException e) {
            throw ClientSideException.builder()
                .code(ErrorCode.BAD_REQUEST)
                .message("Invalid status: " + status)
                .build();
        }
    }
    
    @Override
    public List<PriceListHeaderResponse> getActivePriceListsByBranch(UUID branchId) {
        List<PriceListHeader> activePriceLists = priceListHeaderRepository.findActiveByBranch(branchId);
        return activePriceLists.stream()
            .map(header -> {
                List<PriceListDetail> details = priceListDetailRepository.findByPriceListHeaderPriceListId(header.getPriceListId());
                return mapToHeaderResponse(header, details);
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public List<PriceListHeaderResponse> getActivePriceListsByBranchAndCustomerRank(UUID branchId, CustomerRank customerRank) {
        List<PriceListHeader> activePriceLists = priceListHeaderRepository.findActiveByBranchAndCustomerRank(branchId, customerRank);
        return activePriceLists.stream()
            .map(header -> {
                List<PriceListDetail> details = priceListDetailRepository.findByPriceListHeaderPriceListId(header.getPriceListId());
                return mapToHeaderResponse(header, details);
            })
            .collect(Collectors.toList());
    }
    
    // Helper methods
    private List<PriceListDetail> createPriceListDetails(PriceListHeader header, List<PriceListDetailRequest> detailRequests) {
        List<PriceListDetail> details = new ArrayList<>();
        
        for (PriceListDetailRequest detailRequest : detailRequests) {
            PriceListDetail detail = priceListDetailMapper.toEntity(detailRequest);
            detail.setPriceListHeader(header);
            
            // Set reference to related entity based on itemType
            if (detailRequest.getItemType() == ItemType.PRODUCT && detailRequest.getItemId() != null) {
                productRepository.findById(detailRequest.getItemId())
                    .ifPresent(detail::setProduct);
            } else if (detailRequest.getItemType() == ItemType.SERVICE && detailRequest.getItemId() != null) {
                serviceRepository.findById(detailRequest.getItemId())
                    .ifPresent(detail::setService);
            } else if (detailRequest.getItemType() == ItemType.SERVICE_PACKAGE && detailRequest.getItemId() != null) {
                servicePackageRepository.findById(detailRequest.getItemId())
                    .ifPresent(detail::setServicePackage);
            }
            
            details.add(priceListDetailRepository.save(detail));
        }
        
        return details;
    }
    
    private void validateStatusTransition(PriceListStatus currentStatus, PriceListStatus newStatus) {
        // Define allowed transitions
        boolean isAllowed = switch (currentStatus) {
            case DRAFT -> newStatus == PriceListStatus.ACTIVE || newStatus == PriceListStatus.INACTIVE;
            case PENDING, APPROVED, CANCELLED -> false;
            case ACTIVE -> newStatus == PriceListStatus.INACTIVE || newStatus == PriceListStatus.EXPIRED;
            case INACTIVE -> newStatus == PriceListStatus.ACTIVE || newStatus == PriceListStatus.DRAFT;
            case EXPIRED -> newStatus == PriceListStatus.INACTIVE;
        };
        
        if (!isAllowed) {
            throw ClientSideException.builder()
                .code(ErrorCode.FORBIDDEN)
                .message("Invalid status transition from " + currentStatus + " to " + newStatus)
                .build();
        }
    }
    
    private PriceListHeaderResponse mapToHeaderResponse(PriceListHeader header, List<PriceListDetail> details) {
        PriceListHeaderResponse response = priceListHeaderMapper.toResponse(header);
        response.setDetails(priceListDetailMapper.toResponseList(details));
        return response;
    }
}
