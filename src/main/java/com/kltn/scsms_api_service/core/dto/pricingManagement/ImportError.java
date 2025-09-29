package com.kltn.scsms_api_service.core.dto.pricingManagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportError {
    private int row;
    private String message;
}
