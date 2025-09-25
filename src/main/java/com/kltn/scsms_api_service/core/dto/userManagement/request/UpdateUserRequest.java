package com.kltn.scsms_api_service.core.dto.userManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.RoleResponse;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CustomerRank;
import com.kltn.scsms_api_service.core.entity.enumAttribute.UserType;
import com.kltn.scsms_api_service.core.enums.Gender;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateUserRequest {
    
    @JsonProperty("user_id")
    private UUID userId;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("full_name")
    private String fullName;
    
    @JsonProperty("phone_number")
    private String phoneNumber;
    
    @JsonProperty("date_of_birth")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateOfBirth;
    
    @JsonProperty("gender")
    private Gender gender;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("avatar_url")
    private String avatarUrl;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("role")
    private RoleResponse role;
    
    // Customer-specific fields
    @JsonProperty("customer_rank")
    private CustomerRank customerRank;
    
    @JsonProperty("accumulated_points")
    private Integer accumulatedPoints = 0;
    
    // Employee-specific fields
    @JsonProperty("citizen_id")
    private String citizenId;
}
