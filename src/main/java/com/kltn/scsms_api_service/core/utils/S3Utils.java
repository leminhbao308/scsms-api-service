package com.kltn.scsms_api_service.core.utils;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.kltn.scsms_api_service.configs.property.AwsS3Properties;
import com.kltn.scsms_api_service.constants.S3Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.*;

/**
 * S3 Utility Class
 * Cung cấp các chức năng để tương tác với AWS S3
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3Utils {

    private final AmazonS3 amazonS3;

    private final AwsS3Properties awsS3Properties;

    /**
     * Upload file lên S3
     * 
     * @param file   MultipartFile cần upload
     * @param folder Thư mục đích trên S3
     * @return URL của file đã upload
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            validateFile(file);
            String key = generateFileKey(folder, file.getOriginalFilename());
            String contentType = file.getContentType();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(contentType);
            metadata.setCacheControl(S3Constants.CACHE_CONTROL_PUBLIC);

            // Remove ACL to support buckets with "Block all public access" enabled
            // Public access should be managed via bucket policy instead
            amazonS3.putObject(new PutObjectRequest(
                    awsS3Properties.getS3().getBucketName(),
                    key,
                    file.getInputStream(),
                    metadata));

            log.info("File uploaded successfully: {}", key);
            return getFileUrl(key);

        } catch (IOException e) {
            log.error("Error uploading file to S3: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * Upload file từ byte array
     * 
     * @param fileData    Dữ liệu file dạng byte array
     * @param fileName    Tên file
     * @param folder      Thư mục đích
     * @param contentType Content type của file
     * @return URL của file đã upload
     */
    public String uploadFile(byte[] fileData, String fileName, String folder, String contentType) {
        try {
            String key = generateFileKey(folder, fileName);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileData.length);
            metadata.setContentType(contentType);
            metadata.setCacheControl(S3Constants.CACHE_CONTROL_PUBLIC);

            InputStream inputStream = new ByteArrayInputStream(fileData);
            // Remove ACL to support buckets with "Block all public access" enabled
            amazonS3.putObject(new PutObjectRequest(
                    awsS3Properties.getS3().getBucketName(),
                    key,
                    inputStream,
                    metadata));

            log.info("File uploaded successfully from byte array: {}", key);
            return getFileUrl(key);

        } catch (Exception e) {
            log.error("Error uploading file from byte array to S3: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * Upload nhiều file cùng lúc
     * 
     * @param files  Danh sách các file cần upload
     * @param folder Thư mục đích
     * @return Danh sách URL của các file đã upload
     */
    public List<String> uploadMultipleFiles(List<MultipartFile> files, String folder) {
        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String url = uploadFile(file, folder);
                uploadedUrls.add(url);
            } catch (Exception e) {
                log.error("Error uploading file {}: {}", file.getOriginalFilename(), e.getMessage());
            }
        }
        return uploadedUrls;
    }

    /**
     * Download file từ S3
     * 
     * @param key Key của file trên S3
     * @return Byte array của file
     */
    public byte[] downloadFile(String key) {
        try {
            S3Object s3Object = amazonS3.getObject(awsS3Properties.getS3().getBucketName(), key);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            byte[] content = inputStream.readAllBytes();
            inputStream.close();

            log.info("File downloaded successfully: {}", key);
            return content;

        } catch (Exception e) {
            log.error("Error downloading file from S3: {}", e.getMessage());
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

    /**
     * Xóa file khỏi S3
     * 
     * @param key Key của file cần xóa
     * @return true nếu xóa thành công
     */
    public boolean deleteFile(String key) {
        try {
            if (!doesFileExist(key)) {
                log.warn("File not found for deletion: {}", key);
                return false;
            }

            amazonS3.deleteObject(awsS3Properties.getS3().getBucketName(), key);
            log.info("File deleted successfully: {}", key);
            return true;

        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", e.getMessage());
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    /**
     * Xóa nhiều file cùng lúc
     * 
     * @param keys Danh sách key của các file cần xóa
     * @return Số lượng file đã xóa thành công
     */
    public int deleteMultipleFiles(List<String> keys) {
        int deletedCount = 0;
        for (String key : keys) {
            try {
                if (deleteFile(key)) {
                    deletedCount++;
                }
            } catch (Exception e) {
                log.error("Error deleting file {}: {}", key, e.getMessage());
            }
        }
        return deletedCount;
    }

    /**
     * Copy file trong S3
     * 
     * @param sourceKey      Key của file nguồn
     * @param destinationKey Key của file đích
     * @return URL của file đã copy
     */
    public String copyFile(String sourceKey, String destinationKey) {
        try {
            // Remove ACL to support buckets with "Block all public access" enabled
            CopyObjectRequest copyRequest = new CopyObjectRequest(
                    awsS3Properties.getS3().getBucketName(), sourceKey,
                    awsS3Properties.getS3().getBucketName(), destinationKey);

            amazonS3.copyObject(copyRequest);
            log.info("File copied successfully from {} to {}", sourceKey, destinationKey);
            return getFileUrl(destinationKey);

        } catch (Exception e) {
            log.error("Error copying file in S3: {}", e.getMessage());
            throw new RuntimeException("Failed to copy file in S3", e);
        }
    }

    /**
     * Di chuyển file (copy và xóa file cũ)
     * 
     * @param sourceKey      Key của file nguồn
     * @param destinationKey Key của file đích
     * @return URL của file đã di chuyển
     */
    public String moveFile(String sourceKey, String destinationKey) {
        try {
            String newUrl = copyFile(sourceKey, destinationKey);
            deleteFile(sourceKey);
            log.info("File moved successfully from {} to {}", sourceKey, destinationKey);
            return newUrl;

        } catch (Exception e) {
            log.error("Error moving file in S3: {}", e.getMessage());
            throw new RuntimeException("Failed to move file in S3", e);
        }
    }

    /**
     * Tạo presigned URL cho file (URL tạm thời có thời hạn)
     * 
     * @param key               Key của file
     * @param expirationMinutes Thời gian hết hạn (phút)
     * @return Presigned URL
     */
    public String generatePresignedUrl(String key, int expirationMinutes) {
        try {
            Date expiration = Date.from(Instant.now().plusSeconds(expirationMinutes * 60L));
            URL url = amazonS3.generatePresignedUrl(
                    awsS3Properties.getS3().getBucketName(),
                    key,
                    expiration,
                    HttpMethod.GET);

            log.info("Presigned URL generated for file: {}", key);
            return url.toString();

        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", e.getMessage());
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    /**
     * Tạo presigned URL để upload file
     * 
     * @param key               Key của file sẽ upload
     * @param expirationMinutes Thời gian hết hạn (phút)
     * @return Presigned URL cho upload
     */
    public String generatePresignedUploadUrl(String key, int expirationMinutes) {
        try {
            Date expiration = Date.from(Instant.now().plusSeconds(expirationMinutes * 60L));
            URL url = amazonS3.generatePresignedUrl(
                    awsS3Properties.getS3().getBucketName(),
                    key,
                    expiration,
                    HttpMethod.PUT);

            log.info("Presigned upload URL generated for file: {}", key);
            return url.toString();

        } catch (Exception e) {
            log.error("Error generating presigned upload URL: {}", e.getMessage());
            throw new RuntimeException("Failed to generate presigned upload URL", e);
        }
    }

    /**
     * Kiểm tra file có tồn tại không
     * 
     * @param key Key của file
     * @return true nếu file tồn tại
     */
    public boolean doesFileExist(String key) {
        try {
            return amazonS3.doesObjectExist(awsS3Properties.getS3().getBucketName(), key);
        } catch (Exception e) {
            log.error("Error checking file existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Lấy thông tin metadata của file
     * 
     * @param key Key của file
     * @return ObjectMetadata
     */
    public ObjectMetadata getFileMetadata(String key) {
        try {
            return amazonS3.getObjectMetadata(awsS3Properties.getS3().getBucketName(), key);
        } catch (Exception e) {
            log.error("Error getting file metadata: {}", e.getMessage());
            throw new RuntimeException("Failed to get file metadata", e);
        }
    }

    /**
     * Lấy kích thước file
     * 
     * @param key Key của file
     * @return Kích thước file (bytes)
     */
    public long getFileSize(String key) {
        try {
            ObjectMetadata metadata = getFileMetadata(key);
            return metadata.getContentLength();
        } catch (Exception e) {
            log.error("Error getting file size: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Liệt kê tất cả file trong một folder
     * 
     * @param folderPath Đường dẫn folder
     * @return Danh sách key của các file
     */
    public List<String> listFiles(String folderPath) {
        try {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(awsS3Properties.getS3().getBucketName())
                    .withPrefix(folderPath);

            ListObjectsV2Result result = amazonS3.listObjectsV2(request);
            List<String> fileKeys = new ArrayList<>();

            for (S3ObjectSummary summary : result.getObjectSummaries()) {
                fileKeys.add(summary.getKey());
            }

            log.info("Listed {} files in folder: {}", fileKeys.size(), folderPath);
            return fileKeys;

        } catch (Exception e) {
            log.error("Error listing files in folder: {}", e.getMessage());
            throw new RuntimeException("Failed to list files", e);
        }
    }

    /**
     * Lấy public URL của file
     * 
     * @param key Key của file
     * @return Public URL
     */
    public String getFileUrl(String key) {
        return amazonS3.getUrl(awsS3Properties.getS3().getBucketName(), key).toString();
    }

    /**
     * Trích xuất key từ URL
     * 
     * @param url URL đầy đủ của file
     * @return Key của file
     */
    public String extractKeyFromUrl(String url) {
        try {
            String bucketUrl = amazonS3.getUrl(awsS3Properties.getS3().getBucketName(), "").toString();
            if (url.startsWith(bucketUrl)) {
                return url.substring(bucketUrl.length());
            }
            return url;
        } catch (Exception e) {
            log.error("Error extracting key from URL: {}", e.getMessage());
            return url;
        }
    }

    /**
     * Generate unique file key
     * 
     * @param folder           Thư mục
     * @param originalFileName Tên file gốc
     * @return Key duy nhất
     */
    private String generateFileKey(String folder, String originalFileName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFileName);
        String fileName = removeFileExtension(originalFileName);

        // Sanitize filename
        fileName = fileName.replaceAll("[^a-zA-Z0-9-_]", "_");

        return folder + fileName + "_" + timestamp + "_" + uuid + extension;
    }

    /**
     * Validate file trước khi upload
     * 
     * @param file File cần validate
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        long fileSize = file.getSize();

        // Check allowed extensions first
        if (!isAllowedExtension(extension)) {
            throw new IllegalArgumentException("File type not allowed: " + extension);
        }

        // Check file size based on type using properties from AwsS3Properties
        if (isImageFile(extension)) {
            Integer imageLimit = awsS3Properties.getS3().getImageFileSizeLimit();
            long maxImageSize = imageLimit != null ? imageLimit : S3Constants.FILE_SIZE_5MB;
            if (fileSize > maxImageSize) {
                throw new IllegalArgumentException(
                        String.format("Image file size (%d bytes) exceeds maximum limit (%d bytes)",
                                fileSize, maxImageSize));
            }
        } else if (isVideoFile(extension)) {
            Integer videoLimit = awsS3Properties.getS3().getVideoFileSizeLimit();
            long maxVideoSize = videoLimit != null ? videoLimit : S3Constants.FILE_SIZE_50MB;
            if (fileSize > maxVideoSize) {
                throw new IllegalArgumentException(
                        String.format("Video file size (%d bytes) exceeds maximum limit (%d bytes)",
                                fileSize, maxVideoSize));
            }
        } else if (isDocumentFile(extension)) {
            // Document sử dụng giới hạn mặc định
            if (fileSize > S3Constants.FILE_SIZE_10MB) {
                throw new IllegalArgumentException(
                        String.format("Document file size (%d bytes) exceeds maximum limit (%d bytes)",
                                fileSize, S3Constants.FILE_SIZE_10MB));
            }
        }
    }

    /**
     * Lấy extension của file
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * Xóa extension khỏi tên file
     */
    private String removeFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    /**
     * Kiểm tra có phải file ảnh không
     */
    private boolean isImageFile(String extension) {
        return Arrays.asList(S3Constants.IMAGE_EXTENSIONS).contains(extension.toLowerCase());
    }

    /**
     * Kiểm tra có phải file document không
     */
    private boolean isDocumentFile(String extension) {
        return Arrays.asList(S3Constants.DOCUMENT_EXTENSIONS).contains(extension.toLowerCase());
    }

    /**
     * Kiểm tra có phải file video không
     */
    private boolean isVideoFile(String extension) {
        return Arrays.asList(S3Constants.VIDEO_EXTENSIONS).contains(extension.toLowerCase());
    }

    /**
     * Kiểm tra extension có được phép không
     */
    private boolean isAllowedExtension(String extension) {
        return isImageFile(extension) || isDocumentFile(extension) || isVideoFile(extension);
    }
}
