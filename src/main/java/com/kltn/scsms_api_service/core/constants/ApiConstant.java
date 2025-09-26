package com.kltn.scsms_api_service.core.constants;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class ApiConstant {
    
    @Value("${server.servlet.context-path}")
    private static String API_PREFIX;
    
    // Auth module endpoints (Google integration)
    public static final String AUTH_PREFIX = "/auth";
    public static final String LOGIN_API = AUTH_PREFIX + "/login";
    public static final String REGISTER_API = AUTH_PREFIX + "/register";
    public static final String REFRESH_TOKEN_API = AUTH_PREFIX + "/refresh-token";
    public static final String LOGOUT_API = AUTH_PREFIX + "/logout";
    public static final String VERIFY_TOKEN_API = AUTH_PREFIX + "/verify-token";
    public static final String CHANGE_PASSWORD_API = AUTH_PREFIX + "/change-password";
    public static final String FORGOT_PASSWORD_API = AUTH_PREFIX + "/forgot-password";
    public static final String RESET_PASSWORD_API = AUTH_PREFIX + "/reset-password";
    public static final String GOOGLE_OAUTH2_CALLBACK_API = AUTH_PREFIX + "/oauth2/callback";
    public static final String GOOGLE_OAUTH2_URL_API = AUTH_PREFIX + "/oauth2/url";
    
    // User management module endpoints
    public static final String USER_MANAGEMENT_PREFIX = "/users";
    public static final String GET_ALL_USERS_API = USER_MANAGEMENT_PREFIX + "/get-all";
    public static final String GET_USER_BY_ID_API = USER_MANAGEMENT_PREFIX + "/{userId}";
    public static final String CREATE_USER_API = USER_MANAGEMENT_PREFIX + "/create";
    public static final String UPDATE_USER_API = USER_MANAGEMENT_PREFIX + "/{userId}/update";
    public static final String DELETE_USER_API = USER_MANAGEMENT_PREFIX + "/{userId}/delete";
    public static final String RESET_USER_PASSWORD_API = USER_MANAGEMENT_PREFIX + "/{userId}/reset-password";
    public static final String ACTIVATE_USER_API = USER_MANAGEMENT_PREFIX + "/{userId}/activate";
    public static final String DEACTIVATE_USER_API = USER_MANAGEMENT_PREFIX + "/{userId}/deactivate";
    
    // Role management module endpoints
    public static final String ROLE_MANAGEMENT_PREFIX = "/roles";
    public static final String GET_ALL_ROLES_API = ROLE_MANAGEMENT_PREFIX + "/get-all";
    public static final String GET_ROLE_BY_ID_API = ROLE_MANAGEMENT_PREFIX + "/{roleId}";
    public static final String CREATE_ROLE_API = ROLE_MANAGEMENT_PREFIX + "/create";
    public static final String UPDATE_ROLE_API = ROLE_MANAGEMENT_PREFIX + "/{roleId}/update";
    public static final String DELETE_ROLE_API = ROLE_MANAGEMENT_PREFIX + "/{roleId}/delete";
    public static final String ASSIGN_ROLE_TO_USER_API = ROLE_MANAGEMENT_PREFIX + "/{roleId}/assign/{userId}";
    public static final String UNASSIGN_ROLE_FROM_USER_API = ROLE_MANAGEMENT_PREFIX + "/{roleId}/unassign/{userId}";
    
    // Vehicle management module endpoints
    public static final String VEHICLE_MANAGEMENT_PREFIX = "/vehicles";
    public static final String VEHICLE_BRAND_PREFIX = VEHICLE_MANAGEMENT_PREFIX + "/brands";
    public static final String VEHICLE_TYPE_PREFIX = VEHICLE_MANAGEMENT_PREFIX + "/types";
    public static final String VEHICLE_MODEL_PREFIX = VEHICLE_MANAGEMENT_PREFIX + "/models";
    public static final String VEHICLE_PROFILE_PREFIX = VEHICLE_MANAGEMENT_PREFIX + "/profiles";
    // Vehicle Brand APIs
    public static final String GET_ALL_VEHICLE_BRANDS_API = VEHICLE_BRAND_PREFIX + "/get-all";
    public static final String GET_ALL_VEHICLE_BRANDS_DROPDOWN_API = VEHICLE_BRAND_PREFIX + "/dropdown";
    public static final String GET_VEHICLE_BRAND_BY_ID_API =  VEHICLE_BRAND_PREFIX + "/{brandId}";
    public static final String CREATE_VEHICLE_BRAND_API = VEHICLE_BRAND_PREFIX + "/create";
    public static final String UPDATE_VEHICLE_BRAND_API = VEHICLE_BRAND_PREFIX + "/{brandId}/update";
    public static final String DELETE_VEHICLE_BRAND_API = VEHICLE_BRAND_PREFIX + "/{brandId}/delete";
    // Vehicle Type APIs
    public static final String GET_ALL_VEHICLE_TYPES_API = VEHICLE_TYPE_PREFIX + "/get-all";
    public static final String GET_ALL_VEHICLE_TYPES_DROPDOWN_API = VEHICLE_TYPE_PREFIX + "/dropdown";
    public static final String GET_VEHICLE_TYPE_BY_ID_API = VEHICLE_TYPE_PREFIX + "/{typeId}";
    public static final String CREATE_VEHICLE_TYPE_API = VEHICLE_TYPE_PREFIX + "/create";
    public static final String UPDATE_VEHICLE_TYPE_API = VEHICLE_TYPE_PREFIX + "/{typeId}/update";
    public static final String DELETE_VEHICLE_TYPE_API = VEHICLE_TYPE_PREFIX + "/{typeId}/delete";
    // Vehicle Model APIs
    public static final String GET_ALL_VEHICLE_MODELS_API = VEHICLE_MODEL_PREFIX + "/get-all";
    public static final String GET_ALL_VEHICLE_MODELS_DROPDOWN_API = VEHICLE_MODEL_PREFIX + "/dropdown";
    public static final String GET_VEHICLE_MODEL_BY_ID_API = VEHICLE_MODEL_PREFIX + "/{modelId}";
    public static final String CREATE_VEHICLE_MODEL_API = VEHICLE_MODEL_PREFIX + "/create";
    public static final String UPDATE_VEHICLE_MODEL_API = VEHICLE_MODEL_PREFIX + "/{modelId}/update";
    public static final String DELETE_VEHICLE_MODEL_API = VEHICLE_MODEL_PREFIX + "/{modelId}/delete";
    // Vehicle Profile APIs
    public static final String GET_ALL_VEHICLE_PROFILES_API = VEHICLE_PROFILE_PREFIX + "/get-all";
    public static final String GET_VEHICLE_PROFILE_BY_ID_API = VEHICLE_PROFILE_PREFIX + "/{profileId}";
    public static final String CREATE_VEHICLE_PROFILE_API = VEHICLE_PROFILE_PREFIX + "/create";
    public static final String UPDATE_VEHICLE_PROFILE_API = VEHICLE_PROFILE_PREFIX + "/{profileId}/update";
    public static final String DELETE_VEHICLE_PROFILE_API = VEHICLE_PROFILE_PREFIX + "/{profileId}/delete";
    
    // Supplier management module endpoints
    public static final String SUPPLIER_MANAGEMENT_PREFIX = "/suppliers";
    public static final String GET_ALL_SUPPLIERS_API = SUPPLIER_MANAGEMENT_PREFIX + "/get-all";
    public static final String GET_SUPPLIER_BY_ID_API = SUPPLIER_MANAGEMENT_PREFIX + "/{supplierId}";
    public static final String CREATE_SUPPLIER_API = SUPPLIER_MANAGEMENT_PREFIX + "/create";
    public static final String UPDATE_SUPPLIER_API = SUPPLIER_MANAGEMENT_PREFIX + "/{supplierId}/update";
    public static final String DELETE_SUPPLIER_API = SUPPLIER_MANAGEMENT_PREFIX + "/{supplierId}/delete";
    
    
    public static List<String> PROTECTED_PATHS(String apiPrefix) {
        return List.of(
            apiPrefix + VERIFY_TOKEN_API,
            apiPrefix + GET_ALL_USERS_API,
            apiPrefix + GET_USER_BY_ID_API,
            apiPrefix + CREATE_USER_API,
            apiPrefix + UPDATE_USER_API,
            apiPrefix + DELETE_USER_API,
            apiPrefix + RESET_USER_PASSWORD_API,
            apiPrefix + ACTIVATE_USER_API,
            apiPrefix + DEACTIVATE_USER_API,
            apiPrefix + GET_ALL_ROLES_API,
            apiPrefix + GET_ROLE_BY_ID_API,
            apiPrefix + CREATE_ROLE_API,
            apiPrefix + UPDATE_ROLE_API,
            apiPrefix + DELETE_ROLE_API,
            apiPrefix + ASSIGN_ROLE_TO_USER_API,
            apiPrefix + UNASSIGN_ROLE_FROM_USER_API,
            apiPrefix + GET_ALL_VEHICLE_BRANDS_API,
            apiPrefix + GET_ALL_VEHICLE_BRANDS_DROPDOWN_API,
            apiPrefix + GET_VEHICLE_BRAND_BY_ID_API,
            apiPrefix + CREATE_VEHICLE_BRAND_API,
            apiPrefix + UPDATE_VEHICLE_BRAND_API,
            apiPrefix + DELETE_VEHICLE_BRAND_API,
            apiPrefix + GET_ALL_VEHICLE_TYPES_API,
            apiPrefix + GET_ALL_VEHICLE_TYPES_DROPDOWN_API,
            apiPrefix + GET_VEHICLE_TYPE_BY_ID_API,
            apiPrefix + CREATE_VEHICLE_TYPE_API,
            apiPrefix + UPDATE_VEHICLE_TYPE_API,
            apiPrefix + DELETE_VEHICLE_TYPE_API,
            apiPrefix + GET_ALL_VEHICLE_MODELS_API,
            apiPrefix + GET_ALL_VEHICLE_MODELS_DROPDOWN_API,
            apiPrefix + GET_VEHICLE_MODEL_BY_ID_API,
            apiPrefix + CREATE_VEHICLE_MODEL_API,
            apiPrefix + UPDATE_VEHICLE_MODEL_API,
            apiPrefix + DELETE_VEHICLE_MODEL_API,
            apiPrefix + GET_ALL_VEHICLE_PROFILES_API,
            apiPrefix + GET_VEHICLE_PROFILE_BY_ID_API,
            apiPrefix + CREATE_VEHICLE_PROFILE_API,
            apiPrefix + UPDATE_VEHICLE_PROFILE_API,
            apiPrefix + DELETE_VEHICLE_PROFILE_API,
            apiPrefix + GET_ALL_SUPPLIERS_API,
            apiPrefix + GET_SUPPLIER_BY_ID_API,
            apiPrefix + CREATE_SUPPLIER_API,
            apiPrefix + UPDATE_SUPPLIER_API,
            apiPrefix + DELETE_SUPPLIER_API
        );
    }
}
