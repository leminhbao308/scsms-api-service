package com.kltn.scsms_api_service.constants;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;

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
        public static final String GET_ALL_VEHICLE_PROFILES_BY_OWNER_ID_API = VEHICLE_PROFILE_PREFIX
                        + "/owner/{ownerId}/get-all";
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

        // Center Business Hours management module endpoints
        public static final String CENTER_BUSINESS_HOURS_PREFIX = "/center-business-hours";
        public static final String GET_ALL_CENTER_BUSINESS_HOURS_API = CENTER_BUSINESS_HOURS_PREFIX
                        + "/center/{centerId}";
        public static final String GET_CENTER_BUSINESS_HOURS_BY_ID_API = CENTER_BUSINESS_HOURS_PREFIX
                        + "/{businessHoursId}";
        public static final String CREATE_CENTER_BUSINESS_HOURS_API = CENTER_BUSINESS_HOURS_PREFIX + "/create";
        public static final String UPDATE_CENTER_BUSINESS_HOURS_API = CENTER_BUSINESS_HOURS_PREFIX
                        + "/{businessHoursId}/update";
        public static final String DELETE_CENTER_BUSINESS_HOURS_API = CENTER_BUSINESS_HOURS_PREFIX
                        + "/{businessHoursId}/delete";
        public static final String UPDATE_CENTER_BUSINESS_HOURS_STATUS_API = CENTER_BUSINESS_HOURS_PREFIX
                        + "/{businessHoursId}/status";

        // Center Social Media management module endpoints
        public static final String CENTER_SOCIAL_MEDIA_PREFIX = "/center-social-media";
        public static final String GET_ALL_CENTER_SOCIAL_MEDIA_API = CENTER_SOCIAL_MEDIA_PREFIX + "/center/{centerId}";
        public static final String GET_CENTER_SOCIAL_MEDIA_BY_ID_API = CENTER_SOCIAL_MEDIA_PREFIX + "/{socialMediaId}";
        public static final String CREATE_CENTER_SOCIAL_MEDIA_API = CENTER_SOCIAL_MEDIA_PREFIX + "/create";
        public static final String UPDATE_CENTER_SOCIAL_MEDIA_API = CENTER_SOCIAL_MEDIA_PREFIX
                        + "/{socialMediaId}/update";
        public static final String DELETE_CENTER_SOCIAL_MEDIA_API = CENTER_SOCIAL_MEDIA_PREFIX
                        + "/{socialMediaId}/delete";
        public static final String UPDATE_CENTER_SOCIAL_MEDIA_STATUS_API = CENTER_SOCIAL_MEDIA_PREFIX
                        + "/{socialMediaId}/status";

        // Branch management module endpoints
        public static final String BRANCH_MANAGEMENT_PREFIX = "/branches";
        public static final String GET_ALL_BRANCHES_API = BRANCH_MANAGEMENT_PREFIX + "/get-all";
        public static final String GET_ALL_BRANCHES_DROPDOWN_API = BRANCH_MANAGEMENT_PREFIX + "/dropdown";
        public static final String GET_BRANCH_BY_ID_API = BRANCH_MANAGEMENT_PREFIX + "/{branchId}";
        public static final String GET_BRANCHES_BY_CENTER_API = BRANCH_MANAGEMENT_PREFIX + "/center/{centerId}";
        public static final String CREATE_BRANCH_API = BRANCH_MANAGEMENT_PREFIX + "/create";
        public static final String UPDATE_BRANCH_API = BRANCH_MANAGEMENT_PREFIX + "/{branchId}/update";
        public static final String UPDATE_BRANCH_STATUS_API = BRANCH_MANAGEMENT_PREFIX + "/{branchId}/status";
        public static final String DELETE_BRANCH_API = BRANCH_MANAGEMENT_PREFIX + "/{branchId}/delete";

        // Product Type management module endpoints
        public static final String PRODUCT_TYPE_MANAGEMENT_PREFIX = "/product-types";
        public static final String GET_ALL_PRODUCT_TYPES_API = PRODUCT_TYPE_MANAGEMENT_PREFIX + "/get-all";
        public static final String GET_PRODUCT_TYPE_BY_ID_API = PRODUCT_TYPE_MANAGEMENT_PREFIX + "/{productTypeId}";
        public static final String GET_PRODUCT_TYPE_BY_CODE_API = PRODUCT_TYPE_MANAGEMENT_PREFIX
                        + "/code/{productTypeCode}";
        public static final String GET_PRODUCT_TYPES_BY_CATEGORY_API = PRODUCT_TYPE_MANAGEMENT_PREFIX
                        + "/category/{categoryId}";
        public static final String GET_ACTIVE_PRODUCT_TYPES_API = PRODUCT_TYPE_MANAGEMENT_PREFIX + "/active";
        public static final String CREATE_PRODUCT_TYPE_API = PRODUCT_TYPE_MANAGEMENT_PREFIX + "/create";
        public static final String UPDATE_PRODUCT_TYPE_API = PRODUCT_TYPE_MANAGEMENT_PREFIX + "/{productTypeId}/update";
        public static final String UPDATE_PRODUCT_TYPE_STATUS_API = PRODUCT_TYPE_MANAGEMENT_PREFIX
                        + "/{productTypeId}/status";
        public static final String DELETE_PRODUCT_TYPE_API = PRODUCT_TYPE_MANAGEMENT_PREFIX + "/{productTypeId}/delete";
        public static final String VALIDATE_PRODUCT_TYPE_CODE_API = PRODUCT_TYPE_MANAGEMENT_PREFIX + "/validate-code";
        public static final String GET_PRODUCT_TYPE_STATISTICS_API = PRODUCT_TYPE_MANAGEMENT_PREFIX + "/statistics";

        // Media management module endpoints
        public static final String MEDIA_MANAGEMENT_PREFIX = "/media";
        public static final String GET_ALL_MEDIA_API = MEDIA_MANAGEMENT_PREFIX + "/get-all";
        public static final String GET_MEDIA_BY_ID_API = MEDIA_MANAGEMENT_PREFIX + "/{mediaId}";
        public static final String GET_MEDIA_BY_ENTITY_API = MEDIA_MANAGEMENT_PREFIX
                        + "/entity/{entityType}/{entityId}";
        public static final String GET_MAIN_MEDIA_BY_ENTITY_API = MEDIA_MANAGEMENT_PREFIX
                        + "/entity/{entityType}/{entityId}/main";
        public static final String GET_MEDIA_BY_TYPE_API = MEDIA_MANAGEMENT_PREFIX + "/type/{mediaType}";
        public static final String CREATE_MEDIA_API = MEDIA_MANAGEMENT_PREFIX + "/create";
        public static final String UPDATE_MEDIA_API = MEDIA_MANAGEMENT_PREFIX + "/{mediaId}/update";
        public static final String UPDATE_MEDIA_MAIN_STATUS_API = MEDIA_MANAGEMENT_PREFIX + "/{mediaId}/main-status";
        public static final String DELETE_MEDIA_API = MEDIA_MANAGEMENT_PREFIX + "/{mediaId}/delete";
        public static final String BULK_UPDATE_MEDIA_ORDER_API = MEDIA_MANAGEMENT_PREFIX + "/bulk-update-order";
        public static final String VALIDATE_MEDIA_URL_API = MEDIA_MANAGEMENT_PREFIX + "/validate-url";
        public static final String GET_MEDIA_STATISTICS_API = MEDIA_MANAGEMENT_PREFIX + "/statistics";

        // Product Attribute management module endpoints
        public static final String PRODUCT_ATTRIBUTE_MANAGEMENT_PREFIX = "/product-attributes";
        public static final String GET_ALL_PRODUCT_ATTRIBUTES_API = PRODUCT_ATTRIBUTE_MANAGEMENT_PREFIX + "/get-all";
        public static final String GET_PRODUCT_ATTRIBUTE_BY_ID_API = PRODUCT_ATTRIBUTE_MANAGEMENT_PREFIX
                        + "/{attributeId}";
        public static final String GET_PRODUCT_ATTRIBUTE_BY_CODE_API = PRODUCT_ATTRIBUTE_MANAGEMENT_PREFIX
                        + "/code/{attributeCode}";
        public static final String GET_PRODUCT_ATTRIBUTES_BY_DATA_TYPE_API = PRODUCT_ATTRIBUTE_MANAGEMENT_PREFIX
                        + "/data-type/{dataType}";
        public static final String GET_REQUIRED_PRODUCT_ATTRIBUTES_API = PRODUCT_ATTRIBUTE_MANAGEMENT_PREFIX
                        + "/required";
        public static final String GET_ACTIVE_PRODUCT_ATTRIBUTES_API = PRODUCT_ATTRIBUTE_MANAGEMENT_PREFIX + "/active";
        public static final String CREATE_PRODUCT_ATTRIBUTE_API = PRODUCT_ATTRIBUTE_MANAGEMENT_PREFIX + "/create";
        public static final String UPDATE_PRODUCT_ATTRIBUTE_API = PRODUCT_ATTRIBUTE_MANAGEMENT_PREFIX
                        + "/{attributeId}/update";
        public static final String UPDATE_PRODUCT_ATTRIBUTE_STATUS_API = PRODUCT_ATTRIBUTE_MANAGEMENT_PREFIX
                        + "/{attributeId}/status";
        public static final String DELETE_PRODUCT_ATTRIBUTE_API = PRODUCT_ATTRIBUTE_MANAGEMENT_PREFIX
                        + "/{attributeId}/delete";
        public static final String VALIDATE_PRODUCT_ATTRIBUTE_CODE_API = PRODUCT_ATTRIBUTE_MANAGEMENT_PREFIX
                        + "/validate-code";
        public static final String GET_PRODUCT_ATTRIBUTE_STATISTICS_API = PRODUCT_ATTRIBUTE_MANAGEMENT_PREFIX
                        + "/statistics";

        // Product Attribute Value management module endpoints
        public static final String PRODUCT_ATTRIBUTE_VALUE_MANAGEMENT_PREFIX = "/product-attribute-values";
        public static final String ADD_PRODUCT_ATTRIBUTE_VALUE_API = PRODUCT_ATTRIBUTE_VALUE_MANAGEMENT_PREFIX
                        + "/products/{productId}/attributes/add";
        public static final String GET_PRODUCT_ATTRIBUTE_VALUE_API = PRODUCT_ATTRIBUTE_VALUE_MANAGEMENT_PREFIX
                        + "/products/{productId}/attributes/{attributeId}";
        public static final String GET_PRODUCT_ATTRIBUTE_VALUES_API = PRODUCT_ATTRIBUTE_VALUE_MANAGEMENT_PREFIX
                        + "/products/{productId}/attributes/all";
        public static final String GET_PRODUCTS_BY_ATTRIBUTE_API = PRODUCT_ATTRIBUTE_VALUE_MANAGEMENT_PREFIX
                        + "/attributes/{attributeId}/products";
        public static final String UPDATE_PRODUCT_ATTRIBUTE_VALUE_API = PRODUCT_ATTRIBUTE_VALUE_MANAGEMENT_PREFIX
                        + "/products/{productId}/attributes/{attributeId}/update";
        public static final String DELETE_PRODUCT_ATTRIBUTE_VALUE_API = PRODUCT_ATTRIBUTE_VALUE_MANAGEMENT_PREFIX
                        + "/products/{productId}/attributes/{attributeId}/delete";
        public static final String BULK_UPDATE_PRODUCT_ATTRIBUTE_VALUES_API = PRODUCT_ATTRIBUTE_VALUE_MANAGEMENT_PREFIX
                        + "/bulk-update";
        public static final String BULK_UPDATE_PRODUCT_ATTRIBUTE_VALUES_BY_PRODUCT_API = PRODUCT_ATTRIBUTE_VALUE_MANAGEMENT_PREFIX
                        + "/products/{productId}/bulk-update";
        public static final String SEARCH_PRODUCTS_BY_ATTRIBUTE_VALUE_API = PRODUCT_ATTRIBUTE_VALUE_MANAGEMENT_PREFIX
                        + "/attributes/{attributeId}/search";
        public static final String SEARCH_PRODUCTS_BY_ATTRIBUTE_VALUE_RANGE_API = PRODUCT_ATTRIBUTE_VALUE_MANAGEMENT_PREFIX
                        + "/attributes/{attributeId}/search-range";
        public static final String GET_PRODUCT_ATTRIBUTE_VALUE_COUNT_API = PRODUCT_ATTRIBUTE_VALUE_MANAGEMENT_PREFIX
                        + "/products/{productId}/attributes/count";
        public static final String GET_PRODUCT_COUNT_BY_ATTRIBUTE_API = PRODUCT_ATTRIBUTE_VALUE_MANAGEMENT_PREFIX
                        + "/attributes/{attributeId}/count";

        // Product management module endpoints
        public static final String PRODUCT_MANAGEMENT_PREFIX = "/products";
        public static final String GET_ALL_PRODUCTS_API = PRODUCT_MANAGEMENT_PREFIX + "/get-all";
        public static final String GET_PRODUCT_BY_ID_API = PRODUCT_MANAGEMENT_PREFIX + "/{productId}";
        public static final String GET_PRODUCT_BY_URL_API = PRODUCT_MANAGEMENT_PREFIX + "/url/{productUrl}";
        public static final String GET_PRODUCTS_BY_PRODUCT_TYPE_API = PRODUCT_MANAGEMENT_PREFIX
                        + "/product-type/{productTypeId}";
        public static final String GET_PRODUCTS_BY_SUPPLIER_API = PRODUCT_MANAGEMENT_PREFIX + "/supplier/{supplierId}";
        public static final String SEARCH_PRODUCTS_API = PRODUCT_MANAGEMENT_PREFIX + "/search";
        public static final String GET_FEATURED_PRODUCTS_API = PRODUCT_MANAGEMENT_PREFIX + "/featured";
        public static final String CREATE_PRODUCT_API = PRODUCT_MANAGEMENT_PREFIX + "/create";
        public static final String UPDATE_PRODUCT_API = PRODUCT_MANAGEMENT_PREFIX + "/{productId}/update";
        public static final String UPDATE_PRODUCT_STATUS_API = PRODUCT_MANAGEMENT_PREFIX + "/{productId}/status";
        public static final String DELETE_PRODUCT_API = PRODUCT_MANAGEMENT_PREFIX + "/{productId}/delete";

        // Product Media management endpoints
        public static final String GET_PRODUCT_IMAGES_API = PRODUCT_MANAGEMENT_PREFIX + "/{productId}/images";
        public static final String UPLOAD_PRODUCT_IMAGE_API = PRODUCT_MANAGEMENT_PREFIX + "/{productId}/images/upload";
        public static final String ADD_PRODUCT_IMAGE_API = PRODUCT_MANAGEMENT_PREFIX + "/{productId}/images/add";
        public static final String UPDATE_PRODUCT_IMAGE_API = PRODUCT_MANAGEMENT_PREFIX
                        + "/{productId}/images/{mediaId}/update";
        public static final String DELETE_PRODUCT_IMAGE_API = PRODUCT_MANAGEMENT_PREFIX
                        + "/{productId}/images/{mediaId}/delete";
        public static final String SET_MAIN_PRODUCT_IMAGE_API = PRODUCT_MANAGEMENT_PREFIX
                        + "/{productId}/images/{mediaId}/set-main";
        public static final String REORDER_PRODUCT_IMAGES_API = PRODUCT_MANAGEMENT_PREFIX
                        + "/{productId}/images/reorder";

        // Service management module endpoints
        public static final String SERVICE_MANAGEMENT_PREFIX = "/services";
        public static final String GET_ALL_SERVICES_API = SERVICE_MANAGEMENT_PREFIX + "/get-all";
        public static final String GET_SERVICE_BY_ID_API = SERVICE_MANAGEMENT_PREFIX + "/{serviceId}";
        public static final String GET_SERVICES_BY_CATEGORY_API = SERVICE_MANAGEMENT_PREFIX + "/category/{categoryId}";
        public static final String GET_SERVICES_BY_TYPE_API = SERVICE_MANAGEMENT_PREFIX + "/type/{serviceTypeId}";
        public static final String GET_SERVICES_BY_SKILL_LEVEL_API = SERVICE_MANAGEMENT_PREFIX
                        + "/skill-level/{skillLevel}";
        public static final String SEARCH_SERVICES_API = SERVICE_MANAGEMENT_PREFIX + "/search";
        public static final String CREATE_SERVICE_API = SERVICE_MANAGEMENT_PREFIX + "/create";
        public static final String UPDATE_SERVICE_API = SERVICE_MANAGEMENT_PREFIX + "/{serviceId}/update";
        public static final String DELETE_SERVICE_API = SERVICE_MANAGEMENT_PREFIX + "/{serviceId}/delete";

        // Service Pricing management endpoints
        public static final String GET_SERVICE_PRICING_API = SERVICE_MANAGEMENT_PREFIX + "/{serviceId}/pricing";
        public static final String GET_SERVICE_PRICING_INFO_API = SERVICE_MANAGEMENT_PREFIX
                        + "/{serviceId}/pricing-info";
        public static final String RECALCULATE_SERVICE_BASE_PRICE_API = SERVICE_MANAGEMENT_PREFIX
                        + "/{serviceId}/recalculate-base-price";
        public static final String UPDATE_SERVICE_LABOR_COST_API = SERVICE_MANAGEMENT_PREFIX
                        + "/{serviceId}/update-labor-cost";

        // Pricing Management endpoints
        public static final String PRICING_PREFIX = "/pricing";
        public static final String GET_SERVICE_PRICING_BY_PRICEBOOK_API = PRICING_PREFIX + "/services/{serviceId}";
        public static final String RECALCULATE_SERVICE_PRICING_API = PRICING_PREFIX
                        + "/services/{serviceId}/recalculate";

        // Service Process management module endpoints
        public static final String SERVICE_PROCESS_MANAGEMENT_PREFIX = "/service-processes";
        public static final String GET_ALL_SERVICE_PROCESSES_API = SERVICE_PROCESS_MANAGEMENT_PREFIX + "/get-all";
        public static final String GET_SERVICE_PROCESS_BY_ID_API = SERVICE_PROCESS_MANAGEMENT_PREFIX + "/{processId}";
        public static final String GET_SERVICE_PROCESS_BY_CODE_API = SERVICE_PROCESS_MANAGEMENT_PREFIX + "/code/{code}";
        public static final String GET_SERVICE_PROCESS_BY_SERVICE_API = SERVICE_PROCESS_MANAGEMENT_PREFIX
                        + "/service/{serviceId}";
        public static final String GET_DEFAULT_SERVICE_PROCESS_API = SERVICE_PROCESS_MANAGEMENT_PREFIX + "/default";
        public static final String GET_ALL_ACTIVE_SERVICE_PROCESSES_API = SERVICE_PROCESS_MANAGEMENT_PREFIX + "/active";
        public static final String CREATE_SERVICE_PROCESS_API = SERVICE_PROCESS_MANAGEMENT_PREFIX + "/create";
        public static final String UPDATE_SERVICE_PROCESS_API = SERVICE_PROCESS_MANAGEMENT_PREFIX
                        + "/{processId}/update";
        public static final String DELETE_SERVICE_PROCESS_API = SERVICE_PROCESS_MANAGEMENT_PREFIX
                        + "/{processId}/delete";
        public static final String SET_DEFAULT_SERVICE_PROCESS_API = SERVICE_PROCESS_MANAGEMENT_PREFIX
                        + "/{processId}/set-default";

        // Service Process Step management module endpoints
        public static final String GET_SERVICE_PROCESS_STEPS_API = SERVICE_PROCESS_MANAGEMENT_PREFIX
                        + "/{processId}/steps";
        public static final String GET_SERVICE_PROCESS_STEP_BY_ID_API = SERVICE_PROCESS_MANAGEMENT_PREFIX
                        + "/steps/{stepId}";
        public static final String ADD_SERVICE_PROCESS_STEP_API = SERVICE_PROCESS_MANAGEMENT_PREFIX
                        + "/{processId}/steps";
        public static final String UPDATE_SERVICE_PROCESS_STEP_API = SERVICE_PROCESS_MANAGEMENT_PREFIX
                        + "/steps/{stepId}/update";
        public static final String DELETE_SERVICE_PROCESS_STEP_API = SERVICE_PROCESS_MANAGEMENT_PREFIX
                        + "/steps/{stepId}/delete";

        // Service Process Step Product management module endpoints
        public static final String GET_SERVICE_PROCESS_STEP_PRODUCTS_API = SERVICE_PROCESS_MANAGEMENT_PREFIX
                        + "/steps/{stepId}/products";
        public static final String GET_SERVICE_PROCESS_STEP_PRODUCT_BY_ID_API = SERVICE_PROCESS_MANAGEMENT_PREFIX
                        + "/step-products/{productId}";
        public static final String ADD_SERVICE_PROCESS_STEP_PRODUCT_API = SERVICE_PROCESS_MANAGEMENT_PREFIX
                        + "/steps/{stepId}/products";
        public static final String UPDATE_SERVICE_PROCESS_STEP_PRODUCT_API = SERVICE_PROCESS_MANAGEMENT_PREFIX
                        + "/step-products/{productId}/update";
        public static final String DELETE_SERVICE_PROCESS_STEP_PRODUCT_API = SERVICE_PROCESS_MANAGEMENT_PREFIX
                        + "/step-products/{productId}/delete";

        // Category management module endpoints
        public static final String CATEGORY_MANAGEMENT_PREFIX = "/categories";
        public static final String GET_ALL_CATEGORIES_API = CATEGORY_MANAGEMENT_PREFIX + "/get-all";
        public static final String GET_ALL_CATEGORIES_HIERARCHY_API = CATEGORY_MANAGEMENT_PREFIX + "/get-all-hierarchy";
        public static final String GET_CATEGORY_BY_ID_API = CATEGORY_MANAGEMENT_PREFIX + "/{categoryId}";
        public static final String GET_CATEGORY_PATH_BY_ID_API = CATEGORY_MANAGEMENT_PREFIX + "/{categoryId}/path";
        public static final String GET_SUB_CATEGORY_BY_ID_API = CATEGORY_MANAGEMENT_PREFIX
                        + "/{categoryId}/sub-categories";
        public static final String CREATE_CATEGORY_API = CATEGORY_MANAGEMENT_PREFIX + "/create";
        public static final String UPDATE_CATEGORY_API = CATEGORY_MANAGEMENT_PREFIX + "/{categoryId}/update";
        public static final String UPDATE_CATEGORY_STATUS_API = CATEGORY_MANAGEMENT_PREFIX + "/{categoryId}/status";
        public static final String DELETE_CATEGORY_API = CATEGORY_MANAGEMENT_PREFIX + "/{categoryId}/delete";
        public static final String VALIDATE_CATEGORY_CODE_API = CATEGORY_MANAGEMENT_PREFIX + "/validate-code";

        // Promotion Type management module endpoints
        public static final String PROMOTION_TYPE_MANAGEMENT_PREFIX = "/promotion-types";
        public static final String GET_ALL_PROMOTION_TYPES_API = PROMOTION_TYPE_MANAGEMENT_PREFIX + "/get-all";
        public static final String GET_PROMOTION_TYPE_BY_ID_API = PROMOTION_TYPE_MANAGEMENT_PREFIX
                        + "/{promotionTypeId}";
        public static final String GET_PROMOTION_TYPE_BY_TYPE_CODE_API = PROMOTION_TYPE_MANAGEMENT_PREFIX
                        + "/code/{typeCode}";
        public static final String GET_ACTIVE_PROMOTION_TYPES_API = PROMOTION_TYPE_MANAGEMENT_PREFIX + "/active";
        public static final String SEARCH_PROMOTION_TYPES_API = PROMOTION_TYPE_MANAGEMENT_PREFIX + "/search";
        public static final String CREATE_PROMOTION_TYPE_API = PROMOTION_TYPE_MANAGEMENT_PREFIX + "/create";
        public static final String UPDATE_PROMOTION_TYPE_API = PROMOTION_TYPE_MANAGEMENT_PREFIX
                        + "/{promotionTypeId}/update";
        public static final String UPDATE_PROMOTION_TYPE_STATUS_API = PROMOTION_TYPE_MANAGEMENT_PREFIX
                        + "/{promotionTypeId}/status";
        public static final String DELETE_PROMOTION_TYPE_API = PROMOTION_TYPE_MANAGEMENT_PREFIX
                        + "/{promotionTypeId}/delete";
        public static final String ACTIVATE_PROMOTION_TYPE_API = PROMOTION_TYPE_MANAGEMENT_PREFIX
                        + "/{promotionTypeId}/activate";
        public static final String DEACTIVATE_PROMOTION_TYPE_API = PROMOTION_TYPE_MANAGEMENT_PREFIX
                        + "/{promotionTypeId}/deactivate";
        public static final String GET_PROMOTION_TYPE_STATISTICS_API = PROMOTION_TYPE_MANAGEMENT_PREFIX + "/statistics";

        // Promotion management module endpoints
        public static final String PROMOTION_MANAGEMENT_PREFIX = "/promotions";
        public static final String GET_ALL_PROMOTIONS_API = PROMOTION_MANAGEMENT_PREFIX + "/get-all";
        public static final String GET_PROMOTION_BY_ID_API = PROMOTION_MANAGEMENT_PREFIX + "/{promotionId}";
        public static final String GET_PROMOTION_BY_CODE_API = PROMOTION_MANAGEMENT_PREFIX + "/code/{promotionCode}";
        public static final String CREATE_PROMOTION_API = PROMOTION_MANAGEMENT_PREFIX + "/create";
        public static final String UPDATE_PROMOTION_API = PROMOTION_MANAGEMENT_PREFIX + "/{promotionId}/update";
        public static final String DELETE_PROMOTION_API = PROMOTION_MANAGEMENT_PREFIX + "/{promotionId}/delete";
        public static final String UPDATE_PROMOTION_STATUS_API = PROMOTION_MANAGEMENT_PREFIX + "/{promotionId}/status";
        public static final String MAKE_PROMOTION_VISIBLE_API = PROMOTION_MANAGEMENT_PREFIX
                        + "/{promotionId}/make-visible";
        public static final String MAKE_PROMOTION_INVISIBLE_API = PROMOTION_MANAGEMENT_PREFIX
                        + "/{promotionId}/make-invisible";
        public static final String RESTORE_PROMOTION_API = PROMOTION_MANAGEMENT_PREFIX + "/{promotionId}/restore";
        public static final String GET_PROMOTION_STATISTICS_API = PROMOTION_MANAGEMENT_PREFIX + "/statistics";
        public static final String GET_ACTIVE_PROMOTIONS_API = PROMOTION_MANAGEMENT_PREFIX + "/active";
        public static final String GET_VISIBLE_PROMOTIONS_API = PROMOTION_MANAGEMENT_PREFIX + "/visible";
        public static final String GET_EXPIRED_PROMOTIONS_API = PROMOTION_MANAGEMENT_PREFIX + "/expired";
        public static final String GET_PROMOTIONS_STARTING_SOON_API = PROMOTION_MANAGEMENT_PREFIX + "/starting-soon";
        public static final String GET_PROMOTIONS_ENDING_SOON_API = PROMOTION_MANAGEMENT_PREFIX + "/ending-soon";
        public static final String GET_PROMOTION_USAGE_HISTORY_API = PROMOTION_MANAGEMENT_PREFIX + "/usage-history";

        // Service Type management module endpoints
        public static final String SERVICE_TYPE_MANAGEMENT_PREFIX = "/service-types";
        public static final String GET_ALL_SERVICE_TYPES_API = SERVICE_TYPE_MANAGEMENT_PREFIX + "/get-all";
        public static final String GET_SERVICE_TYPE_BY_ID_API = SERVICE_TYPE_MANAGEMENT_PREFIX + "/{serviceTypeId}";
        public static final String GET_SERVICE_TYPE_BY_CODE_API = SERVICE_TYPE_MANAGEMENT_PREFIX + "/code/{code}";
        public static final String GET_ACTIVE_SERVICE_TYPES_API = SERVICE_TYPE_MANAGEMENT_PREFIX + "/active";
        public static final String SEARCH_SERVICE_TYPES_API = SERVICE_TYPE_MANAGEMENT_PREFIX + "/search";
        public static final String CREATE_SERVICE_TYPE_API = SERVICE_TYPE_MANAGEMENT_PREFIX + "/create";
        public static final String UPDATE_SERVICE_TYPE_API = SERVICE_TYPE_MANAGEMENT_PREFIX + "/{serviceTypeId}/update";
        public static final String DELETE_SERVICE_TYPE_API = SERVICE_TYPE_MANAGEMENT_PREFIX + "/{serviceTypeId}/delete";
        public static final String RESTORE_SERVICE_TYPE_API = SERVICE_TYPE_MANAGEMENT_PREFIX
                        + "/{serviceTypeId}/restore";
        public static final String UPDATE_SERVICE_TYPE_STATUS_API = SERVICE_TYPE_MANAGEMENT_PREFIX
                        + "/{serviceTypeId}/status";
        public static final String ACTIVATE_SERVICE_TYPE_API = SERVICE_TYPE_MANAGEMENT_PREFIX
                        + "/{serviceTypeId}/activate";
        public static final String DEACTIVATE_SERVICE_TYPE_API = SERVICE_TYPE_MANAGEMENT_PREFIX
                        + "/{serviceTypeId}/deactivate";
        public static final String VALIDATE_SERVICE_TYPE_CODE_API = SERVICE_TYPE_MANAGEMENT_PREFIX + "/validate-code";
        public static final String GET_SERVICE_TYPE_STATISTICS_API = SERVICE_TYPE_MANAGEMENT_PREFIX + "/statistics";

        // Service Bay management module endpoints
        public static final String SERVICE_BAY_MANAGEMENT_PREFIX = "/service-bays";
        public static final String GET_ALL_SERVICE_BAYS_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/get-all";
        public static final String GET_ALL_SERVICE_BAYS_DROPDOWN_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/dropdown";
        public static final String GET_SERVICE_BAY_BY_ID_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/{bayId}";
        public static final String GET_SERVICE_BAYS_BY_BRANCH_API = SERVICE_BAY_MANAGEMENT_PREFIX
                        + "/branch/{branchId}";
        public static final String GET_SERVICE_BAYS_BY_TYPE_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/type/{bayType}";
        public static final String GET_ACTIVE_SERVICE_BAYS_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/active";
        public static final String GET_AVAILABLE_SERVICE_BAYS_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/available";
        public static final String SEARCH_SERVICE_BAYS_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/search";
        public static final String CREATE_SERVICE_BAY_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/create";
        public static final String UPDATE_SERVICE_BAY_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/{bayId}/update";
        public static final String DELETE_SERVICE_BAY_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/{bayId}/delete";
        public static final String UPDATE_SERVICE_BAY_STATUS_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/{bayId}/status";
        public static final String ACTIVATE_SERVICE_BAY_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/{bayId}/activate";
        public static final String DEACTIVATE_SERVICE_BAY_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/{bayId}/deactivate";
        public static final String GET_SERVICE_BAY_AVAILABILITY_API = SERVICE_BAY_MANAGEMENT_PREFIX
                        + "/{bayId}/availability";
        public static final String GET_SERVICE_BAY_BOOKINGS_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/{bayId}/bookings";
        public static final String GET_SERVICE_BAY_STATISTICS_API = SERVICE_BAY_MANAGEMENT_PREFIX
                        + "/{bayId}/statistics";
        public static final String VALIDATE_SERVICE_BAY_NAME_API = SERVICE_BAY_MANAGEMENT_PREFIX + "/validate-name";

        // Service Process Tracking management module endpoints
        public static final String SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX = "/service-process-trackings";
        public static final String GET_ALL_SERVICE_PROCESS_TRACKINGS_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/get-all";
        public static final String GET_SERVICE_PROCESS_TRACKING_BY_ID_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/{trackingId}";
        public static final String CREATE_SERVICE_PROCESS_TRACKING_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/create";
        public static final String UPDATE_SERVICE_PROCESS_TRACKING_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/{trackingId}/update";
        public static final String DELETE_SERVICE_PROCESS_TRACKING_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/{trackingId}/delete";
        public static final String GET_SERVICE_PROCESS_TRACKINGS_BY_BOOKING_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/booking/{bookingId}";
        public static final String GET_SERVICE_PROCESS_TRACKINGS_BY_TECHNICIAN_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/technician/{technicianId}";
        public static final String GET_SERVICE_PROCESS_TRACKINGS_BY_BAY_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/bay/{bayId}";
        public static final String GET_IN_PROGRESS_SERVICE_PROCESS_TRACKINGS_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/in-progress";
        public static final String START_SERVICE_PROCESS_TRACKING_STEP_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/{trackingId}/start";
        public static final String UPDATE_SERVICE_PROCESS_TRACKING_PROGRESS_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/{trackingId}/progress/update";
        public static final String COMPLETE_SERVICE_PROCESS_TRACKING_STEP_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/{trackingId}/complete";
        public static final String CANCEL_SERVICE_PROCESS_TRACKING_STEP_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/{trackingId}/cancel";
        public static final String ADD_SERVICE_PROCESS_TRACKING_NOTE_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/{trackingId}/notes";
        public static final String ADD_SERVICE_PROCESS_TRACKING_EVIDENCE_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/{trackingId}/evidence";
        public static final String GET_SERVICE_PROCESS_TRACKING_STATISTICS_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/statistics";
        public static final String GET_TECHNICIAN_EFFICIENCY_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/technician/{technicianId}/efficiency";
        public static final String GET_TECHNICIAN_WORK_TIME_API = SERVICE_PROCESS_TRACKING_MANAGEMENT_PREFIX
                        + "/technician/{technicianId}/work-time";

        public static List<String> PROTECTED_PATHS(String apiPrefix) {
                return List.of(
                                apiPrefix + CHANGE_PASSWORD_API,
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
                                apiPrefix + GET_ALL_VEHICLE_PROFILES_BY_OWNER_ID_API,
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
                                // Center Business Hours APIs
                                apiPrefix + GET_ALL_CENTER_BUSINESS_HOURS_API,
                                apiPrefix + GET_CENTER_BUSINESS_HOURS_BY_ID_API,
                                apiPrefix + CREATE_CENTER_BUSINESS_HOURS_API,
                                apiPrefix + UPDATE_CENTER_BUSINESS_HOURS_API,
                                apiPrefix + DELETE_CENTER_BUSINESS_HOURS_API,
                                apiPrefix + UPDATE_CENTER_BUSINESS_HOURS_STATUS_API,
                                // Center Social Media APIs
                                apiPrefix + GET_ALL_CENTER_SOCIAL_MEDIA_API,
                                apiPrefix + GET_CENTER_SOCIAL_MEDIA_BY_ID_API,
                                apiPrefix + CREATE_CENTER_SOCIAL_MEDIA_API,
                                apiPrefix + UPDATE_CENTER_SOCIAL_MEDIA_API,
                                apiPrefix + DELETE_CENTER_SOCIAL_MEDIA_API,
                                apiPrefix + UPDATE_CENTER_SOCIAL_MEDIA_STATUS_API,
                                apiPrefix + GET_ALL_BRANCHES_API,
                                apiPrefix + GET_ALL_BRANCHES_DROPDOWN_API,
                                apiPrefix + GET_BRANCH_BY_ID_API,
                                apiPrefix + GET_BRANCHES_BY_CENTER_API,
                                apiPrefix + CREATE_BRANCH_API,
                                apiPrefix + UPDATE_BRANCH_API,
                                apiPrefix + UPDATE_BRANCH_STATUS_API,
                                apiPrefix + DELETE_BRANCH_API,
                                // Product Type management APIs
                                apiPrefix + GET_ALL_PRODUCT_TYPES_API,
                                apiPrefix + GET_PRODUCT_TYPE_BY_ID_API,
                                apiPrefix + GET_PRODUCT_TYPE_BY_CODE_API,
                                apiPrefix + GET_PRODUCT_TYPES_BY_CATEGORY_API,
                                apiPrefix + GET_ACTIVE_PRODUCT_TYPES_API,
                                apiPrefix + CREATE_PRODUCT_TYPE_API,
                                apiPrefix + UPDATE_PRODUCT_TYPE_API,
                                apiPrefix + UPDATE_PRODUCT_TYPE_STATUS_API,
                                apiPrefix + DELETE_PRODUCT_TYPE_API,
                                apiPrefix + VALIDATE_PRODUCT_TYPE_CODE_API,
                                apiPrefix + GET_PRODUCT_TYPE_STATISTICS_API,
                                // Media management APIs
                                apiPrefix + GET_ALL_MEDIA_API,
                                apiPrefix + GET_MEDIA_BY_ID_API,
                                apiPrefix + GET_MEDIA_BY_ENTITY_API,
                                apiPrefix + GET_MAIN_MEDIA_BY_ENTITY_API,
                                apiPrefix + GET_MEDIA_BY_TYPE_API,
                                apiPrefix + CREATE_MEDIA_API,
                                apiPrefix + UPDATE_MEDIA_API,
                                apiPrefix + UPDATE_MEDIA_MAIN_STATUS_API,
                                apiPrefix + DELETE_MEDIA_API,
                                apiPrefix + BULK_UPDATE_MEDIA_ORDER_API,
                                apiPrefix + VALIDATE_MEDIA_URL_API,
                                apiPrefix + GET_MEDIA_STATISTICS_API,
                                // Product Attribute management APIs
                                apiPrefix + GET_ALL_PRODUCT_ATTRIBUTES_API,
                                apiPrefix + GET_PRODUCT_ATTRIBUTE_BY_ID_API,
                                apiPrefix + GET_PRODUCT_ATTRIBUTE_BY_CODE_API,
                                apiPrefix + GET_PRODUCT_ATTRIBUTES_BY_DATA_TYPE_API,
                                apiPrefix + GET_REQUIRED_PRODUCT_ATTRIBUTES_API,
                                apiPrefix + GET_ACTIVE_PRODUCT_ATTRIBUTES_API,
                                apiPrefix + CREATE_PRODUCT_ATTRIBUTE_API,
                                apiPrefix + UPDATE_PRODUCT_ATTRIBUTE_API,
                                apiPrefix + UPDATE_PRODUCT_ATTRIBUTE_STATUS_API,
                                apiPrefix + DELETE_PRODUCT_ATTRIBUTE_API,
                                apiPrefix + VALIDATE_PRODUCT_ATTRIBUTE_CODE_API,
                                apiPrefix + GET_PRODUCT_ATTRIBUTE_STATISTICS_API,
                                // Product Attribute Value management APIs
                                apiPrefix + ADD_PRODUCT_ATTRIBUTE_VALUE_API,
                                apiPrefix + GET_PRODUCT_ATTRIBUTE_VALUE_API,
                                apiPrefix + GET_PRODUCT_ATTRIBUTE_VALUES_API,
                                apiPrefix + GET_PRODUCTS_BY_ATTRIBUTE_API,
                                apiPrefix + UPDATE_PRODUCT_ATTRIBUTE_VALUE_API,
                                apiPrefix + DELETE_PRODUCT_ATTRIBUTE_VALUE_API,
                                apiPrefix + BULK_UPDATE_PRODUCT_ATTRIBUTE_VALUES_API,
                                apiPrefix + BULK_UPDATE_PRODUCT_ATTRIBUTE_VALUES_BY_PRODUCT_API,
                                apiPrefix + SEARCH_PRODUCTS_BY_ATTRIBUTE_VALUE_API,
                                apiPrefix + SEARCH_PRODUCTS_BY_ATTRIBUTE_VALUE_RANGE_API,
                                apiPrefix + GET_PRODUCT_ATTRIBUTE_VALUE_COUNT_API,
                                apiPrefix + GET_PRODUCT_COUNT_BY_ATTRIBUTE_API,
                                // Product management APIs
                                apiPrefix + GET_ALL_PRODUCTS_API,
                                apiPrefix + GET_PRODUCT_BY_ID_API,
                                apiPrefix + GET_PRODUCT_BY_URL_API,
                                apiPrefix + GET_PRODUCTS_BY_PRODUCT_TYPE_API,
                                apiPrefix + GET_PRODUCTS_BY_SUPPLIER_API,
                                apiPrefix + SEARCH_PRODUCTS_API,
                                apiPrefix + GET_FEATURED_PRODUCTS_API,
                                apiPrefix + CREATE_PRODUCT_API,
                                apiPrefix + UPDATE_PRODUCT_API,
                                apiPrefix + UPDATE_PRODUCT_STATUS_API,
                                apiPrefix + DELETE_PRODUCT_API,
                                // Service management APIs
                                apiPrefix + GET_ALL_SERVICES_API,
                                apiPrefix + GET_SERVICE_BY_ID_API,
                                apiPrefix + GET_SERVICES_BY_CATEGORY_API,
                                apiPrefix + GET_SERVICES_BY_TYPE_API,
                                apiPrefix + GET_SERVICES_BY_SKILL_LEVEL_API,
                                apiPrefix + SEARCH_SERVICES_API,
                                apiPrefix + CREATE_SERVICE_API,
                                apiPrefix + UPDATE_SERVICE_API,
                                apiPrefix + DELETE_SERVICE_API,
                                // Service Pricing management APIs
                                apiPrefix + GET_SERVICE_PRICING_API,
                                apiPrefix + GET_SERVICE_PRICING_INFO_API,
                                apiPrefix + RECALCULATE_SERVICE_BASE_PRICE_API,
                                apiPrefix + UPDATE_SERVICE_LABOR_COST_API,
                                // Pricing Management APIs
                                apiPrefix + GET_SERVICE_PRICING_BY_PRICEBOOK_API,
                                apiPrefix + RECALCULATE_SERVICE_PRICING_API,
                                // Service Process management APIs
                                apiPrefix + GET_ALL_SERVICE_PROCESSES_API,
                                apiPrefix + GET_SERVICE_PROCESS_BY_ID_API,
                                apiPrefix + GET_SERVICE_PROCESS_BY_CODE_API,
                                apiPrefix + GET_DEFAULT_SERVICE_PROCESS_API,
                                apiPrefix + GET_ALL_ACTIVE_SERVICE_PROCESSES_API,
                                apiPrefix + CREATE_SERVICE_PROCESS_API,
                                apiPrefix + UPDATE_SERVICE_PROCESS_API,
                                apiPrefix + DELETE_SERVICE_PROCESS_API,
                                apiPrefix + SET_DEFAULT_SERVICE_PROCESS_API,
                                apiPrefix + GET_SERVICE_PROCESS_STEPS_API,
                                apiPrefix + GET_SERVICE_PROCESS_STEP_BY_ID_API,
                                apiPrefix + ADD_SERVICE_PROCESS_STEP_API,
                                apiPrefix + UPDATE_SERVICE_PROCESS_STEP_API,
                                apiPrefix + DELETE_SERVICE_PROCESS_STEP_API,
                                apiPrefix + GET_SERVICE_PROCESS_STEP_PRODUCTS_API,
                                apiPrefix + GET_SERVICE_PROCESS_STEP_PRODUCT_BY_ID_API,
                                apiPrefix + ADD_SERVICE_PROCESS_STEP_PRODUCT_API,
                                apiPrefix + UPDATE_SERVICE_PROCESS_STEP_PRODUCT_API,
                                apiPrefix + DELETE_SERVICE_PROCESS_STEP_PRODUCT_API,
                                // Category management APIs
                                apiPrefix + GET_ALL_CATEGORIES_API,
                                apiPrefix + GET_ALL_CATEGORIES_HIERARCHY_API,
                                apiPrefix + GET_CATEGORY_BY_ID_API,
                                apiPrefix + GET_CATEGORY_PATH_BY_ID_API,
                                apiPrefix + GET_SUB_CATEGORY_BY_ID_API,
                                apiPrefix + CREATE_CATEGORY_API,
                                apiPrefix + UPDATE_CATEGORY_API,
                                apiPrefix + UPDATE_CATEGORY_STATUS_API,
                                apiPrefix + DELETE_CATEGORY_API,
                                apiPrefix + VALIDATE_CATEGORY_CODE_API,
                                // Promotion Type management APIs
                                apiPrefix + GET_ALL_PROMOTION_TYPES_API,
                                apiPrefix + GET_PROMOTION_TYPE_BY_ID_API,
                                apiPrefix + GET_PROMOTION_TYPE_BY_TYPE_CODE_API,
                                apiPrefix + GET_ACTIVE_PROMOTION_TYPES_API,
                                apiPrefix + SEARCH_PROMOTION_TYPES_API,
                                apiPrefix + CREATE_PROMOTION_TYPE_API,
                                apiPrefix + UPDATE_PROMOTION_TYPE_API,
                                apiPrefix + UPDATE_PROMOTION_TYPE_STATUS_API,
                                apiPrefix + DELETE_PROMOTION_TYPE_API,
                                apiPrefix + ACTIVATE_PROMOTION_TYPE_API,
                                apiPrefix + DEACTIVATE_PROMOTION_TYPE_API,
                                apiPrefix + GET_PROMOTION_TYPE_STATISTICS_API,
                                // Promotion management APIs
                                apiPrefix + GET_ALL_PROMOTIONS_API,
                                apiPrefix + GET_PROMOTION_BY_ID_API,
                                apiPrefix + GET_PROMOTION_BY_CODE_API,
                                apiPrefix + CREATE_PROMOTION_API,
                                apiPrefix + UPDATE_PROMOTION_API,
                                apiPrefix + DELETE_PROMOTION_API,
                                apiPrefix + MAKE_PROMOTION_VISIBLE_API,
                                apiPrefix + MAKE_PROMOTION_INVISIBLE_API,
                                apiPrefix + RESTORE_PROMOTION_API,
                                apiPrefix + GET_PROMOTION_STATISTICS_API,
                                apiPrefix + GET_ACTIVE_PROMOTIONS_API,
                                apiPrefix + GET_VISIBLE_PROMOTIONS_API,
                                apiPrefix + GET_EXPIRED_PROMOTIONS_API,
                                apiPrefix + GET_PROMOTIONS_STARTING_SOON_API,
                                apiPrefix + GET_PROMOTIONS_ENDING_SOON_API,
                                apiPrefix + GET_PROMOTION_USAGE_HISTORY_API,
                                // Service Type management APIs
                                apiPrefix + GET_ALL_SERVICE_TYPES_API,
                                apiPrefix + GET_SERVICE_TYPE_BY_ID_API,
                                apiPrefix + GET_SERVICE_TYPE_BY_CODE_API,
                                apiPrefix + GET_ACTIVE_SERVICE_TYPES_API,
                                apiPrefix + SEARCH_SERVICE_TYPES_API,
                                apiPrefix + CREATE_SERVICE_TYPE_API,
                                apiPrefix + UPDATE_SERVICE_TYPE_API,
                                apiPrefix + DELETE_SERVICE_TYPE_API,
                                apiPrefix + RESTORE_SERVICE_TYPE_API,
                                apiPrefix + UPDATE_SERVICE_TYPE_STATUS_API,
                                apiPrefix + ACTIVATE_SERVICE_TYPE_API,
                                apiPrefix + DEACTIVATE_SERVICE_TYPE_API,
                                apiPrefix + VALIDATE_SERVICE_TYPE_CODE_API,
                                apiPrefix + GET_SERVICE_TYPE_STATISTICS_API,
                                // Service Bay management APIs
                                apiPrefix + GET_ALL_SERVICE_BAYS_API,
                                apiPrefix + GET_ALL_SERVICE_BAYS_DROPDOWN_API,
                                apiPrefix + GET_SERVICE_BAY_BY_ID_API,
                                apiPrefix + GET_SERVICE_BAYS_BY_BRANCH_API,
                                apiPrefix + GET_SERVICE_BAYS_BY_TYPE_API,
                                apiPrefix + GET_ACTIVE_SERVICE_BAYS_API,
                                apiPrefix + GET_AVAILABLE_SERVICE_BAYS_API,
                                apiPrefix + SEARCH_SERVICE_BAYS_API,
                                apiPrefix + CREATE_SERVICE_BAY_API,
                                apiPrefix + UPDATE_SERVICE_BAY_API,
                                apiPrefix + DELETE_SERVICE_BAY_API,
                                apiPrefix + UPDATE_SERVICE_BAY_STATUS_API,
                                apiPrefix + ACTIVATE_SERVICE_BAY_API,
                                apiPrefix + DEACTIVATE_SERVICE_BAY_API,
                                apiPrefix + GET_SERVICE_BAY_AVAILABILITY_API,
                                apiPrefix + GET_SERVICE_BAY_BOOKINGS_API,
                                apiPrefix + GET_SERVICE_BAY_STATISTICS_API,
                                apiPrefix + VALIDATE_SERVICE_BAY_NAME_API,
                                // Service Process Tracking management APIs
                                apiPrefix + GET_ALL_SERVICE_PROCESS_TRACKINGS_API,
                                apiPrefix + GET_SERVICE_PROCESS_TRACKING_BY_ID_API,
                                apiPrefix + CREATE_SERVICE_PROCESS_TRACKING_API,
                                apiPrefix + UPDATE_SERVICE_PROCESS_TRACKING_API,
                                apiPrefix + DELETE_SERVICE_PROCESS_TRACKING_API,
                                apiPrefix + GET_SERVICE_PROCESS_TRACKINGS_BY_BOOKING_API,
                                apiPrefix + GET_SERVICE_PROCESS_TRACKINGS_BY_TECHNICIAN_API,
                                apiPrefix + GET_SERVICE_PROCESS_TRACKINGS_BY_BAY_API,
                                apiPrefix + GET_IN_PROGRESS_SERVICE_PROCESS_TRACKINGS_API,
                                apiPrefix + START_SERVICE_PROCESS_TRACKING_STEP_API,
                                apiPrefix + UPDATE_SERVICE_PROCESS_TRACKING_PROGRESS_API,
                                apiPrefix + COMPLETE_SERVICE_PROCESS_TRACKING_STEP_API,
                                apiPrefix + CANCEL_SERVICE_PROCESS_TRACKING_STEP_API,
                                apiPrefix + ADD_SERVICE_PROCESS_TRACKING_NOTE_API,
                                apiPrefix + ADD_SERVICE_PROCESS_TRACKING_EVIDENCE_API,
                                apiPrefix + GET_SERVICE_PROCESS_TRACKING_STATISTICS_API,
                                apiPrefix + GET_TECHNICIAN_EFFICIENCY_API,
                                apiPrefix + GET_TECHNICIAN_WORK_TIME_API);
        }
}
