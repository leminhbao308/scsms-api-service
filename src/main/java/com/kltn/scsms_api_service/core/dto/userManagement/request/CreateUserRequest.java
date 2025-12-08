package com.kltn.scsms_api_service.core.dto.userManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CustomerRank;
import com.kltn.scsms_api_service.core.entity.enumAttribute.UserType;
import com.kltn.scsms_api_service.core.entity.enumAttribute.Gender;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateUserRequest {


    private String password;

    private String googleId;

    private String email;

    private String fullName;

    private String phoneNumber;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    private Gender gender;

    private String address;

    private String avatarUrl;

    private String roleCode;

    @JsonProperty("user_type")
    private UserType userType;

    // Customer-specific fields
    @JsonProperty("customer_rank")
    private CustomerRank customerRank = CustomerRank.BRONZE;

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
