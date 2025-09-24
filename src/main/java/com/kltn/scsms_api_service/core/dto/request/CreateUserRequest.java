package com.kltn.scsms_api_service.core.dto.request;

import com.kltn.scsms_api_service.core.enums.Gender;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateUserRequest {
    
    private String password;
    
    private String googleId;

    private String email;
    
    private String fullName;

    private String phoneNumber;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateOfBirth;

    private Gender gender;

    private String address;

    private String avatarUrl;
    
    private String roleCode;
}
