package com.kltn.scsms_api_service.core.dto.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.enums.Gender;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class RegisterRequest {
    
    private String password;
    
    @JsonProperty("google_id")
    private String googleId;

    private String email;
    
    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("phone_number")
    private String phoneNumber;
    
    @JsonProperty("date_of_birth")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateOfBirth;

    private Gender gender;

    private String address;

    @JsonProperty("avatar_url")
    private String avatarUrl;
}
