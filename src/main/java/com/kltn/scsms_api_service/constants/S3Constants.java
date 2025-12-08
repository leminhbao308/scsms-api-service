package com.kltn.scsms_api_service.constants;

/**
 * S3 Storage Constants
 * Định nghĩa các folder và hằng số cho S3 storage
 */
public class S3Constants {
    
    // Base folders
    public static final String IMAGES_FOLDER = "images/";
    public static final String DOCUMENTS_FOLDER = "documents/";
    public static final String VIDEOS_FOLDER = "videos/";
    public static final String TEMP_FOLDER = "temp/";
    
    // Sub-folders for images
    public static final String AVATAR_FOLDER = IMAGES_FOLDER + "avatars/";
    public static final String VEHICLE_FOLDER = IMAGES_FOLDER + "vehicles/";
    public static final String SERVICE_FOLDER = IMAGES_FOLDER + "services/";
    public static final String PROMOTION_FOLDER = IMAGES_FOLDER + "promotions/";
    public static final String REVIEW_FOLDER = IMAGES_FOLDER + "reviews/";
    public static final String INVOICE_FOLDER = IMAGES_FOLDER + "invoices/";
    
    // Sub-folders for documents
    public static final String CONTRACT_FOLDER = DOCUMENTS_FOLDER + "contracts/";
    public static final String REPORT_FOLDER = DOCUMENTS_FOLDER + "reports/";
    public static final String RECEIPT_FOLDER = DOCUMENTS_FOLDER + "receipts/";
    
    // Default file size limits (in bytes)
    public static final long FILE_SIZE_5MB = 5 * 1024 * 1024; // 5MB
    public static final long FILE_SIZE_10MB = 10 * 1024 * 1024; // 10MB
    public static final long FILE_SIZE_50MB = 50 * 1024 * 1024; // 50MB
    
    // Allowed file extensions
    public static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    public static final String[] DOCUMENT_EXTENSIONS = {".pdf", ".doc", ".docx", ".xls", ".xlsx"};
    public static final String[] VIDEO_EXTENSIONS = {".mp4", ".avi", ".mov", ".wmv"};
    
    // Cache control
    public static final String CACHE_CONTROL_PUBLIC = "public, max-age=31536000"; // 1 year
    public static final String CACHE_CONTROL_PRIVATE = "private, max-age=3600"; // 1 hour
    
    private S3Constants() {
        throw new IllegalStateException("Constants class");
    }
}
