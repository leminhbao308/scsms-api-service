package com.kltn.scsms_api_service.core.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.enums.Gender;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("email")
    private String email;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("date_of_birth")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
}
