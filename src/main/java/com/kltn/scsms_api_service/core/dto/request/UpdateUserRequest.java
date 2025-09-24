package com.kltn.scsms_api_service.core.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kltn.scsms_api_service.core.enums.Gender;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateUserRequest {

    private String email;

    private String fullName;

    private String phoneNumber;

    private LocalDateTime dateOfBirth;

    private Gender gender;

    private String address;

    private String avatarUrl;

    private UUID roleId;

    private Boolean isActive;
}
