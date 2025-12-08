package com.kltn.scsms_api_service.core.dto.OtpVerification.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^84[0-9]{9}$", message = "Phone number must be in format 84xxxxxxxxx (e.g., 84858484522)")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP code must be 6 digits")
    @JsonProperty("otp_code")
    private String otpCode;
}
