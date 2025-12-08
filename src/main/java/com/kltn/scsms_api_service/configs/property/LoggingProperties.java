package com.kltn.scsms_api_service.configs.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "logging")
@Getter
@Setter
public class LoggingProperties {
    private List<String> sensitiveFields = new ArrayList<>();
}
