package com.kltn.scsms_api_service.core.entity.enumAttribute;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConditionOperator {
    EQUAL("="),
    GREATER_THAN(">"),
    GREATER_EQUAL(">="),
    LESS_THAN("<"),
    LESS_EQUAL("<="),
    IN("IN"),
    BETWEEN("BETWEEN");
    
    private final String operator;
}
