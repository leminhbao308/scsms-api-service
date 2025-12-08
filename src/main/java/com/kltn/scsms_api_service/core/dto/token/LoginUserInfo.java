package com.kltn.scsms_api_service.core.dto.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUserInfo {

    private String sub;
    
    @JsonProperty("full_name")
    private String fullName;

    private String email;

    private String phone;

    private String role;
    
    private String type;
    
    private String jti;

    private Integer iat;

    private Integer exp;

    private Set<String> permissions;
}
