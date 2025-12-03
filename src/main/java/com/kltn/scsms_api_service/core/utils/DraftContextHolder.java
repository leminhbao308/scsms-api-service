package com.kltn.scsms_api_service.core.utils;

import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetCustomerVehiclesResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetBranchesResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetServicesResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.AvailabilityResponse;

import java.util.UUID;

/**
 * ThreadLocal holder để lưu draft context và function responses trong request
 * Giúp function configs có thể access draft_id và controller có thể lấy function responses
 */
public class DraftContextHolder {
    
    private static final ThreadLocal<UUID> draftIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> sessionIdHolder = new ThreadLocal<>();
    
    // Function responses - lưu để controller có thể lấy vehicle_id, branch_id, service_name, bay_id
    private static final ThreadLocal<GetCustomerVehiclesResponse> vehiclesResponseHolder = new ThreadLocal<>();
    private static final ThreadLocal<GetBranchesResponse> branchesResponseHolder = new ThreadLocal<>();
    private static final ThreadLocal<GetServicesResponse> servicesResponseHolder = new ThreadLocal<>();
    private static final ThreadLocal<AvailabilityResponse> availabilityResponseHolder = new ThreadLocal<>();
    
    /**
     * Set draft_id cho current thread
     */
    public static void setDraftId(UUID draftId) {
        draftIdHolder.set(draftId);
    }
    
    /**
     * Get draft_id từ current thread
     */
    public static UUID getDraftId() {
        return draftIdHolder.get();
    }
    
    /**
     * Set session_id cho current thread
     */
    public static void setSessionId(String sessionId) {
        sessionIdHolder.set(sessionId);
    }
    
    /**
     * Get session_id từ current thread
     */
    public static String getSessionId() {
        return sessionIdHolder.get();
    }
    
    /**
     * Lưu GetCustomerVehiclesResponse để controller có thể lấy vehicle_id
     */
    public static void setVehiclesResponse(GetCustomerVehiclesResponse response) {
        vehiclesResponseHolder.set(response);
    }
    
    /**
     * Lấy GetCustomerVehiclesResponse từ current thread
     */
    public static GetCustomerVehiclesResponse getVehiclesResponse() {
        return vehiclesResponseHolder.get();
    }
    
    /**
     * Lưu GetBranchesResponse để controller có thể lấy branch_id
     */
    public static void setBranchesResponse(GetBranchesResponse response) {
        branchesResponseHolder.set(response);
    }
    
    /**
     * Lấy GetBranchesResponse từ current thread
     */
    public static GetBranchesResponse getBranchesResponse() {
        return branchesResponseHolder.get();
    }
    
    /**
     * Lưu GetServicesResponse để controller có thể lấy service_name
     */
    public static void setServicesResponse(GetServicesResponse response) {
        servicesResponseHolder.set(response);
    }
    
    /**
     * Lấy GetServicesResponse từ current thread
     */
    public static GetServicesResponse getServicesResponse() {
        return servicesResponseHolder.get();
    }
    
    /**
     * Lưu AvailabilityResponse để controller có thể lấy bay_id
     */
    public static void setAvailabilityResponse(AvailabilityResponse response) {
        availabilityResponseHolder.set(response);
    }
    
    /**
     * Lấy AvailabilityResponse từ current thread
     */
    public static AvailabilityResponse getAvailabilityResponse() {
        return availabilityResponseHolder.get();
    }
    
    /**
     * Clear context sau khi request hoàn thành
     */
    public static void clear() {
        draftIdHolder.remove();
        sessionIdHolder.remove();
        vehiclesResponseHolder.remove();
        branchesResponseHolder.remove();
        servicesResponseHolder.remove();
        availabilityResponseHolder.remove();
    }
}

