package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.supplierManagement.SupplierInfoDto;
import com.kltn.scsms_api_service.core.dto.supplierManagement.request.CreateSupplierRequest;
import com.kltn.scsms_api_service.core.dto.supplierManagement.request.UpdateSupplierRequest;
import com.kltn.scsms_api_service.core.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class}
)
public interface SupplierMapper {
    
    SupplierInfoDto toSupplierInfoDto(Supplier supplier);
    
    Supplier toEntity(CreateSupplierRequest createSupplierRequest);
    
    default Supplier updateEntity(Supplier existingSupplier, UpdateSupplierRequest updateRequest) {
        if (updateRequest == null) {
            return existingSupplier;
        }
        
        if (updateRequest.getSupplierName() != null) {
            existingSupplier.setSupplierName(updateRequest.getSupplierName());
        }
        if (updateRequest.getContactPerson() != null) {
            existingSupplier.setContactPerson(updateRequest.getContactPerson());
        }
        if (updateRequest.getPhone() != null) {
            existingSupplier.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getEmail() != null) {
            existingSupplier.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getAddress() != null) {
            existingSupplier.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getBankName() != null) {
            existingSupplier.setBankName(updateRequest.getBankName());
        }
        if (updateRequest.getBankAccount() != null) {
            existingSupplier.setBankAccount(updateRequest.getBankAccount());
        }
        if (updateRequest.getIsActive() != null) {
            existingSupplier.setIsActive(updateRequest.getIsActive());
        }
        
        return existingSupplier;
    }
}
