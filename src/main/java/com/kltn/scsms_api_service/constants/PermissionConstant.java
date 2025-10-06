package com.kltn.scsms_api_service.constants;

/**
 * Permission constants that correspond to permission_code in database
 * These should match exactly with the permission codes stored in the permissions table
 */
public final class PermissionConstant {

    private PermissionConstant() {
        // Utility class - no instantiation
    }

    // APPOINTMENT_MANAGEMENT module
    public static final String APPOINTMENT_CREATE = "APPOINTMENT_CREATE";
    public static final String APPOINTMENT_READ = "APPOINTMENT_READ";
    public static final String APPOINTMENT_ASSIGN = "APPOINTMENT_ASSIGN";
    public static final String APPOINTMENT_DELETE = "APPOINTMENT_DELETE";
    public static final String APPOINTMENT_UPDATE = "APPOINTMENT_UPDATE";


    public static final String LOG_EXPORT = "LOG_EXPORT";
    public static final String AUDIT_READ = "AUDIT_READ";
    public static final String LOG_READ = "LOG_READ";


    public static final String CUSTOMER_UPDATE = "CUSTOMER_UPDATE";
    public static final String CUSTOMER_DELETE = "CUSTOMER_DELETE";
    public static final String CUSTOMER_HISTORY = "CUSTOMER_HISTORY";
    public static final String CUSTOMER_CREATE = "CUSTOMER_CREATE";
    public static final String CUSTOMER_READ = "CUSTOMER_READ";


    public static final String INVENTORY_READ = "INVENTORY_READ";
    public static final String INVENTORY_IMPORT = "INVENTORY_IMPORT";
    public static final String INVENTORY_EXPORT = "INVENTORY_EXPORT";
    public static final String INVENTORY_DELETE = "INVENTORY_DELETE";
    public static final String INVENTORY_UPDATE = "INVENTORY_UPDATE";
    public static final String INVENTORY_CREATE = "INVENTORY_CREATE";
    public static final String INVENTORY_STOCKTAKE = "INVENTORY_STOCKTAKE";


    public static final String PAYMENT_DELETE = "PAYMENT_DELETE";
    public static final String PAYMENT_REFUND = "PAYMENT_REFUND";
    public static final String PAYMENT_DISCOUNT = "PAYMENT_DISCOUNT";
    public static final String PAYMENT_CREATE = "PAYMENT_CREATE";
    public static final String PAYMENT_UPDATE = "PAYMENT_UPDATE";
    public static final String PAYMENT_READ = "PAYMENT_READ";


    public static final String REPORT_INVENTORY = "REPORT_INVENTORY";
    public static final String REPORT_EMPLOYEE = "REPORT_EMPLOYEE";
    public static final String REPORT_SALES = "REPORT_SALES";
    public static final String REPORT_CUSTOMER = "REPORT_CUSTOMER";
    public static final String REPORT_SERVICE = "REPORT_SERVICE";
    public static final String REPORT_EXPORT = "REPORT_EXPORT";


    public static final String SERVICE_READ = "SERVICE_READ";
    public static final String SERVICE_CREATE = "SERVICE_CREATE";
    public static final String SERVICE_DELETE = "SERVICE_DELETE";
    public static final String SERVICE_PRICING = "SERVICE_PRICING";
    public static final String SERVICE_UPDATE = "SERVICE_UPDATE";
    
    // SERVICE_PROCESS_MANAGEMENT module
    public static final String SERVICE_PROCESS_READ = "SERVICE_PROCESS_READ";
    public static final String SERVICE_PROCESS_CREATE = "SERVICE_PROCESS_CREATE";
    public static final String SERVICE_PROCESS_UPDATE = "SERVICE_PROCESS_UPDATE";
    public static final String SERVICE_PROCESS_DELETE = "SERVICE_PROCESS_DELETE";


    public static final String CONFIG_UPDATE = "CONFIG_UPDATE";
    public static final String CONFIG_RESTORE = "CONFIG_RESTORE";
    public static final String CONFIG_READ = "CONFIG_READ";
    public static final String CONFIG_BACKUP = "CONFIG_BACKUP";


    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_ASSIGN_ROLE = "USER_ASSIGN_ROLE";
    public static final String USER_READ = "USER_READ";


    public static final String VEHICLE_DELETE = "VEHICLE_DELETE";
    public static final String VEHICLE_CREATE = "VEHICLE_CREATE";
    public static final String VEHICLE_READ = "VEHICLE_READ";
    public static final String VEHICLE_MAINTENANCE_HISTORY = "VEHICLE_MAINTENANCE_HISTORY";
    public static final String VEHICLE_UPDATE = "VEHICLE_UPDATE";


    public static final String WORKORDER_DELETE = "WORKORDER_DELETE";
    public static final String WORKORDER_COMPLETE = "WORKORDER_COMPLETE";
    public static final String WORKORDER_ASSIGN = "WORKORDER_ASSIGN";
    public static final String WORKORDER_CREATE = "WORKORDER_CREATE";
    public static final String WORKORDER_READ = "WORKORDER_READ";
    public static final String WORKORDER_UPDATE = "WORKORDER_UPDATE";

