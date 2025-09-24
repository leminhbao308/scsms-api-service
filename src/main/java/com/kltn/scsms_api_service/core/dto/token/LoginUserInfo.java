package com.kltn.scsms_api_service.core.dto.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUserInfo {

    private String sub;

    private String email;

    private String phone;

    private String role;
    
    private String type;
    
    private String jti;

    private Integer iat;

    private Integer exp;

}
