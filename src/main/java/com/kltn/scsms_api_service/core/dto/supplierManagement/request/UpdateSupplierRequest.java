package com.kltn.scsms_api_service.core.dto.supplierManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateSupplierRequest {
    
    @Size(min = 2, max = 255, message = "Supplier name must be between 2 and 255 characters")
    @JsonProperty("supplier_name")
    private String supplierName;
    
    @Size(max = 255, message = "Contact person must not exceed 255 characters")
    @JsonProperty("contact_person")
    private String contactPerson;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @JsonProperty("phone")
    private String phone;
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @JsonProperty("email")
    private String email;
    
    @Size(max = 1000, message = "Address must not exceed 1000 characters")
    @JsonProperty("address")
    private String address;
    
    @Size(max = 255, message = "Bank name must not exceed 255 characters")
    @JsonProperty("bank_name")
    private String bankName;
    
    @Size(max = 255, message = "Bank account must not exceed 255 characters")
    @JsonProperty("bank_account")
    private String bankAccount;
    
    @JsonProperty("is_active")
    private Boolean isActive;
}
