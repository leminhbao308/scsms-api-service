package com.kltn.scsms_api_service.core.dto.userManagement.param;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterParam {
    // Pagination
    @Min(value = 1, message = "Page must be greater than 0")
    private int page = 1;
    
    @Min(value = 1, message = "Size must be greater than 0")
    @Max(value = 100, message = "Size must not exceed 100")
    private int size = 10;
    
    private String sort = "createdDate";
    private String direction = "DESC";
    
    // Filters
    private String role;
    
    private Boolean active;
    
    private String gender;
    
    private String userType;
    
    private String customerRank;
    
    private Boolean hasGoogleId;
    private Boolean hasPhoneNumber;
    private Boolean hasDateOfBirth;
    private Boolean hasAvatar;
    
    // Search
    @Size(min = 2, message = "Search term must be at least 2 characters")
    private String search;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Size(min = 2, message = "Full name must be at least 2 characters")
    private String fullName;
    
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Invalid phone number format")
    private String phoneNumber;
    
    private String address;
    
    // Date ranges
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdDateFrom;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdDateTo;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime modifiedDateFrom;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime modifiedDateTo;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateOfBirthFrom;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateOfBirthTo;
    
    public static UserFilterParam standardizeFilterRequest(UserFilterParam request) {
        // Chuẩn hóa pagination
        request.setPage(Math.max(request.getPage() - 1, 0)); // Convert to 0-based
        request.setSize(Math.max(1, Math.min(request.getSize(), 100)));
        
        // Chuẩn hóa sort và direction
        if (request.getSort() == null || request.getSort().trim().isEmpty()) {
            request.setSort("createdAt");
        }
        request.setDirection("ASC".equalsIgnoreCase(request.getDirection()) ? "ASC" : "DESC");
        
        // Chuẩn hóa strings
        if (request.getRole() != null) {
            request.setRole(request.getRole().trim().toUpperCase());
        }
        
        if (request.getGender() != null) {
            request.setGender(request.getGender().trim().toUpperCase());
        }
        
        if (request.getUserType() != null) {
            request.setUserType(request.getUserType().trim().toUpperCase());
        }
        
        if (request.getCustomerRank() != null) {
            request.setCustomerRank(request.getCustomerRank().trim().toUpperCase());
        }
        
        // Chuẩn hóa search terms
        request.setSearch(trimAndNullify(request.getSearch()));
        request.setEmail(trimAndNullify(request.getEmail()));
        request.setFullName(trimAndNullify(request.getFullName()));
        request.setAddress(trimAndNullify(request.getAddress()));
        
        // Chuẩn hóa phone number
        if (request.getPhoneNumber() != null) {
            String cleanPhone = request.getPhoneNumber().replaceAll("[^0-9+]", "");
            request.setPhoneNumber(cleanPhone.length() >= 10 ? cleanPhone : null);
        }
        
        return request;
    }
    
    private static String trimAndNullify(String str) {
        return (str != null && !str.trim().isEmpty()) ? str.trim() : null;
    }
}
