package com.kltn.scsms_api_service.core.dto.auth.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {

    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String password;
}
