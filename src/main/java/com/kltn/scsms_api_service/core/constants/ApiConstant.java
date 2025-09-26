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
    public static final String GET_VEHICLE_BRAND_BY_ID_API = VEHICLE_BRAND_PREFIX + "/{brandId}";
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
    
    // Center management module endpoints
    public static final String CENTER_MANAGEMENT_PREFIX = "/centers";
    public static final String GET_ALL_CENTERS_API = CENTER_MANAGEMENT_PREFIX + "/get-all";
    public static final String GET_CENTER_BY_ID_API = CENTER_MANAGEMENT_PREFIX + "/{centerId}";
    public static final String GET_CENTER_WITH_BRANCHES_API = CENTER_MANAGEMENT_PREFIX + "/{centerId}/branches";
    public static final String CREATE_CENTER_API = CENTER_MANAGEMENT_PREFIX + "/create";
    public static final String UPDATE_CENTER_API = CENTER_MANAGEMENT_PREFIX + "/{centerId}/update";
    public static final String DELETE_CENTER_API = CENTER_MANAGEMENT_PREFIX + "/{centerId}/delete";

    // Branch management module endpoints
    public static final String BRANCH_MANAGEMENT_PREFIX = "/branches";
    public static final String GET_ALL_BRANCHES_API = BRANCH_MANAGEMENT_PREFIX + "/get-all";
    public static final String GET_ALL_BRANCHES_DROPDOWN_API = BRANCH_MANAGEMENT_PREFIX + "/dropdown";
    public static final String GET_BRANCH_BY_ID_API = BRANCH_MANAGEMENT_PREFIX + "/{branchId}";
    public static final String GET_BRANCHES_BY_CENTER_API = BRANCH_MANAGEMENT_PREFIX + "/center/{centerId}";
    public static final String GET_AVAILABLE_BRANCHES_API = BRANCH_MANAGEMENT_PREFIX + "/available";
    public static final String GET_BRANCHES_BY_LOCATION_API = BRANCH_MANAGEMENT_PREFIX + "/location";
    public static final String CREATE_BRANCH_API = BRANCH_MANAGEMENT_PREFIX + "/create";
    public static final String UPDATE_BRANCH_API = BRANCH_MANAGEMENT_PREFIX + "/{branchId}/update";
    public static final String DELETE_BRANCH_API = BRANCH_MANAGEMENT_PREFIX + "/{branchId}/delete";


    // Category management module endpoints
    public static final String CATEGORY_MANAGEMENT_PREFIX = "/categories";
    public static final String GET_ROOT_CATEGORIES_API = CATEGORY_MANAGEMENT_PREFIX + "/root";
    public static final String GET_ALL_CATEGORIES_API = CATEGORY_MANAGEMENT_PREFIX + "/get-all";
    public static final String GET_ALL_CATEGORIES_HIERARCHY_API = CATEGORY_MANAGEMENT_PREFIX + "/get-all-hierarchy";
    public static final String GET_ALL_CATEGORIES_BREADCRUMB_API = CATEGORY_MANAGEMENT_PREFIX + "/get-all-breadcrumb";
    public static final String GET_CATEGORY_BY_ID_API = CATEGORY_MANAGEMENT_PREFIX + "/{categoryId}";
    public static final String GET_CATEGORY_URL_BY_ID_API = CATEGORY_MANAGEMENT_PREFIX + "/{categoryId}/url";
    public static final String GET_CATEGORY_PATH_BY_ID_API = CATEGORY_MANAGEMENT_PREFIX + "/{categoryId}/path";
    public static final String GET_SUB_CATEGORY_BY_ID_API = CATEGORY_MANAGEMENT_PREFIX + "/{categoryId}/sub-categories";
    public static final String CREATE_CATEGORY_API = CATEGORY_MANAGEMENT_PREFIX + "/create";
    public static final String UPDATE_CATEGORY_API = CATEGORY_MANAGEMENT_PREFIX + "/{categoryId}/update";
    public static final String MOVE_CATEGORY_API = CATEGORY_MANAGEMENT_PREFIX + "/{categoryId}/move";
    public static final String DELETE_CATEGORY_API = CATEGORY_MANAGEMENT_PREFIX + "/{categoryId}/delete";
    public static final String VALIDATE_CATEGORY_URL_API = CATEGORY_MANAGEMENT_PREFIX + "/validate-url";
    
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
            apiPrefix + DELETE_SUPPLIER_API,
            apiPrefix + GET_ALL_CENTERS_API,
            apiPrefix + GET_CENTER_BY_ID_API,
            apiPrefix + GET_CENTER_WITH_BRANCHES_API,
            apiPrefix + CREATE_CENTER_API,
            apiPrefix + UPDATE_CENTER_API,
            apiPrefix + DELETE_CENTER_API,
            apiPrefix + GET_ALL_BRANCHES_API,
            apiPrefix + GET_ALL_BRANCHES_DROPDOWN_API,
            apiPrefix + GET_BRANCH_BY_ID_API,
            apiPrefix + GET_BRANCHES_BY_CENTER_API,
            apiPrefix + GET_AVAILABLE_BRANCHES_API,
            apiPrefix + GET_BRANCHES_BY_LOCATION_API,
            apiPrefix + CREATE_BRANCH_API,
            apiPrefix + UPDATE_BRANCH_API,
            apiPrefix + DELETE_BRANCH_API,
            apiPrefix + DELETE_SUPPLIER_API,
            apiPrefix + DELETE_VEHICLE_PROFILE_API,
            apiPrefix + GET_ROOT_CATEGORIES_API,
            apiPrefix + GET_ALL_CATEGORIES_API,
            apiPrefix + GET_ALL_CATEGORIES_HIERARCHY_API,
            apiPrefix + GET_ALL_CATEGORIES_BREADCRUMB_API,
            apiPrefix + GET_CATEGORY_BY_ID_API,
            apiPrefix + GET_CATEGORY_URL_BY_ID_API,
            apiPrefix + GET_CATEGORY_PATH_BY_ID_API,
            apiPrefix + GET_SUB_CATEGORY_BY_ID_API,
            apiPrefix + CREATE_CATEGORY_API,
            apiPrefix + UPDATE_CATEGORY_API,
            apiPrefix + MOVE_CATEGORY_API,
            apiPrefix + DELETE_CATEGORY_API,
            apiPrefix + VALIDATE_CATEGORY_URL_API
        );
    }
}
