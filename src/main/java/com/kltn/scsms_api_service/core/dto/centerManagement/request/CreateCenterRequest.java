package com.kltn.scsms_api_service.core.dto.centerManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateCenterRequest {
    
    @NotBlank(message = "Center name is required")
    @Size(min = 2, max = 255, message = "Center name must be between 2 and 255 characters")
    @JsonProperty("center_name")
    private String centerName;
    
    @NotBlank(message = "Center code is required")
    @Size(min = 2, max = 50, message = "Center code must be between 2 and 50 characters")
    @JsonProperty("center_code")
    private String centerCode;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @JsonProperty("description")
    private String description;
    
    @Size(max = 1000, message = "Headquarters address must not exceed 1000 characters")
    @JsonProperty("headquarters_address")
    private String headquartersAddress;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @JsonProperty("headquarters_phone")
    private String headquartersPhone;
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @JsonProperty("headquarters_email")
    private String headquartersEmail;
    
    @Size(max = 255, message = "Website must not exceed 255 characters")
    @JsonProperty("website")
    private String website;
    
    @Size(max = 50, message = "Tax code must not exceed 50 characters")
    @JsonProperty("tax_code")
    private String taxCode;
    
    @Size(max = 100, message = "Business license must not exceed 100 characters")
    @JsonProperty("business_license")
    private String businessLicense;
    
    @Size(max = 1000, message = "Logo URL must not exceed 1000 characters")
    @JsonProperty("logo_url")
    private String logoUrl;
    
    @JsonProperty("established_date")
    private LocalDate establishedDate;
    
    @JsonProperty("manager_id")
    private java.util.UUID managerId;
    
    @JsonProperty("business_hours")
    private String businessHours;
    
    @JsonProperty("contact_info")
    private String contactInfo;
    
    @JsonProperty("social_media")
    private String socialMedia;
    
    @JsonProperty("service_areas")
    private String serviceAreas;
}
