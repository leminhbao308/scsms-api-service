package com.kltn.scsms_api_service.core.dto.supplierManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupplierInfoDto extends AuditDto {
    
    @JsonProperty("supplier_id")
    private UUID supplierId;
    
    @JsonProperty("supplier_name")
    private String supplierName;
    
    @JsonProperty("contact_person")
    private String contactPerson;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("bank_name")
    private String bankName;
    
    @JsonProperty("bank_account")
    private String bankAccount;
}
