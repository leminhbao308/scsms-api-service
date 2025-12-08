package com.kltn.scsms_api_service.core.dto.centerManagement.param;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import com.kltn.scsms_api_service.core.entity.Center;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CenterFilterParam extends BaseFilterParam<CenterFilterParam> {
    
    @JsonProperty("center_name")
    private String centerName;
    
    @JsonProperty("center_code")
    private String centerCode;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("headquarters_address")
    private String headquartersAddress;
    
    @JsonProperty("headquarters_phone")
    private String headquartersPhone;
    
    @JsonProperty("headquarters_email")
    private String headquartersEmail;
    
    @JsonProperty("website")
    private String website;
    
    @JsonProperty("tax_code")
    private String taxCode;
    
    @JsonProperty("business_license")
    private String businessLicense;
    
    @JsonProperty("operating_status")
    private Center.OperatingStatus operatingStatus;
    
    @JsonProperty("manager_id")
    private java.util.UUID managerId;
    
    @JsonProperty("established_date_from")
    private LocalDate establishedDateFrom;
    
    @JsonProperty("established_date_to")
    private LocalDate establishedDateTo;
    
    
    @JsonProperty("has_manager")
    private Boolean hasManager;
    
    @JsonProperty("has_website")
    private Boolean hasWebsite;
    
    @JsonProperty("has_tax_code")
    private Boolean hasTaxCode;
    
    @JsonProperty("has_business_license")
    private Boolean hasBusinessLicense;
    
    @JsonProperty("has_logo")
    private Boolean hasLogo;
    
    @Override
    protected void standardizeSpecificFields(CenterFilterParam request) {
        // Standardize string fields
        request.setCenterName(trimAndNullify(request.getCenterName()));
        request.setCenterCode(trimAndNullify(request.getCenterCode()));
        request.setDescription(trimAndNullify(request.getDescription()));
        request.setHeadquartersAddress(trimAndNullify(request.getHeadquartersAddress()));
        request.setHeadquartersPhone(cleanPhoneNumber(request.getHeadquartersPhone()));
        request.setHeadquartersEmail(trimAndNullify(request.getHeadquartersEmail()));
        request.setWebsite(trimAndNullify(request.getWebsite()));
        request.setTaxCode(trimAndNullify(request.getTaxCode()));
        request.setBusinessLicense(trimAndNullify(request.getBusinessLicense()));
    }
    
    @Override
    protected String getDefaultSortField() {
        return "centerName";
    }
}
