package com.kltn.scsms_api_service.core.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kltn.scsms_api_service.core.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateUserRequest {

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    private LocalDateTime dateOfBirth;

    private Gender gender;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    private String avatarUrl;

    private UUID roleId;

    private Boolean isActive;
}
