package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.productManagement.ProductInfoDto;
import com.kltn.scsms_api_service.core.dto.promotionManagement.PromotionInfoDto;
import com.kltn.scsms_api_service.core.dto.promotionManagement.PromotionUsageHistoryDto;
import com.kltn.scsms_api_service.core.dto.promotionManagement.param.PromotionFilterParam;
import com.kltn.scsms_api_service.core.dto.promotionManagement.param.PromotionUsageHistoryFilterParam;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.CreatePromotionRequest;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.UpdatePromotionRequest;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.UpdatePromotionStatusRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceInfoDto;
import com.kltn.scsms_api_service.core.entity.Promotion;
import com.kltn.scsms_api_service.core.entity.PromotionLine;
import com.kltn.scsms_api_service.core.entity.PromotionUsage;
import com.kltn.scsms_api_service.core.service.businessService.ProductManagementService;
import com.kltn.scsms_api_service.core.service.businessService.PromotionManagementService;
import com.kltn.scsms_api_service.core.service.businessService.ServiceManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Controller handling promotion management operations
 * Manages promotion creation, updates, deletion, and retrieval
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Promotion Management", description = "Promotion management endpoints")
public class PromotionManagementController {
    
    private final PromotionManagementService promotionManagementService;
    private final ProductManagementService productManagementService;
    private final ServiceManagementService serviceManagementService;
    
    /**
     * Get all promotions with filters
     */
    @GetMapping(ApiConstant.GET_ALL_PROMOTIONS_API)
    @SwaggerOperation(summary = "Get all promotions", description = "Retrieve a paginated list of all promotions that can be filtered by various criteria")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionInfoDto>>> getAllPromotions(
        @ModelAttribute PromotionFilterParam promotionFilterParam) {
        log.info("Fetching all promotions");
        
        Page<PromotionInfoDto> promotions = promotionManagementService.getAllPromotions(
            PromotionFilterParam.standardize(promotionFilterParam));
        
        return ResponseBuilder.paginated("Promotions fetched successfully", promotions);
    }
    
    /**
     * Get promotion by ID
     */
    @GetMapping(ApiConstant.GET_PROMOTION_BY_ID_API)
    @SwaggerOperation(summary = "Get promotion by ID", description = "Retrieve a specific promotion by its ID")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PromotionInfoDto>> getPromotionById(
        @PathVariable(value = "promotionId") String promotionId) {
        log.info("Fetching promotion by ID: {}", promotionId);
        
        PromotionInfoDto promotion = promotionManagementService.getPromotionById(UUID.fromString(promotionId));
        
        return ResponseBuilder.success("Promotion fetched successfully", promotion);
    }
    
    /**
     * Get promotion by code
     */
    @GetMapping(ApiConstant.GET_PROMOTION_BY_CODE_API)
    @SwaggerOperation(summary = "Get promotion by code", description = "Retrieve a specific promotion by its code")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PromotionInfoDto>> getPromotionByCode(
        @PathVariable(value = "promotionCode") String promotionCode) {
        log.info("Fetching promotion by code: {}", promotionCode);
        
        PromotionInfoDto promotion = promotionManagementService.getPromotionByCode(promotionCode);
        
        return ResponseBuilder.success("Promotion fetched successfully", promotion);
    }
    
