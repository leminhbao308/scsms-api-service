package com.kltn.scsms_api_service.core.dto.userManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.dto.response.RoleResponse;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CustomerRank;
import com.kltn.scsms_api_service.core.entity.enumAttribute.UserType;
import com.kltn.scsms_api_service.core.entity.enumAttribute.Gender;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoDto extends AuditDto {
    
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
    
    @JsonProperty("role")
    private RoleResponse role;
    
    @JsonProperty("user_type")
    private UserType userType;
    
    // Customer-specific fields
    @JsonProperty("customer_rank")
    private CustomerRank customerRank;
    
    @JsonProperty("accumulated_points")
    private Integer accumulatedPoints = 0;
    
    @JsonProperty("total_orders")
    private Integer totalOrders = 0;
    
    @JsonProperty("total_spent")
    private Double totalSpent = 0.0;
    
    // Employee-specific fields
    @JsonProperty("hired_at")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime hiredAt;
    
    @JsonProperty("citizen_id")
    private String citizenId;
}
