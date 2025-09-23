package com.kltn.scsms_api_service.core.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleOAuth2UrlResponse {

    @JsonProperty("authorization_url")
    private String authorizationUrl;

    @JsonProperty("state")
    private String state;
}
