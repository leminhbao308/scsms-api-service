package com.kltn.scsms_api_service.core.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("new_password")
    private String newPassword;
}
