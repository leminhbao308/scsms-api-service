package com.kltn.scsms_api_service.core.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangePasswordRequest {

    private String currentPassword;

    private String newPassword;
}