    /**
     * Create new promotion
     */
    @PostMapping(ApiConstant.CREATE_PROMOTION_API)
    @SwaggerOperation(summary = "Create a new promotion", description = "Create a new promotion with the provided details")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_CREATE)
    public ResponseEntity<ApiResponse<PromotionInfoDto>> createPromotion(
        @RequestBody CreatePromotionRequest createPromotionRequest) {
        log.info("Creating new promotion with code: {}", createPromotionRequest.getPromotionCode());
        
        PromotionInfoDto createdPromotion = promotionManagementService.createPromotion(createPromotionRequest);
        
        return ResponseBuilder.success("Promotion created successfully", createdPromotion);
    }
    
    /**
     * Update existing promotion
     */
    @PostMapping(ApiConstant.UPDATE_PROMOTION_API)
    @SwaggerOperation(summary = "Update an existing promotion", description = "Update the details of an existing promotion")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_UPDATE)
    public ResponseEntity<ApiResponse<PromotionInfoDto>> updatePromotion(
        @PathVariable(value = "promotionId") String promotionId,
        @RequestBody UpdatePromotionRequest updatePromotionRequest) {
        log.info("Updating promotion with ID: {}", promotionId);
        
        PromotionInfoDto updatedPromotion = promotionManagementService.updatePromotion(
            UUID.fromString(promotionId), updatePromotionRequest);
        
        return ResponseBuilder.success("Promotion updated successfully", updatedPromotion);
    }
    
    /**
     * Delete promotion (soft delete)
     */
    @PostMapping(ApiConstant.DELETE_PROMOTION_API)
    @SwaggerOperation(summary = "Delete a promotion", description = "Delete a promotion by its ID (soft delete)")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_DELETE)
    public ResponseEntity<ApiResponse<Void>> deletePromotion(
        @PathVariable(value = "promotionId") String promotionId) {
        log.info("Deleting promotion with ID: {}", promotionId);
        
        promotionManagementService.deletePromotion(UUID.fromString(promotionId));
        
        return ResponseBuilder.success("Promotion deleted successfully");
    }
    
    /**
     * Update promotion status (activate/deactivate)
     */
    @PostMapping(ApiConstant.UPDATE_PROMOTION_STATUS_API)
    @SwaggerOperation(summary = "Update promotion status", description = "Update promotion status to active or inactive")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_UPDATE)
    public ResponseEntity<ApiResponse<Void>> updatePromotionStatus(
        @PathVariable(value = "promotionId") String promotionId,
        @RequestBody UpdatePromotionStatusRequest request) {
        log.info("Updating promotion status with ID: {} to active: {}", promotionId, request.getIsActive());
        
        promotionManagementService.updatePromotionStatus(UUID.fromString(promotionId), request);
        
        String statusMessage = request.getIsActive() ? "activated" : "deactivated";
        return ResponseBuilder.success("Promotion " + statusMessage + " successfully");
    }
    
    /**
     * Make promotion visible
     */
    @PostMapping(ApiConstant.MAKE_PROMOTION_VISIBLE_API)
    @SwaggerOperation(summary = "Make promotion visible", description = "Make a promotion visible to customers")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_UPDATE)
    public ResponseEntity<ApiResponse<Void>> makePromotionVisible(
        @PathVariable(value = "promotionId") String promotionId) {
        log.info("Making promotion visible with ID: {}", promotionId);
        
        promotionManagementService.makePromotionVisible(UUID.fromString(promotionId));
        
        return ResponseBuilder.success("Promotion made visible successfully");
    }
    
    /**
     * Make promotion invisible
     */
    @PostMapping(ApiConstant.MAKE_PROMOTION_INVISIBLE_API)
    @SwaggerOperation(summary = "Make promotion invisible", description = "Make a promotion invisible to customers")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_UPDATE)
    public ResponseEntity<ApiResponse<Void>> makePromotionInvisible(
        @PathVariable(value = "promotionId") String promotionId) {
        log.info("Making promotion invisible with ID: {}", promotionId);
        
        promotionManagementService.makePromotionInvisible(UUID.fromString(promotionId));
        
        return ResponseBuilder.success("Promotion made invisible successfully");
    }
    
    /**
     * Restore promotion (undo soft delete)
     */
    @PostMapping(ApiConstant.RESTORE_PROMOTION_API)
    @SwaggerOperation(summary = "Restore a promotion", description = "Restore a soft-deleted promotion")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_UPDATE)
    public ResponseEntity<ApiResponse<Void>> restorePromotion(
        @PathVariable(value = "promotionId") String promotionId) {
        log.info("Restoring promotion with ID: {}", promotionId);
        
        promotionManagementService.restorePromotion(UUID.fromString(promotionId));
        
        return ResponseBuilder.success("Promotion restored successfully");
    }
    
    /**
     * Get promotion statistics
     */
    @GetMapping(ApiConstant.GET_PROMOTION_STATISTICS_API)
    @SwaggerOperation(summary = "Get promotion statistics", description = "Get statistics about promotions")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PromotionManagementService.PromotionStatisticsDto>> getPromotionStatistics() {
        log.info("Fetching promotion statistics");
        
        PromotionManagementService.PromotionStatisticsDto statistics = promotionManagementService
            .getPromotionStatistics();
        
        return ResponseBuilder.success("Promotion statistics fetched successfully", statistics);
    }
    
    /**
     * Get active promotions
     */
    @GetMapping(ApiConstant.GET_ACTIVE_PROMOTIONS_API)
    @SwaggerOperation(summary = "Get active promotions", description = "Retrieve a paginated list of active promotions")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionInfoDto>>> getActivePromotions(
        @ModelAttribute PromotionFilterParam promotionFilterParam) {
        log.info("Fetching active promotions");
        
        Page<PromotionInfoDto> promotions = promotionManagementService.getActivePromotions(
            PromotionFilterParam.standardize(promotionFilterParam));
        
        return ResponseBuilder.paginated("Active promotions fetched successfully", promotions);
    }
    
    /**
     * Get visible promotions
     */
    @GetMapping(ApiConstant.GET_VISIBLE_PROMOTIONS_API)
    @SwaggerOperation(summary = "Get visible promotions", description = "Retrieve a paginated list of visible promotions")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionInfoDto>>> getVisiblePromotions(
        @ModelAttribute PromotionFilterParam promotionFilterParam) {
        log.info("Fetching visible promotions");
        
        Page<PromotionInfoDto> promotions = promotionManagementService.getVisiblePromotions(
            PromotionFilterParam.standardize(promotionFilterParam));
        
        return ResponseBuilder.paginated("Visible promotions fetched successfully", promotions);
    }
    
    /**
     * Get expired promotions
     */
    @GetMapping(ApiConstant.GET_EXPIRED_PROMOTIONS_API)
    @SwaggerOperation(summary = "Get expired promotions", description = "Retrieve a paginated list of expired promotions")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionInfoDto>>> getExpiredPromotions(
        @ModelAttribute PromotionFilterParam promotionFilterParam) {
        log.info("Fetching expired promotions");
        
        Page<PromotionInfoDto> promotions = promotionManagementService.getExpiredPromotions(
            PromotionFilterParam.standardize(promotionFilterParam));
        
        return ResponseBuilder.paginated("Expired promotions fetched successfully", promotions);
    }
    
    /**
     * Get promotions starting soon
     */
    @GetMapping(ApiConstant.GET_PROMOTIONS_STARTING_SOON_API)
    @SwaggerOperation(summary = "Get promotions starting soon", description = "Retrieve a paginated list of promotions that are starting soon")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionInfoDto>>> getPromotionsStartingSoon(
        @ModelAttribute PromotionFilterParam promotionFilterParam) {
        log.info("Fetching promotions starting soon");
        
        Page<PromotionInfoDto> promotions = promotionManagementService.getPromotionsStartingSoon(
            PromotionFilterParam.standardize(promotionFilterParam));
        
        return ResponseBuilder.paginated("Promotions starting soon fetched successfully", promotions);
    }
    
    /**
     * Get promotions ending soon
     */
    @GetMapping(ApiConstant.GET_PROMOTIONS_ENDING_SOON_API)
    @SwaggerOperation(summary = "Get promotions ending soon", description = "Retrieve a paginated list of promotions that are ending soon")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionInfoDto>>> getPromotionsEndingSoon(
        @ModelAttribute PromotionFilterParam promotionFilterParam) {
        log.info("Fetching promotions ending soon");
        
        Page<PromotionInfoDto> promotions = promotionManagementService.getPromotionsEndingSoon(
            PromotionFilterParam.standardize(promotionFilterParam));
        
        return ResponseBuilder.paginated("Promotions ending soon fetched successfully", promotions);
    }
    
    /**
     * Get promotion usage history
     */
    @GetMapping(ApiConstant.GET_PROMOTION_USAGE_HISTORY_API)
    @SwaggerOperation(summary = "Get promotion usage history", description = "Retrieve a paginated list of promotion usage history with filters")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionUsageHistoryDto>>> getPromotionUsageHistory(
        @ModelAttribute PromotionUsageHistoryFilterParam filterParam) {
        log.info("Fetching promotion usage history");
        
        Page<PromotionUsageHistoryDto> usageHistory = promotionManagementService
            .getPromotionUsageHistory(PromotionUsageHistoryFilterParam.standardize(filterParam));
        
        return ResponseBuilder.paginated("Promotion usage history fetched successfully", usageHistory);
    }
    
    // Add this endpoint to PromotionManagementController.java
    
    @GetMapping("/promotions/export-summary-report")
    @SwaggerOperation(summary = "Export promotion summary report to Excel",
        description = "Export promotion summary report with date range filter and optional promotion code")
    public ResponseEntity<byte[]> exportPromotionSummaryReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(required = false) String promotionCode) {
        
        try {
            log.info("Exporting promotion summary report from {} to {}, code: {}",
                fromDate, toDate, promotionCode);
            
            // Get filtered promotions
            List<Promotion> promotions = promotionManagementService.getPromotionsForReport(
                fromDate.atStartOfDay(),
                toDate.atTime(23, 59, 59),
                promotionCode);
            
            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Báo cáo tổng kết CTKM");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            
            int rowCount = 0;
            
            // Create title
            Row titleRow = sheet.createRow(rowCount++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO TỔNG KẾT CTKM");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));
            
            // Create info rows
            Row periodRow = sheet.createRow(rowCount++);
            periodRow.createCell(0).setCellValue(
                "Thời gian xuất báo cáo : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            Row userRow = sheet.createRow(rowCount++);
            userRow.createCell(0).setCellValue("User xuất báo cáo : ");
            
            rowCount++; // Empty row
            
            // Create header row
            Row headerRow = sheet.createRow(rowCount++);
            String[] headers = {
                "Mã CTKM", "Tên CTKM", "Ngày bắt đầu", "Ngày kết thúc",
                "Mã SP/DV tặng", "Tên SP/DV tặng", "SL tặng", "Đơn vị tính",
                "Số tiền chiết khấu", "Ngân sách tổng", "Ngân sách đã sử dụng",
                "Ngân sách còn lại"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Fill data
            BigDecimal totalBudget = BigDecimal.ZERO;
            BigDecimal totalUsedBudget = BigDecimal.ZERO;
            BigDecimal totalRemainingBudget = BigDecimal.ZERO;
            
            for (Promotion promotion : promotions) {
                // Calculate promotion statistics
                BigDecimal promotionBudget = calculatePromotionBudget(promotion);
                BigDecimal usedBudget = calculateUsedBudget(promotion);
                BigDecimal remainingBudget = promotionBudget.subtract(usedBudget);
                
                totalBudget = totalBudget.add(promotionBudget);
                totalUsedBudget = totalUsedBudget.add(usedBudget);
                totalRemainingBudget = totalRemainingBudget.add(remainingBudget);
                
                // For each promotion line (if applicable)
                if (promotion.getPromotionLines() != null && !promotion.getPromotionLines().isEmpty()) {
                    for (PromotionLine line : promotion.getPromotionLines()) {
                        Row row = sheet.createRow(rowCount++);
                        
                        // Mã CTKM
                        Cell cell0 = row.createCell(0);
                        cell0.setCellValue(promotion.getPromotionCode() != null ? promotion.getPromotionCode() : "");
                        cell0.setCellStyle(dataStyle);
                        
                        // Tên CTKM
                        Cell cell1 = row.createCell(1);
                        cell1.setCellValue(promotion.getName());
                        cell1.setCellStyle(dataStyle);
                        
                        // Ngày bắt đầu
                        Cell cell2 = row.createCell(2);
                        if (promotion.getStartAt() != null) {
                            cell2.setCellValue(promotion.getStartAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        }
                        cell2.setCellStyle(dataStyle);
                        
                        // Ngày kết thúc
                        Cell cell3 = row.createCell(3);
                        if (promotion.getEndAt() != null) {
                            cell3.setCellValue(promotion.getEndAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        }
                        cell3.setCellStyle(dataStyle);
                        
                        // Mã SP tặng
                        Cell cell4 = row.createCell(4);
                        if (line.getDiscountType() == PromotionLine.DiscountType.FREE_PRODUCT && line.getFreeProduct() != null) {
                            cell4.setCellValue(line.getFreeProduct().getProductId() != null ? line.getFreeProduct().getProductId().toString() : "");
                        } else if (
                            line.getDiscountType() == PromotionLine.DiscountType.BUY_X_GET_Y &&
                                line.getTargetId() != null
                        ) {
                            cell4.setCellValue(line.getTargetId().toString());
                        }
                        cell4.setCellStyle(dataStyle);
                        
                        // Tên SP tặng
                        Cell cell5 = row.createCell(5);
                        if (line.getDiscountType() == PromotionLine.DiscountType.FREE_PRODUCT && line.getFreeProduct() != null) {
                            cell5.setCellValue(line.getFreeProduct().getProductName() != null ? line.getFreeProduct().getProductName() : "");
                        } else if (
                            line.getDiscountType() == PromotionLine.DiscountType.BUY_X_GET_Y &&
                                line.getTargetId() != null
                        ) {
                            if (line.getLineType() == PromotionLine.LineType.PRODUCT) {
                                ProductInfoDto targetProduct = productManagementService.getProductById(line.getTargetId());
                                cell5.setCellValue(targetProduct.getProductName() != null ? targetProduct.getProductName() : "");
                            } else if (line.getLineType() == PromotionLine.LineType.SERVICE) {
                                ServiceInfoDto targetService = serviceManagementService.getServiceById(line.getTargetId());
                                cell5.setCellValue(targetService.getServiceName() != null ? targetService.getServiceName() : "");
                            }
                        }
                        cell5.setCellStyle(dataStyle);
                        
                        // SL tặng
                        Cell cell6 = row.createCell(6);
                        if (line.getDiscountType() == PromotionLine.DiscountType.FREE_PRODUCT) {
                            cell6.setCellValue(line.getFreeQuantity());
                        } else if (line.getDiscountType() == PromotionLine.DiscountType.BUY_X_GET_Y) {
                            cell6.setCellValue(line.getGetQty());
                        }
                        cell6.setCellStyle(numberStyle);
                        
                        // Đơn vị tính
                        Cell cell7 = row.createCell(7);
                        if (line.getDiscountType() == PromotionLine.DiscountType.FREE_PRODUCT && line.getFreeProduct() != null) {
                            cell7.setCellValue(line.getFreeProduct().getUnitOfMeasure() != null ? line.getFreeProduct().getUnitOfMeasure() : "Cái");
                        } else if (line.getDiscountType() == PromotionLine.DiscountType.BUY_X_GET_Y) {
                            if (line.getLineType() == PromotionLine.LineType.PRODUCT) {
                                ProductInfoDto targetProduct = productManagementService.getProductById(line.getTargetId());
                                cell7.setCellValue(targetProduct.getUnitOfMeasure() != null ? targetProduct.getUnitOfMeasure() : "Cái");
                            } else if (line.getLineType() == PromotionLine.LineType.SERVICE) {
                                cell7.setCellValue("Dịch vụ");
                            }
                        }
                        cell7.setCellStyle(dataStyle);
                        
                        // Số tiền chiết khấu
                        Cell cell8 = row.createCell(8);
                        BigDecimal lineDiscount = calculateLineDiscount(line, promotion);
                        cell8.setCellValue(lineDiscount.doubleValue());
                        cell8.setCellStyle(currencyStyle);
                        
                        // Ngân sách tổng
                        Cell cell9 = row.createCell(9);
                        cell9.setCellValue(promotionBudget.doubleValue());
                        cell9.setCellStyle(currencyStyle);
                        
                        // Ngân sách đã sử dụng
                        Cell cell10 = row.createCell(10);
                        cell10.setCellValue(usedBudget.doubleValue());
                        cell10.setCellStyle(currencyStyle);
                        
                        // Ngân sách còn lại
                        Cell cell11 = row.createCell(11);
                        cell11.setCellValue(remainingBudget.doubleValue());
                        cell11.setCellStyle(currencyStyle);
                    }
                } else {
                    // If no promotion lines, create one row for the promotion
                    Row row = sheet.createRow(rowCount++);
                    
                    Cell cell0 = row.createCell(0);
                    cell0.setCellValue(promotion.getPromotionCode() != null ? promotion.getPromotionCode() : "");
                    cell0.setCellStyle(dataStyle);
                    
                    Cell cell1 = row.createCell(1);
                    cell1.setCellValue(promotion.getName());
                    cell1.setCellStyle(dataStyle);
                    
                    Cell cell2 = row.createCell(2);
                    if (promotion.getStartAt() != null) {
                        cell2.setCellValue(promotion.getStartAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    }
                    cell2.setCellStyle(dataStyle);
                    
                    Cell cell3 = row.createCell(3);
                    if (promotion.getEndAt() != null) {
                        cell3.setCellValue(promotion.getEndAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    }
                    cell3.setCellStyle(dataStyle);
                    
                    // Empty cells for product-related columns
                    for (int i = 4; i <= 7; i++) {
                        Cell cell = row.createCell(i);
                        cell.setCellStyle(dataStyle);
                    }
                    
                    // Discount amount
                    Cell cell8 = row.createCell(8);
                    cell8.setCellValue(usedBudget.doubleValue());
                    cell8.setCellStyle(currencyStyle);
                    
                    Cell cell9 = row.createCell(9);
                    cell9.setCellValue(promotionBudget.doubleValue());
                    cell9.setCellStyle(currencyStyle);
                    
                    Cell cell10 = row.createCell(10);
                    cell10.setCellValue(usedBudget.doubleValue());
                    cell10.setCellStyle(currencyStyle);
                    
                    Cell cell11 = row.createCell(11);
                    cell11.setCellValue(remainingBudget.doubleValue());
                    cell11.setCellStyle(currencyStyle);
                }
            }
            
            // Create total row
            Row totalRow = sheet.createRow(rowCount++);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Tổng CTKM");
            totalLabelCell.setCellStyle(headerStyle);
            
            // Empty cells
            for (int i = 1; i <= 7; i++) {
                Cell cell = totalRow.createCell(i);
                cell.setCellStyle(headerStyle);
            }
            
            // Total discount (leave empty or sum if needed)
            Cell totalDiscountCell = totalRow.createCell(8);
            totalDiscountCell.setCellValue(totalUsedBudget.doubleValue());
            totalDiscountCell.setCellStyle(currencyStyle);
            
            // Total budget
            Cell totalBudgetCell = totalRow.createCell(9);
            totalBudgetCell.setCellValue(totalBudget.doubleValue());
            totalBudgetCell.setCellStyle(currencyStyle);
            
            // Total used
            Cell totalUsedCell = totalRow.createCell(10);
            totalUsedCell.setCellValue(totalUsedBudget.doubleValue());
            totalUsedCell.setCellStyle(currencyStyle);
            
            // Total remaining
            Cell totalRemainingCell = totalRow.createCell(11);
            totalRemainingCell.setCellValue(totalRemainingBudget.doubleValue());
            totalRemainingCell.setCellStyle(currencyStyle);
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            // Prepare response
            String filename = String.format("BaoCaoTongKetCTKM_%s_%s_%s.xlsx",
                fromDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                toDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            httpHeaders.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(outputStream.toByteArray());
            
        } catch (Exception e) {
            log.error("Error exporting promotion summary report", e);
            throw new RuntimeException("Error exporting promotion summary report: " + e.getMessage());
        }
    }
    
    // Helper methods for styling
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }
    
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }
    
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    // Helper methods for calculations
    private BigDecimal calculatePromotionBudget(Promotion promotion) {
        // Calculate total budget based on usage limit and average discount
        if (promotion.getUsageLimit() != null) {
            BigDecimal avgDiscount = calculateAverageDiscount(promotion);
            return avgDiscount.multiply(BigDecimal.valueOf(promotion.getUsageLimit()));
        }
        // Default budget estimation
        return BigDecimal.valueOf(2000000); // 2,000,000 VND
    }
    
    private BigDecimal calculateUsedBudget(Promotion promotion) {
        // Sum all discount amounts from promotion usages
        if (promotion.getUsages() != null && !promotion.getUsages().isEmpty()) {
            return promotion.getUsages().stream()
                .map(PromotionUsage::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateAverageDiscount(Promotion promotion) {
        if (promotion.getUsages() != null && !promotion.getUsages().isEmpty()) {
            BigDecimal total = promotion.getUsages().stream()
                .map(PromotionUsage::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            return total.divide(BigDecimal.valueOf(promotion.getUsages().size()), RoundingMode.HALF_UP);
        }
        // Default discount from promotion lines
        if (promotion.getPromotionLines() != null && !promotion.getPromotionLines().isEmpty()) {
            PromotionLine firstLine = promotion.getPromotionLines().get(0);
            if (firstLine.getDiscountValue() != null) {
                return firstLine.getDiscountValue();
            }
        }
        return BigDecimal.valueOf(20000); // 20,000 VND default
    }
    
    private BigDecimal calculateLineDiscount(PromotionLine line, Promotion promotion) {
        // Calculate total discount for this line based on usage
        BigDecimal lineDiscount = BigDecimal.ZERO;
        
        if (promotion.getUsages() != null) {
            for (PromotionUsage usage : promotion.getUsages()) {
                if (usage.getPromotionLine() != null &&
                    usage.getPromotionLine().getPromotionLineId().equals(line.getPromotionLineId())) {
                    lineDiscount = lineDiscount.add(usage.getDiscountAmount());
                }
            }
        }
        
        return lineDiscount;
    }
}
