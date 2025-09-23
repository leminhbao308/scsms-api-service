package com.kltn.scsms_api_service.core.utils;

import com.kltn.scsms_api_service.core.configs.property.LoggingProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@AllArgsConstructor
public class SensitiveValueMasker {
    private final LoggingProperties loggingProperties;
    private static final String BEARER = "Bearer ";
    private static final int VISIBLE_COUNT = 7;

    public String maskSensitiveStringValues(String input) {
        String masked = input;

        for (String key : loggingProperties.getSensitiveFields()) {
            String regex1 = key + "\\s*=\\s*([^,\\)\\s]+)";
            String regex2 = "\"([^\"]*\\.)?" + Pattern.quote(key) + "\"\\s*:\\s*\"(.*?)\"";

            masked = maskStringUsingRegex(masked, regex1, key);
            masked = maskStringUsingRegex(masked, regex2, key);
        }

        return masked;
    }

    public String maskSensitiveObjectValues(Object input) {
        String masked = input.toString();

        for (String key : loggingProperties.getSensitiveFields()) {
            String regex1 = key + "\\s*=\\s*([^,\\)]+)";
            String regex2 = "\"" + key + "\"\\s*:\\s*\"(.*?)\"";
            masked = maskUsingRegex(masked, regex1, key);
            masked = maskUsingRegex(masked, regex2, key);
        }
        return masked;
    }

    private String maskUsingRegex(String input, String regex, String key) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String originalValue = matcher.group(1).trim();
            if (originalValue.contains("*")) {
                matcher.appendReplacement(sb, matcher.group(0));
            } else {
                String maskedValue = maskKeepFirst7(originalValue);
                String replacement = matcher.group(0).replace(originalValue, maskedValue);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String maskKeepFirst7(String value) {
        if (value == null || value.length() <= 7) {
            return value;
        }
        return value.substring(0, 7) + "*****";
    }

    private String maskStringUsingRegex(String input, String regex, String key) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String originalValue;
            if (regex.contains("\"")) {
                originalValue = matcher.group(2).trim();
            } else {
                originalValue = matcher.group(1).trim();
            }
            if (originalValue.contains("*")) {
                matcher.appendReplacement(sb, matcher.group(0));
            } else {
                String maskedValue = maskKeepFirst7(originalValue);
                String replacement = matcher.group(0).replace(originalValue, maskedValue);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public Map<String, String> flattenHeaders(Map<String, Collection<String>> headers) {
        Map<String, String> flatMap = new HashMap<>();
        Set<String> sensitiveKeys =
                new HashSet<>(loggingProperties.getSensitiveFields()); // Sử dụng loggingProperties ở đây

        for (Map.Entry<String, Collection<String>> entry : headers.entrySet()) {
            String key = entry.getKey();
            Collection<String> values = entry.getValue();
            String value = (values != null && !values.isEmpty()) ? values.iterator().next() : "";

            if (sensitiveKeys.contains(key.toLowerCase())) {
                flatMap.put(key, maskValue(value));
            } else {
                flatMap.put(key, value);
            }
        }
        return flatMap;
    }

    private static String maskValue(String value) {
        if (value == null) return "";
        if (value.startsWith(BEARER)) {
            value = value.substring(BEARER.length());
        }
        if (value.length() <= VISIBLE_COUNT) {
            return value;
        }

        return value.substring(0, VISIBLE_COUNT) + "*******";
    }
}
