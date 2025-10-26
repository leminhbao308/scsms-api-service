package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.core.dto.OtpVerification.request.SendOtpRequest;
import com.kltn.scsms_api_service.core.dto.OtpVerification.request.VerifyOtpRequest;
import com.kltn.scsms_api_service.core.dto.OtpVerification.response.OtpResponse;
import com.kltn.scsms_api_service.core.service.businessService.SpeedSmsService;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "OTP Verification", description = "APIs for OTP verification via SpeedSMS")
public class OtpController {

    private final SpeedSmsService speedSmsService;

    /**
     * Gửi OTP đến số điện thoại
     */
    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP", description = "Send OTP code to phone number via SpeedSMS")
    public ResponseEntity<ApiResponse<OtpResponse>> sendOtp(
            @Valid @RequestBody SendOtpRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseBuilder.badRequest(bindingResult.getFieldError().getDefaultMessage());
        }

        log.info("Received send OTP request for phone: {}", request.getPhoneNumber());

        OtpResponse response = speedSmsService.sendOtp(request);

        if (!response.isSuccess()) {
            return ResponseBuilder.badRequest(response.getMessage());
        }

        return ResponseBuilder.success("OTP sent successfully", response);
    }

    /**
     * Xác thực OTP code
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verify OTP code sent to phone number")
    public ResponseEntity<ApiResponse<OtpResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseBuilder.badRequest(bindingResult.getFieldError().getDefaultMessage());
        }

        log.info("Received verify OTP request for phone: {}", request.getPhoneNumber());

        OtpResponse response = speedSmsService.verifyOtp(request);

        if (!response.isSuccess()) {
            return ResponseBuilder.badRequest(response.getMessage());
        }

        return ResponseBuilder.success("OTP verified successfully", response);
    }
}
