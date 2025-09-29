package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.supplierManagement.SupplierInfoDto;
import com.kltn.scsms_api_service.core.dto.supplierManagement.param.SupplierFilterParam;
import com.kltn.scsms_api_service.core.dto.supplierManagement.request.CreateSupplierRequest;
import com.kltn.scsms_api_service.core.dto.supplierManagement.request.UpdateSupplierRequest;
import com.kltn.scsms_api_service.core.entity.Supplier;
import com.kltn.scsms_api_service.core.service.entityService.SupplierService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.SupplierMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierManagementService {
    
    private final SupplierMapper supplierMapper;
    private final SupplierService supplierService;
    
    public Page<SupplierInfoDto> getAllSuppliers(SupplierFilterParam supplierFilterParam) {
        
        Page<Supplier> supplierPage = supplierService.getAllSuppliersWithFilters(supplierFilterParam);
        
        return supplierPage.map(supplierMapper::toSupplierInfoDto);
    }
    
    public SupplierInfoDto createSupplier(CreateSupplierRequest createSupplierRequest) {
        // Validate supplier name not already in use
        if (supplierService.existsBySupplierName(createSupplierRequest.getSupplierName())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                "Supplier with name " + createSupplierRequest.getSupplierName() + " already exists.");
        }
        
        // Validate email not already in use (if provided)
        if (createSupplierRequest.getEmail() != null &&
            supplierService.existsByEmail(createSupplierRequest.getEmail())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                "Supplier with email " + createSupplierRequest.getEmail() + " already exists.");
        }
        
        // Validate phone not already in use (if provided)
        if (createSupplierRequest.getPhone() != null &&
            supplierService.existsByPhone(createSupplierRequest.getPhone())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                "Supplier with phone " + createSupplierRequest.getPhone() + " already exists.");
        }
        
        // Create new supplier
        Supplier newSupplier = supplierMapper.toEntity(createSupplierRequest);
        newSupplier.setIsActive(true);
        
        Supplier createdSupplier = supplierService.saveSupplier(newSupplier);
        
        log.info("Created new supplier with name: {}", createdSupplier.getSupplierName());
        
        return supplierMapper.toSupplierInfoDto(createdSupplier);
    }
    
    public SupplierInfoDto updateSupplier(UUID supplierId, UpdateSupplierRequest updateSupplierRequest) {
        // First get existing supplier
        Supplier existingSupplier = supplierService.findById(supplierId).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "Supplier with ID " + supplierId + " not found."));
        
        // If supplier name is being updated, validate new name doesn't exist
        if (updateSupplierRequest.getSupplierName() != null &&
            !updateSupplierRequest.getSupplierName().equals(existingSupplier.getSupplierName())) {
            if (supplierService.existsBySupplierName(updateSupplierRequest.getSupplierName())) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Supplier with name " + updateSupplierRequest.getSupplierName() + " already exists.");
            }
        }
        
        // If email is being updated, validate new email doesn't exist
        if (updateSupplierRequest.getEmail() != null &&
            !updateSupplierRequest.getEmail().equals(existingSupplier.getEmail())) {
            if (supplierService.existsByEmail(updateSupplierRequest.getEmail())) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Supplier with email " + updateSupplierRequest.getEmail() + " already exists.");
            }
        }
        
        // If phone is being updated, validate new phone doesn't exist
        if (updateSupplierRequest.getPhone() != null &&
            !updateSupplierRequest.getPhone().equals(existingSupplier.getPhone())) {
            if (supplierService.existsByPhone(updateSupplierRequest.getPhone())) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Supplier with phone " + updateSupplierRequest.getPhone() + " already exists.");
            }
        }
        
        // Update supplier using mapper
        Supplier updatedSupplier = supplierMapper.updateEntity(existingSupplier, updateSupplierRequest);
        
        // Save updated supplier
        Supplier savedSupplier = supplierService.saveSupplier(updatedSupplier);
        
        return supplierMapper.toSupplierInfoDto(savedSupplier);
    }
    
    public void deleteSupplier(UUID supplierId) {
        // Check supplier exists
        Supplier existingSupplier = supplierService.findById(supplierId).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "Supplier with ID " + supplierId + " not found."));
        
        supplierService.deleteSupplier(existingSupplier);
    }
    
    public SupplierInfoDto getSupplierById(UUID supplierId) {
        Supplier supplier = supplierService.findById(supplierId).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "Supplier with ID " + supplierId + " not found."));
        
        return supplierMapper.toSupplierInfoDto(supplier);
    }
}
