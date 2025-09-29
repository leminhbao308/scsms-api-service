package com.kltn.scsms_api_service.core.service.impl;

import com.kltn.scsms_api_service.core.dto.inventoryManagement.param.InventoryFilterParam;
import com.kltn.scsms_api_service.core.dto.inventoryManagement.request.InventoryDetailRequest;
import com.kltn.scsms_api_service.core.dto.inventoryManagement.request.InventoryHeaderRequest;
import com.kltn.scsms_api_service.core.dto.inventoryManagement.response.InventoryDetailResponse;
import com.kltn.scsms_api_service.core.dto.inventoryManagement.response.InventoryHeaderResponse;
import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.core.entity.enumAttribute.InventoryStatus;
import com.kltn.scsms_api_service.core.repository.*;
import com.kltn.scsms_api_service.core.service.InventoryService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    
    private final InventoryHeaderRepository inventoryHeaderRepository;
    private final InventoryDetailRepository inventoryDetailRepository;
    private final BranchRepository branchRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    
    @Override
    @Transactional
    public InventoryHeaderResponse createInventory(InventoryHeaderRequest request) {
        // Validate branch
        Branch branch = branchRepository.findById(request.getBranchId())
            .orElseThrow(() -> ClientSideException.builder()
                .code(ErrorCode.NOT_FOUND)
                .message("Branch not found")
                .build());
        
        // Create inventory header
        InventoryHeader header = new InventoryHeader();
        header.setInventoryCode(request.getInventoryCode());
        header.setTransactionType(request.getTransactionType());
        header.setBranch(branch);
        header.setTransactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDateTime.now());
        header.setStatus(request.getStatus() != null ? request.getStatus() : InventoryStatus.DRAFT);
        header.setNotes(request.getNotes());
        header.setRequestedBy(request.getRequestedBy());
        header.setShippingInfo(request.getShippingInfo());
        header.setPaymentInfo(request.getPaymentInfo());
        header.setExpectedReceiveDate(request.getExpectedReceiveDate());
        header.setExpectedShipDate(request.getExpectedShipDate());
        header.setShippingCost(request.getShippingCost() != null ? request.getShippingCost() : BigDecimal.ZERO);
        header.setOtherCosts(request.getOtherCosts() != null ? request.getOtherCosts() : BigDecimal.ZERO);
        header.setReferenceType(request.getReferenceType());
        header.setReferenceId(request.getReferenceId());
        header.setReferenceCode(request.getReferenceCode());
        
        // Set supplier if provided
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> ClientSideException.builder()
                    .code(ErrorCode.NOT_FOUND)
                    .message("Supplier not found")
                    .build());
            header.setSupplier(supplier);
        }
        
        // Save header first to get ID
        InventoryHeader savedHeader = inventoryHeaderRepository.save(header);
        
        // Process details if provided
        List<InventoryDetail> details = new ArrayList<>();
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            details = createInventoryDetails(savedHeader, request.getDetails());
        }
        
        // Update header totals
        updateHeaderTotals(savedHeader, details);
        savedHeader = inventoryHeaderRepository.save(savedHeader);
        
        return mapToHeaderResponse(savedHeader, details);
    }
    
    @Override
    public InventoryHeaderResponse getInventoryById(UUID inventoryId) {
        InventoryHeader header = inventoryHeaderRepository.findById(inventoryId)
            .orElseThrow(() -> ClientSideException.builder()
                .code(ErrorCode.NOT_FOUND)
                .message("Inventory not found")
                .build());
        
        List<InventoryDetail> details = inventoryDetailRepository.findByInventoryInventoryId(inventoryId);
        return mapToHeaderResponse(header, details);
    }
    
    @Override
    public InventoryHeaderResponse getInventoryByCode(String inventoryCode) {
        InventoryHeader header = inventoryHeaderRepository.findByInventoryCode(inventoryCode)
            .orElseThrow(() -> ClientSideException.builder()
                .code(ErrorCode.NOT_FOUND)
                .message("Inventory not found")
                .build());
        
        List<InventoryDetail> details = inventoryDetailRepository.findByInventoryInventoryId(header.getInventoryId());
        return mapToHeaderResponse(header, details);
    }
    
    @Override
    @Transactional
    public InventoryHeaderResponse updateInventory(UUID inventoryId, InventoryHeaderRequest request) {
        // Find existing inventory
        InventoryHeader header = inventoryHeaderRepository.findById(inventoryId)
            .orElseThrow(() -> ClientSideException.builder()
                .code(ErrorCode.NOT_FOUND)
                .message("Inventory not found")
                .build());
        
        // Validate if inventory can be updated based on status
        if (header.getStatus() != InventoryStatus.DRAFT && header.getStatus() != InventoryStatus.PENDING) {
            throw ClientSideException.builder()
                .code(ErrorCode.FORBIDDEN)
                .message("Cannot update inventory with status: " + header.getStatus())
                .build();
        }
        
        // Update header fields
        if (request.getBranchId() != null && !request.getBranchId().equals(header.getBranch().getBranchId())) {
            Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> ClientSideException.builder()
                    .code(ErrorCode.NOT_FOUND)
                    .message("Branch not found")
                    .build());
            header.setBranch(branch);
        }
        
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> ClientSideException.builder()
                    .code(ErrorCode.NOT_FOUND)
                    .message("Supplier not found")
                    .build());
            header.setSupplier(supplier);
        }
        
        header.setTransactionType(request.getTransactionType() != null ? request.getTransactionType() : header.getTransactionType());
        header.setTransactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : header.getTransactionDate());
        header.setStatus(request.getStatus() != null ? request.getStatus() : header.getStatus());
        header.setNotes(request.getNotes() != null ? request.getNotes() : header.getNotes());
        header.setRequestedBy(request.getRequestedBy() != null ? request.getRequestedBy() : header.getRequestedBy());
        header.setShippingInfo(request.getShippingInfo() != null ? request.getShippingInfo() : header.getShippingInfo());
        header.setPaymentInfo(request.getPaymentInfo() != null ? request.getPaymentInfo() : header.getPaymentInfo());
        header.setExpectedReceiveDate(request.getExpectedReceiveDate() != null ? request.getExpectedReceiveDate() : header.getExpectedReceiveDate());
        header.setExpectedShipDate(request.getExpectedShipDate() != null ? request.getExpectedShipDate() : header.getExpectedShipDate());
        header.setShippingCost(request.getShippingCost() != null ? request.getShippingCost() : header.getShippingCost());
        header.setOtherCosts(request.getOtherCosts() != null ? request.getOtherCosts() : header.getOtherCosts());
        header.setReferenceType(request.getReferenceType() != null ? request.getReferenceType() : header.getReferenceType());
        header.setReferenceId(request.getReferenceId() != null ? request.getReferenceId() : header.getReferenceId());
        header.setReferenceCode(request.getReferenceCode() != null ? request.getReferenceCode() : header.getReferenceCode());
        
        // Handle details - delete old ones and create new ones
        if (request.getDetails() != null) {
            inventoryDetailRepository.deleteByInventoryInventoryId(inventoryId);
            List<InventoryDetail> newDetails = createInventoryDetails(header, request.getDetails());
            updateHeaderTotals(header, newDetails);
        }
        
        InventoryHeader updatedHeader = inventoryHeaderRepository.save(header);
        List<InventoryDetail> details = inventoryDetailRepository.findByInventoryInventoryId(updatedHeader.getInventoryId());
        
        return mapToHeaderResponse(updatedHeader, details);
    }
    
    @Override
    @Transactional
    public void deleteInventory(UUID inventoryId) {
        InventoryHeader header = inventoryHeaderRepository.findById(inventoryId)
            .orElseThrow(() -> ClientSideException.builder()
                .code(ErrorCode.NOT_FOUND)
                .message("Inventory not found")
                .build());
        
        // Validate if inventory can be deleted based on status
        if (header.getStatus() != InventoryStatus.DRAFT) {
            throw ClientSideException.builder()
                .code(ErrorCode.FORBIDDEN)
                .message("Cannot delete inventory with status: " + header.getStatus())
                .build();
        }
        
        // Delete details first
        inventoryDetailRepository.deleteByInventoryInventoryId(inventoryId);
        
        // Delete header
        inventoryHeaderRepository.delete(header);
    }
    
    @Override
    public Page<InventoryHeaderResponse> searchInventories(InventoryFilterParam filterParam) {
        Pageable pageable = PageRequest.of(
            Math.min(filterParam.getPage() - 1, 0),
            filterParam.getSize(),
            Sort.by(filterParam.getDirection(), filterParam.getSort())
        );
        
        Page<InventoryHeader> inventoryPage = inventoryHeaderRepository.findInventoryHeadersByFilters(
            filterParam.getInventoryCode(),
            filterParam.getTransactionType(),
            filterParam.getBranchId(),
            filterParam.getSupplierId(),
            filterParam.getStatus(),
            filterParam.getFromDate(),
            filterParam.getToDate(),
            pageable
        );
        
        List<InventoryHeaderResponse> responseList = inventoryPage.getContent().stream()
            .map(header -> {
                List<InventoryDetail> details = inventoryDetailRepository.findByInventoryInventoryId(header.getInventoryId());
                return mapToHeaderResponse(header, details);
            })
            .collect(Collectors.toList());
        
        return new PageImpl<>(responseList, pageable, inventoryPage.getTotalElements());
    }
    
    @Override
    @Transactional
    public InventoryHeaderResponse updateInventoryStatus(UUID inventoryId, String status) {
        InventoryHeader header = inventoryHeaderRepository.findById(inventoryId)
            .orElseThrow(() -> ClientSideException.builder()
                .code(ErrorCode.NOT_FOUND)
                .message("Inventory not found")
                .build());
        
        try {
            InventoryStatus newStatus = InventoryStatus.valueOf(status.toUpperCase());
            
            // Validate status transition
            validateStatusTransition(header.getStatus(), newStatus);
            
            header.setStatus(newStatus);
            
            // Set additional information based on status
            if (newStatus == InventoryStatus.APPROVED) {
                header.setApprovedDate(LocalDateTime.now());
                // Typically, you'd set approvedBy from authenticated user
                header.setApprovedBy("System");
            } else if (newStatus == InventoryStatus.COMPLETED) {
                header.setProcessedDate(LocalDateTime.now());
                // Typically, you'd set processedBy from authenticated user
                header.setProcessedBy("System");
                
                if (header.getTransactionType().toString().contains("INBOUND") ||
                    header.getTransactionType().toString().contains("RETURN")) {
                    header.setActualReceiveDate(LocalDateTime.now());
                } else {
                    header.setActualShipDate(LocalDateTime.now());
                }
            }
            
            InventoryHeader updatedHeader = inventoryHeaderRepository.save(header);
            List<InventoryDetail> details = inventoryDetailRepository.findByInventoryInventoryId(updatedHeader.getInventoryId());
            
            return mapToHeaderResponse(updatedHeader, details);
        } catch (IllegalArgumentException e) {
            throw ClientSideException.builder()
                .code(ErrorCode.BAD_REQUEST)
                .message("Invalid status: " + status)
                .build();
        }
    }
    
    // Helper methods
    private List<InventoryDetail> createInventoryDetails(InventoryHeader header, List<InventoryDetailRequest> detailRequests) {
        List<InventoryDetail> details = new ArrayList<>();
        
        for (InventoryDetailRequest detailRequest : detailRequests) {
            Product product = productRepository.findById(detailRequest.getProductId())
                .orElseThrow(() -> ClientSideException.builder()
                    .code(ErrorCode.NOT_FOUND)
                    .message("Product not found with ID: " + detailRequest.getProductId())
                    .build());
            
            InventoryDetail detail = new InventoryDetail();
            detail.setInventory(header);
            detail.setProduct(product);
            detail.setProductName(detailRequest.getProductName() != null ?
                detailRequest.getProductName() : product.getProductName());
            detail.setProductSku(detailRequest.getProductSku() != null ?
                detailRequest.getProductSku() : product.getSku());
            detail.setUnitOfMeasure(detailRequest.getUnitOfMeasure());
            detail.setQuantity(detailRequest.getQuantity());
            detail.setProductionDate(detailRequest.getProductionDate());
            detail.setReceivedQuantity(detailRequest.getReceivedQuantity());
            detail.setRejectedQuantity(detailRequest.getRejectedQuantity());
            detail.setUnitPrice(detailRequest.getUnitPrice());
            detail.setTotalAmount(detailRequest.getTotalAmount());
            detail.setTaxPercentage(detailRequest.getTaxPercentage() != null ?
                detailRequest.getTaxPercentage() : BigDecimal.ZERO);
            detail.setTaxAmount(detailRequest.getTaxAmount() != null ?
                detailRequest.getTaxAmount() : BigDecimal.ZERO);
            detail.setFinalAmount(detailRequest.getFinalAmount() != null ?
                detailRequest.getFinalAmount() : detailRequest.getTotalAmount());
            detail.setBatchNumber(detailRequest.getBatchNumber());
            detail.setSerialNumbers(detailRequest.getSerialNumbers());
            detail.setQualityStatus(detailRequest.getQualityStatus());
            detail.setNotes(detailRequest.getNotes());
            
            details.add(inventoryDetailRepository.save(detail));
        }
        
        return details;
    }
    
    private void updateHeaderTotals(InventoryHeader header, List<InventoryDetail> details) {
        int totalItems = details.size();
        int totalQuantity = details.stream().mapToInt(InventoryDetail::getQuantity).sum();
        BigDecimal totalAmount = details.stream()
            .map(InventoryDetail::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal grandTotal = totalAmount
            .add(header.getShippingCost() != null ? header.getShippingCost() : BigDecimal.ZERO)
            .add(header.getOtherCosts() != null ? header.getOtherCosts() : BigDecimal.ZERO);
        
        header.setTotalItems(totalItems);
        header.setTotalQuantity(totalQuantity);
        header.setTotalAmount(totalAmount);
        header.setGrandTotal(grandTotal);
    }
    
    private void validateStatusTransition(InventoryStatus currentStatus, InventoryStatus newStatus) {
        // Define allowed transitions
        boolean isAllowed = switch (currentStatus) {
            case DRAFT -> newStatus == InventoryStatus.PENDING || newStatus == InventoryStatus.CANCELLED;
            case PENDING -> newStatus == InventoryStatus.APPROVED || newStatus == InventoryStatus.REJECTED;
            case APPROVED -> newStatus == InventoryStatus.IN_TRANSIT || newStatus == InventoryStatus.CANCELLED;
            case IN_TRANSIT -> newStatus == InventoryStatus.COMPLETED || newStatus == InventoryStatus.PARTIALLY_RECEIVED;
            case PARTIALLY_RECEIVED, COMPLETED, CANCELLED, REJECTED -> false; // Terminal states
        };
        
        if (!isAllowed) {
            throw ClientSideException.builder()
                .code(ErrorCode.FORBIDDEN)
                .message("Invalid status transition from " + currentStatus + " to " + newStatus)
                .build();
        }
    }
    
    private InventoryHeaderResponse mapToHeaderResponse(InventoryHeader header, List<InventoryDetail> details) {
        return InventoryHeaderResponse.builder()
            .inventoryId(header.getInventoryId())
            .inventoryCode(header.getInventoryCode())
            .transactionType(header.getTransactionType())
            .branchId(header.getBranch().getBranchId())
            .branchName(header.getBranch().getBranchName())
            .supplierId(header.getSupplier() != null ? header.getSupplier().getSupplierId() : null)
            .supplierName(header.getSupplier() != null ? header.getSupplier().getSupplierName() : null)
            .referenceType(header.getReferenceType())
            .referenceId(header.getReferenceId())
            .referenceCode(header.getReferenceCode())
            .transactionDate(header.getTransactionDate())
            .expectedReceiveDate(header.getExpectedReceiveDate())
            .actualReceiveDate(header.getActualReceiveDate())
            .expectedShipDate(header.getExpectedShipDate())
            .actualShipDate(header.getActualShipDate())
            .status(header.getStatus())
            .totalItems(header.getTotalItems())
            .totalQuantity(header.getTotalQuantity())
            .totalAmount(header.getTotalAmount())
            .shippingCost(header.getShippingCost())
            .otherCosts(header.getOtherCosts())
            .grandTotal(header.getGrandTotal())
            .notes(header.getNotes())
            .requestedBy(header.getRequestedBy())
            .approvedBy(header.getApprovedBy())
            .approvedDate(header.getApprovedDate())
            .processedBy(header.getProcessedBy())
            .processedDate(header.getProcessedDate())
            .shippingInfo(header.getShippingInfo())
            .paymentInfo(header.getPaymentInfo())
            .createdAt(header.getCreatedDate())
            .createdBy(header.getCreatedBy())
            .updatedAt(header.getModifiedDate())
            .updatedBy(header.getModifiedBy())
            .details(mapToDetailResponses(details))
            .build();
    }
    
    private List<InventoryDetailResponse> mapToDetailResponses(List<InventoryDetail> details) {
        return details.stream()
            .map(detail -> InventoryDetailResponse.builder()
                .inventoryDetailId(detail.getInventoryDetailId())
                .inventoryId(detail.getInventory().getInventoryId())
                .productId(detail.getProduct().getProductId())
                .productName(detail.getProductName())
                .productSku(detail.getProductSku())
                .unitOfMeasure(detail.getUnitOfMeasure())
                .quantity(detail.getQuantity())
                .productionDate(detail.getProductionDate())
                .receivedQuantity(detail.getReceivedQuantity())
                .rejectedQuantity(detail.getRejectedQuantity())
                .unitPrice(detail.getUnitPrice())
                .totalAmount(detail.getTotalAmount())
                .taxPercentage(detail.getTaxPercentage())
                .taxAmount(detail.getTaxAmount())
                .finalAmount(detail.getFinalAmount())
                .batchNumber(detail.getBatchNumber())
                .serialNumbers(detail.getSerialNumbers())
                .qualityStatus(detail.getQualityStatus())
                .notes(detail.getNotes())
                .build())
            .collect(Collectors.toList());
    }
}
