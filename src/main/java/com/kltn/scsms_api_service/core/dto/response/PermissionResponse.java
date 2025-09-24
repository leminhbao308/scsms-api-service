package com.kltn.scsms_api_service.core.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

    @JsonProperty("permission_id")
    private UUID permissionId;

    @JsonProperty("permission_name")
    private String permissionName;

    @JsonProperty("permission_code")
    private String permissionCode;

    @JsonProperty("module")
    private String module;

    @JsonProperty("description")
    private String description;
}
