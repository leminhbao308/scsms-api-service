package com.kltn.scsms_api_service.core.dto.userManagement.param;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFilterParam extends BaseFilterParam<UserFilterParam> {
    
    // User-specific filters
    private String role;
    private String gender;
    
    @JsonProperty("user_type")
    private String userType;
    
    @JsonProperty("customer_rank")
    private String customerRank;
    
    @JsonProperty("has_google_id")
    private Boolean hasGoogleId;
    
    @JsonProperty("has_phone_number")
    private Boolean hasPhoneNumber;
    
    @JsonProperty("has_date_of_birth")
    private Boolean hasDateOfBirth;
    
    @JsonProperty("has_avatar")
    private Boolean hasAvatar;
    
    // User-specific search fields
    @Email(message = "Invalid email format")
    private String email;
    
    @JsonProperty("full_name")
    @Size(min = 2, message = "Full name must be at least 2 characters")
    private String fullName;
    
    @JsonProperty("phone_number")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Invalid phone number format")
    private String phoneNumber;
    
    private String address;
    
    // User-specific date ranges
    @JsonProperty("date_of_birth_from")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateOfBirthFrom;
    
    @JsonProperty("date_of_birth_to")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateOfBirthTo;
    
    public static UserFilterParam standardize(UserFilterParam userFilterParam) {
        return userFilterParam.standardizeFilterRequest(userFilterParam);
    }
    
    @Override
    protected String getDefaultSortField() {
        return "createdAt"; // User specific default sort field
    }
    
    @Override
    protected void standardizeSpecificFields(UserFilterParam request) {
        // Chuẩn hóa enum fields
        request.setRole(standardizeEnumField(request.getRole()));
        request.setGender(standardizeEnumField(request.getGender()));
        request.setUserType(standardizeEnumField(request.getUserType()));
        request.setCustomerRank(standardizeEnumField(request.getCustomerRank()));
        
        // Chuẩn hóa search terms
        request.setEmail(trimAndNullify(request.getEmail()));
        request.setFullName(trimAndNullify(request.getFullName()));
        request.setAddress(trimAndNullify(request.getAddress()));
        
        // Chuẩn hóa phone number
        request.setPhoneNumber(cleanPhoneNumber(request.getPhoneNumber()));
    }
}
