package com.kltn.scsms_api_service.core.constants;

import org.springframework.beans.factory.annotation.Value;

public class ApiConstant {
    
    @Value("${server.servlet.context-path}")
    private static String API_PREFIX;
    
    // Auth module endpoints (Google integration)
    public static final String AUTH_PREFIX = "/auth";
    public static final String LOGIN_API = AUTH_PREFIX + "/login";
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
    
}
