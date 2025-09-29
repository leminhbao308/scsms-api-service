package com.kltn.scsms_api_service.core.dto.supplierManagement.param;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupplierFilterParam extends BaseFilterParam<SupplierFilterParam> {
    
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
    
    @JsonProperty("has_contact_person")
    private Boolean hasContactPerson;
    
    @JsonProperty("has_phone")
    private Boolean hasPhone;
    
    @JsonProperty("has_email")
    private Boolean hasEmail;
    
    @JsonProperty("has_address")
    private Boolean hasAddress;
    
    @JsonProperty("has_bank_info")
    private Boolean hasBankInfo;
    
    @Override
    protected void standardizeSpecificFields(SupplierFilterParam request) {
        // Standardize string fields
        request.setSupplierName(trimAndNullify(request.getSupplierName()));
        request.setContactPerson(trimAndNullify(request.getContactPerson()));
        request.setPhone(cleanPhoneNumber(request.getPhone()));
        request.setEmail(trimAndNullify(request.getEmail()));
        request.setAddress(trimAndNullify(request.getAddress()));
        request.setBankName(trimAndNullify(request.getBankName()));
    }
    
    @Override
    protected String getDefaultSortField() {
        return "supplierName";
    }
}
