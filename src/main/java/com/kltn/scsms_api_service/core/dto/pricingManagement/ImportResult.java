package com.kltn.scsms_api_service.core.dto.pricingManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult {
    private int totalRows;
    private int successCount;
    private List<ImportError> errors = new ArrayList<>();
    
    public void incrementSuccessCount() {
        this.successCount++;
    }
    
    public void addError(int row, String message) {
        this.errors.add(new ImportError(row, message));
    }
    
    public int getErrorCount() {
        return errors.size();
    }
    
    // Additional methods needed by service layer
    public void setSuccess(boolean success) {
        // For backwards compatibility - success is determined by error count
    }
    
    public void setMessage(String message) {
        // Add message as first error if it's an error message, otherwise ignore
        if (message != null && message.toLowerCase().contains("error")) {
            this.errors.add(new ImportError(0, message));
        }
    }
    
    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }
    
    public void setSuccessRows(int successRows) {
        this.successCount = successRows;
    }
    
    public void setFailedRows(int failedRows) {
        // Calculated from totalRows - successCount, so we don't need to store it
    }
    
    public int getFailedRows() {
        return totalRows - successCount;
    }
    
    public boolean isSuccess() {
        return errors.isEmpty();
    }
}
