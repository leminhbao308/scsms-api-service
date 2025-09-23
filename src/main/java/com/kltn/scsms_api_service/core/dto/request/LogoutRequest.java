package com.kltn.scsms_api_service.core.dto.request;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
