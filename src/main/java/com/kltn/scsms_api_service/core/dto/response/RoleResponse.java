package com.kltn.scsms_api_service.core.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    @JsonProperty("role_id")
    private UUID roleId;

    @JsonProperty("role_name")
    private String roleName;

    @JsonProperty("role_code")
    private String roleCode;

    @JsonProperty("description")
    private String description;
}