    // PROMOTION_MANAGEMENT module
    public static final String PROMOTION_CREATE = "PROMOTION_CREATE";
    public static final String PROMOTION_READ = "PROMOTION_READ";
    public static final String PROMOTION_UPDATE = "PROMOTION_UPDATE";
    public static final String PROMOTION_DELETE = "PROMOTION_DELETE";
    public static final String PROMOTION_ACTIVATE = "PROMOTION_ACTIVATE";
    public static final String PROMOTION_DEACTIVATE = "PROMOTION_DEACTIVATE";
    public static final String PROMOTION_VISIBILITY = "PROMOTION_VISIBILITY";
    public static final String PROMOTION_STATISTICS = "PROMOTION_STATISTICS";

    // SERVICE_TYPE_MANAGEMENT module
    public static final String SERVICE_TYPE_CREATE = "SERVICE_TYPE_CREATE";
    public static final String SERVICE_TYPE_READ = "SERVICE_TYPE_READ";
    public static final String SERVICE_TYPE_UPDATE = "SERVICE_TYPE_UPDATE";
    public static final String SERVICE_TYPE_DELETE = "SERVICE_TYPE_DELETE";
    public static final String SERVICE_TYPE_ACTIVATE = "SERVICE_TYPE_ACTIVATE";
    public static final String SERVICE_TYPE_DEACTIVATE = "SERVICE_TYPE_DEACTIVATE";
    public static final String SERVICE_TYPE_STATISTICS = "SERVICE_TYPE_STATISTICS";

    // SERVICE_PACKAGE_TYPE_MANAGEMENT module
    public static final String SERVICE_PACKAGE_TYPE_CREATE = "SERVICE_PACKAGE_TYPE_CREATE";
    public static final String SERVICE_PACKAGE_TYPE_READ = "SERVICE_PACKAGE_TYPE_READ";
    public static final String SERVICE_PACKAGE_TYPE_UPDATE = "SERVICE_PACKAGE_TYPE_UPDATE";
    public static final String SERVICE_PACKAGE_TYPE_DELETE = "SERVICE_PACKAGE_TYPE_DELETE";
    public static final String SERVICE_PACKAGE_TYPE_ACTIVATE = "SERVICE_PACKAGE_TYPE_ACTIVATE";
    public static final String SERVICE_PACKAGE_TYPE_DEACTIVATE = "SERVICE_PACKAGE_TYPE_DEACTIVATE";
    public static final String SERVICE_PACKAGE_TYPE_SET_DEFAULT = "SERVICE_PACKAGE_TYPE_SET_DEFAULT";
    public static final String SERVICE_PACKAGE_TYPE_STATISTICS = "SERVICE_PACKAGE_TYPE_STATISTICS";

    // Module constants
    public static final class Modules {
        public static final String APPOINTMENT_MANAGEMENT = "APPOINTMENT_MANAGEMENT";
        public static final String AUDIT_LOG = "AUDIT_LOG";
        public static final String CUSTOMER_MANAGEMENT = "CUSTOMER_MANAGEMENT";
        public static final String INVENTORY_MANAGEMENT = "INVENTORY_MANAGEMENT";
        public static final String PAYMENT_MANAGEMENT = "PAYMENT_MANAGEMENT";
        public static final String PROMOTION_MANAGEMENT = "PROMOTION_MANAGEMENT";
        public static final String REPORTING = "REPORTING";
        public static final String SERVICE_MANAGEMENT = "SERVICE_MANAGEMENT";
        public static final String SYSTEM_CONFIG = "SYSTEM_CONFIG";
        public static final String USER_MANAGEMENT = "USER_MANAGEMENT";
        public static final String VEHICLE_MANAGEMENT = "VEHICLE_MANAGEMENT";
        public static final String WORKORDER_MANAGEMENT = "WORKORDER_MANAGEMENT";
        public static final String SERVICE_TYPE_MANAGEMENT = "SERVICE_TYPE_MANAGEMENT";
        public static final String SERVICE_PACKAGE_TYPE_MANAGEMENT = "SERVICE_PACKAGE_TYPE_MANAGEMENT";
        public static final String SERVICE_SLOT_MANAGEMENT = "SERVICE_SLOT_MANAGEMENT";
        public static final String SERVICE_PROCESS_TRACKING_MANAGEMENT = "SERVICE_PROCESS_TRACKING_MANAGEMENT";
    }

    // SERVICE_SLOT_MANAGEMENT module
    public static final String SERVICE_SLOT_CREATE = "SERVICE_SLOT_CREATE";
    public static final String SERVICE_SLOT_READ = "SERVICE_SLOT_READ";
    public static final String SERVICE_SLOT_UPDATE = "SERVICE_SLOT_UPDATE";
    public static final String SERVICE_SLOT_DELETE = "SERVICE_SLOT_DELETE";

    // SERVICE_PROCESS_TRACKING module
    public static final String SERVICE_PROCESS_TRACKING_CREATE = "SERVICE_PROCESS_TRACKING_CREATE";
    public static final String SERVICE_PROCESS_TRACKING_READ = "SERVICE_PROCESS_TRACKING_READ";
    public static final String SERVICE_PROCESS_TRACKING_UPDATE = "SERVICE_PROCESS_TRACKING_UPDATE";
    public static final String SERVICE_PROCESS_TRACKING_DELETE = "SERVICE_PROCESS_TRACKING_DELETE";
}
