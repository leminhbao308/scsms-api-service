package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.productManagement.ProductInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceInfoDto;
import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import com.kltn.scsms_api_service.core.service.businessService.*;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Unified Report Controller
 *
 * Migration status:
 * [ ] Sales Export Report
 * [ ] Sales Returns Report
 * [ ] Purchase Export Report
 * [ ] Inventory Export Report
 * [ ] Promotion Usage History
 * [ ] Promotion Usage Detail Export
 * [ ] Promotion Summary Export
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports & Analytics", description = "Unified reporting endpoints")
@RequestMapping("/reports")
public class ReportController {
    
    // ==================== SERVICES ====================
    private final SalesBusinessService salesBS;
    private final PurchasingBusinessService purchasingBS;
    private final PromotionManagementService promotionManagementService;
    private final ProductManagementService productManagementService;
    private final ServiceManagementService serviceManagementService;
    private final SalesOrderEntityService soES;
    private final SalesReturnEntityService srES;
    private final InventoryLotEntityService lotES;
    private final InventoryLevelEntityService invLevelES;
    private final BranchService branchES;
    
    // ==================== COMMON UTILITY METHODS ====================
    
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
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
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
    
    private CellStyle createSubtotalStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
    
    private ResponseEntity<byte[]> createExcelResponse(Workbook workbook, String filename) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
            .headers(httpHeaders)
            .body(outputStream.toByteArray());
    }
    
    private BigDecimal calculatePromotionBudget(Promotion promotion) {
        // Calculate total budget based on usage limit and average discount
        if (promotion.getUsageLimit() != null) {
            BigDecimal avgDiscount = calculateAverageDiscount(promotion);
            return avgDiscount.multiply(BigDecimal.valueOf(promotion.getUsageLimit()));
        }
        // Default budget estimation
        return BigDecimal.valueOf(0);
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
        return BigDecimal.valueOf(0);
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
    
    // ==================== SALES REPORTS ====================
    
    /**
     * Columns: STT, Tên NVBH, Ngày, Chiết khấu, Doanh số trước CK, Doanh số sau CK
     */
    @GetMapping("/sales/export")
    @SwaggerOperation(
        summary = "Export sales report to Excel",
        description = "Export sales revenue report with date range and branch filters, grouped by sales staff"
    )
    public ResponseEntity<byte[]> exportSalesReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(required = false) UUID branchId) {
        
        try {
            // Get filtered sales orders (only FULFILLED and CONFIRMED orders count as revenue)
            List<SalesOrder> salesOrders = salesBS.getSalesOrdersByDateAndBranch(
                fromDate.atStartOfDay(),
                toDate.atTime(23, 59, 59),
                branchId);
            
            // Group by sales staff and date
            Map<String, StaffSalesSummary> staffSalesMap = new LinkedHashMap<>();
            
            for (SalesOrder so : salesOrders) {
                // Only count FULFILLED orders for revenue
                if (so.getStatus() != SalesStatus.FULFILLED) {
                    continue;
                }
                
                // Get sales staff info (from createdBy or assigned staff)
                String staffName = so.getCreatedBy() != null ? so.getCreatedBy() : "SYSTEM";
                
                String saleDate = so.getCreatedDate() != null
                    ? so.getCreatedDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                
                String key = staffName + "|" + saleDate;
                
                StaffSalesSummary summary = staffSalesMap.getOrDefault(key, new StaffSalesSummary());
                summary.staffName = staffName;
                summary.saleDate = saleDate;
                
                // Calculate order amounts
                BigDecimal discountAmount = so.getTotalDiscountAmount() != null
                    ? so.getTotalDiscountAmount()
                    : BigDecimal.ZERO;
                
                BigDecimal revenueBeforeDiscount = so.getOriginalAmount() != null
                    ? so.getOriginalAmount()
                    : BigDecimal.ZERO;
                
                BigDecimal revenueAfterDiscount = so.getFinalAmount() != null
                    ? so.getFinalAmount()
                    : BigDecimal.ZERO;
                
                summary.totalDiscount = summary.totalDiscount.add(discountAmount);
                summary.revenueBeforeDiscount = summary.revenueBeforeDiscount.add(revenueBeforeDiscount);
                summary.revenueAfterDiscount = summary.revenueAfterDiscount.add(revenueAfterDiscount);
                
                staffSalesMap.put(key, summary);
            }
            
            // Group by staff for subtotals
            Map<String, List<StaffSalesSummary>> groupedByStaff = new LinkedHashMap<>();
            for (StaffSalesSummary summary : staffSalesMap.values()) {
                String staffKey = summary.staffName;
                groupedByStaff.computeIfAbsent(staffKey, k -> new ArrayList<>()).add(summary);
            }
            
            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Doanh số bán hàng");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle subtotalStyle = createSubtotalStyle(workbook);
            
            int rowCount = 0;
            
            // Create title
            Row titleRow = sheet.createRow(rowCount++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DOANH SỐ BÁN HÀNG THEO NGÀY");
            CellStyle titleStyle = createTitleStyle(workbook);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            
            // Create info rows
            Row dateRangeRow = sheet.createRow(rowCount++);
            dateRangeRow.createCell(0).setCellValue(
                "Từ ngày: " + fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " - Đến ngày: " + toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            Row generatedRow = sheet.createRow(rowCount++);
            generatedRow.createCell(0).setCellValue("Ngày in: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            if (branchId != null) {
                Branch branch = branchES.findById(branchId)
                    .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                        "Branch not found: " + branchId));
                
                Row branchRow = sheet.createRow(rowCount++);
                branchRow.createCell(0).setCellValue("Chi nhánh: " + branch.getBranchName());
                
                Row branchAddressRow = sheet.createRow(rowCount++);
                branchAddressRow.createCell(0).setCellValue(
                    "Địa chỉ: " + (branch.getAddress() != null ? branch.getAddress() : ""));
                
                Row branchPhoneRow = sheet.createRow(rowCount++);
                branchPhoneRow.createCell(0).setCellValue(
                    "SĐT: " + (branch.getPhone() != null ? branch.getPhone() : ""));
            } else {
                Row branchRow = sheet.createRow(rowCount++);
                branchRow.createCell(0).setCellValue("Phạm vi: Toàn hệ thống");
            }
            rowCount++; // Empty row
            
            // Create header row
            Row headerRow = sheet.createRow(rowCount++);
            String[] headers = {
                "STT", "Tên NVBH", "Ngày", "Chiết khấu",
                "Doanh số trước CK", "Doanh số sau CK"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Fill data grouped by staff
            int stt = 1;
            BigDecimal grandTotalDiscount = BigDecimal.ZERO;
            BigDecimal grandTotalBeforeDiscount = BigDecimal.ZERO;
            BigDecimal grandTotalAfterDiscount = BigDecimal.ZERO;
            
            for (Map.Entry<String, List<StaffSalesSummary>> entry : groupedByStaff.entrySet()) {
                String staffName = entry.getKey();
                List<StaffSalesSummary> staffSales = entry.getValue();
                
                BigDecimal staffTotalDiscount = BigDecimal.ZERO;
                BigDecimal staffTotalBeforeDiscount = BigDecimal.ZERO;
                BigDecimal staffTotalAfterDiscount = BigDecimal.ZERO;
                
                // Write each date row for this staff
                for (StaffSalesSummary summary : staffSales) {
                    Row row = sheet.createRow(rowCount++);
                    
                    Cell cell0 = row.createCell(0);
                    cell0.setCellValue(stt++);
                    cell0.setCellStyle(dataStyle);
                    
                    Cell cell2 = row.createCell(1);
                    cell2.setCellValue(staffName);
                    cell2.setCellStyle(dataStyle);
                    
                    Cell cell3 = row.createCell(2);
                    cell3.setCellValue(summary.saleDate);
                    cell3.setCellStyle(dataStyle);
                    
                    Cell cell4 = row.createCell(3);
                    cell4.setCellValue(summary.totalDiscount.doubleValue());
                    cell4.setCellStyle(currencyStyle);
                    
                    Cell cell5 = row.createCell(4);
                    cell5.setCellValue(summary.revenueBeforeDiscount.doubleValue());
                    cell5.setCellStyle(currencyStyle);
                    
                    Cell cell6 = row.createCell(5);
                    cell6.setCellValue(summary.revenueAfterDiscount.doubleValue());
                    cell6.setCellStyle(currencyStyle);
                    
                    staffTotalDiscount = staffTotalDiscount.add(summary.totalDiscount);
                    staffTotalBeforeDiscount = staffTotalBeforeDiscount.add(summary.revenueBeforeDiscount);
                    staffTotalAfterDiscount = staffTotalAfterDiscount.add(summary.revenueAfterDiscount);
                }
                
                // Write staff subtotal row
                Row subtotalRow = sheet.createRow(rowCount++);
                
                Cell stCell0 = subtotalRow.createCell(0);
                stCell0.setCellStyle(subtotalStyle);
                
                Cell stCell2 = subtotalRow.createCell(1);
                stCell2.setCellValue(staffName);
                stCell2.setCellStyle(subtotalStyle);
                
                Cell stCell3 = subtotalRow.createCell(2);
                stCell3.setCellValue("Tổng cộng");
                stCell3.setCellStyle(subtotalStyle);
                
                Cell stCell4 = subtotalRow.createCell(3);
                stCell4.setCellValue(staffTotalDiscount.doubleValue());
                stCell4.setCellStyle(subtotalStyle);
                
                Cell stCell5 = subtotalRow.createCell(4);
                stCell5.setCellValue(staffTotalBeforeDiscount.doubleValue());
                stCell5.setCellStyle(subtotalStyle);
                
                Cell stCell6 = subtotalRow.createCell(5);
                stCell6.setCellValue(staffTotalAfterDiscount.doubleValue());
                stCell6.setCellStyle(subtotalStyle);
                
                grandTotalDiscount = grandTotalDiscount.add(staffTotalDiscount);
                grandTotalBeforeDiscount = grandTotalBeforeDiscount.add(staffTotalBeforeDiscount);
                grandTotalAfterDiscount = grandTotalAfterDiscount.add(staffTotalAfterDiscount);
            }
            
            // Create grand total row
            Row grandTotalRow = sheet.createRow(rowCount);
            
            Cell gtCell3 = grandTotalRow.createCell(2);
            gtCell3.setCellValue("Tổng cộng");
            CellStyle grandTotalLabelStyle = workbook.createCellStyle();
            grandTotalLabelStyle.cloneStyleFrom(subtotalStyle);
            Font grandTotalFont = workbook.createFont();
            grandTotalFont.setBold(true);
            grandTotalFont.setFontHeightInPoints((short) 12);
            grandTotalLabelStyle.setFont(grandTotalFont);
            grandTotalLabelStyle.setAlignment(HorizontalAlignment.RIGHT);
            gtCell3.setCellStyle(grandTotalLabelStyle);
            
            CellStyle grandTotalValueStyle = workbook.createCellStyle();
            grandTotalValueStyle.cloneStyleFrom(subtotalStyle);
            grandTotalValueStyle.setFont(grandTotalFont);
            
            Cell gtCell4 = grandTotalRow.createCell(3);
            gtCell4.setCellValue(grandTotalDiscount.doubleValue());
            gtCell4.setCellStyle(grandTotalValueStyle);
            
            Cell gtCell5 = grandTotalRow.createCell(4);
            gtCell5.setCellValue(grandTotalBeforeDiscount.doubleValue());
            gtCell5.setCellStyle(grandTotalValueStyle);
            
            Cell gtCell6 = grandTotalRow.createCell(5);
            gtCell6.setCellValue(grandTotalAfterDiscount.doubleValue());
            gtCell6.setCellStyle(grandTotalValueStyle);
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }
            
            // Prepare response
            String filename = String.format("DoanhSoBanHang_%s_%s_%s.xlsx",
                fromDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                toDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            
            return createExcelResponse(workbook, filename);
            
        } catch (Exception e) {
            log.error("Error exporting sales report", e);
            throw new RuntimeException("Error exporting sales report: " + e.getMessage());
        }
    }
    
    /**
     * Columns: STT, Ngày trả, Mã trả hàng, Mã đơn hàng, Khách hàng, Chi nhánh, Lý do, Tổng SL, Giá trị trả
     */
    @GetMapping("/sales/export-returns")
    @SwaggerOperation(
        summary = "Export sales returns report to Excel",
        description = "Export detailed sales returns report with date range filter for entire system"
    )
    public ResponseEntity<byte[]> exportReturnsReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        try {
            // Get all sales returns within date range
            List<SalesReturn> salesReturns = srES.getSalesReturnsByDateRange(
                fromDate.atStartOfDay(),
                toDate.atTime(23, 59, 59));
            
            log.info("Found {} sales returns for report from {} to {}",
                salesReturns.size(), fromDate, toDate);
            
            // Group returns by date and sales order
            Map<String, ReturnSummary> returnSummaryMap = new LinkedHashMap<>();
            
            BigDecimal grandTotalReturnValue = BigDecimal.ZERO;
            long grandTotalItems = 0;
            
            for (SalesReturn sr : salesReturns) {
                String returnDate = sr.getCreatedDate() != null
                    ? sr.getCreatedDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                
                String key = returnDate + "|" + sr.getId().toString();
                
                ReturnSummary summary = new ReturnSummary();
                summary.returnDate = returnDate;
                summary.returnId = sr.getId().toString();
                summary.salesOrderId = sr.getSalesOrder().getId().toString();
                summary.reason = sr.getReason() != null ? sr.getReason() : "N/A";
                summary.branchName = sr.getBranch() != null ? sr.getBranch().getBranchName() : "N/A";
                summary.customerName = sr.getSalesOrder().getCustomer() != null
                    ? sr.getSalesOrder().getCustomer().getFullName()
                    : "Guest";
                
                // Calculate return value based on returned items
                BigDecimal returnValue = BigDecimal.ZERO;
                long totalItems = 0;
                
                for (SalesReturnLine line : sr.getLines()) {
                    // Find original order line to get unit price
                    SalesOrderLine originalLine = sr.getSalesOrder().getLines().stream()
                        .filter(sol -> sol.getProduct() != null &&
                            sol.getProduct().getProductId().equals(line.getProduct().getProductId()))
                        .findFirst()
                        .orElse(null);
                    
                    if (originalLine != null) {
                        // Calculate using discount ratio from original order
                        BigDecimal discountRatio = BigDecimal.ONE;
                        if (sr.getSalesOrder().getOriginalAmount() != null &&
                            sr.getSalesOrder().getFinalAmount() != null &&
                            sr.getSalesOrder().getOriginalAmount().compareTo(BigDecimal.ZERO) > 0) {
                            discountRatio = sr.getSalesOrder().getFinalAmount()
                                .divide(sr.getSalesOrder().getOriginalAmount(), 4, RoundingMode.HALF_UP);
                        }
                        
                        BigDecimal lineReturnValue = originalLine.getUnitPrice()
                            .multiply(BigDecimal.valueOf(line.getQuantity()))
                            .multiply(discountRatio)
                            .setScale(0, RoundingMode.HALF_UP);
                        
                        returnValue = returnValue.add(lineReturnValue);
                    }
                    
                    totalItems += line.getQuantity();
                }
                
                summary.returnValue = returnValue;
                summary.totalItems = totalItems;
                
                grandTotalReturnValue = grandTotalReturnValue.add(returnValue);
                grandTotalItems += totalItems;
                
                returnSummaryMap.put(key, summary);
            }
            
            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Báo cáo trả hàng");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle totalStyle = createSubtotalStyle(workbook);
            
            int rowCount = 0;
            
            // Create title
            Row titleRow = sheet.createRow(rowCount++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO TRẢ HÀNG");
            CellStyle titleStyle = createTitleStyle(workbook);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
            
            // Create info rows
            Row dateRangeRow = sheet.createRow(rowCount++);
            dateRangeRow.createCell(0).setCellValue(
                "Từ ngày: " + fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " - Đến ngày: " + toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            Row generatedRow = sheet.createRow(rowCount++);
            generatedRow.createCell(0).setCellValue("Ngày in: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            Row scopeRow = sheet.createRow(rowCount++);
            scopeRow.createCell(0).setCellValue("Phạm vi: Toàn hệ thống");
            
            rowCount++; // Empty row
            
            // Create header row
            Row headerRow = sheet.createRow(rowCount++);
            String[] headers = {
                "STT", "Ngày trả", "Mã trả hàng", "Mã đơn hàng",
                "Khách hàng", "Chi nhánh", "Lý do", "Số lượng SP", "Giá trị trả"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Fill data
            int stt = 1;
            for (ReturnSummary summary : returnSummaryMap.values()) {
                Row row = sheet.createRow(rowCount++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(stt++);
                cell0.setCellStyle(dataStyle);
                
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(summary.returnDate);
                cell1.setCellStyle(dataStyle);
                
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(summary.returnId);
                cell2.setCellStyle(dataStyle);
                
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(summary.salesOrderId);
                cell3.setCellStyle(dataStyle);
                
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(summary.customerName);
                cell4.setCellStyle(dataStyle);
                
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(summary.branchName);
                cell5.setCellStyle(dataStyle);
                
                Cell cell6 = row.createCell(6);
                cell6.setCellValue(summary.reason);
                cell6.setCellStyle(dataStyle);
                
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(summary.totalItems);
                cell7.setCellStyle(dataStyle);
                
                Cell cell8 = row.createCell(8);
                cell8.setCellValue(summary.returnValue.doubleValue());
                cell8.setCellStyle(currencyStyle);
            }
            
            // Create grand total row
            Row grandTotalRow = sheet.createRow(rowCount);
            
            Cell gtCell6 = grandTotalRow.createCell(6);
            gtCell6.setCellValue("Tổng cộng");
            gtCell6.setCellStyle(totalStyle);
            
            Cell gtCell7 = grandTotalRow.createCell(7);
            gtCell7.setCellValue(grandTotalItems);
            gtCell7.setCellStyle(totalStyle);
            
            Cell gtCell8 = grandTotalRow.createCell(8);
            gtCell8.setCellValue(grandTotalReturnValue.doubleValue());
            gtCell8.setCellStyle(totalStyle);
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }
            
            // Prepare response
            String filename = String.format("BaoCaoTraHang_%s_%s_%s.xlsx",
                fromDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                toDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            
            return createExcelResponse(workbook, filename);
            
        } catch (Exception e) {
            log.error("Error exporting sales returns report", e);
            throw new RuntimeException("Error exporting sales returns report: " + e.getMessage());
        }
    }
    
    // ==================== PURCHASE REPORTS ====================
    
    /**
     * Columns: STT, Mã sản phẩm, Tên sản phẩm, Số lượng, Đơn vị tính, Đơn giá, Thành tiền
     */
    @GetMapping("/purchase/export")
    @SwaggerOperation(
        summary = "Export purchase order report to Excel",
        description = "Export purchase order items report with date range and branch filters"
    )
    public ResponseEntity<byte[]> exportPurchaseReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(required = false) UUID branchId) {
        
        try {
            // Get filtered purchase orders
            List<PurchaseOrder> purchaseOrders = purchasingBS.getPurchaseOrdersByDateAndBranch(
                fromDate.atStartOfDay(),
                toDate.atTime(23, 59, 59),
                branchId);
            
            // Group products and sum quantities
            Map<String, ProductSummary> productMap = new LinkedHashMap<>();
            
            for (PurchaseOrder po : purchaseOrders) {
                for (PurchaseOrderLine line : po.getLines()) {
                    String productCode = line.getProduct() != null && line.getProduct().getBarcode() != null
                        ? line.getProduct().getBarcode()
                        : "";
                    
                    if (!productCode.isEmpty()) {
                        ProductSummary summary = productMap.getOrDefault(productCode, new ProductSummary());
                        summary.productCode = productCode;
                        summary.productName = line.getProduct().getProductName() != null
                            ? line.getProduct().getProductName()
                            : "";
                        summary.unitOfMeasure = line.getProduct().getUnitOfMeasure() != null
                            ? line.getProduct().getUnitOfMeasure()
                            : "";
                        summary.unitCost = line.getUnitCost() != null ? line.getUnitCost() : BigDecimal.ZERO;
                        summary.totalQuantity = summary.totalQuantity.add(
                            line.getQuantityOrdered() != null ? BigDecimal.valueOf(line.getQuantityOrdered())
                                : BigDecimal.ZERO);
                        
                        productMap.put(productCode, summary);
                    }
                }
            }
            
            // Row counter
            int rowCount = 0;
            
            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Bảng kê hàng nhập");
            
            // Create styles - use common methods
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            // Create title row
            Row titleRow = sheet.createRow(rowCount++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BẢNG KÊ HÀNG HÓA NHẬP VÀO");
            CellStyle titleStyle = createTitleStyle(workbook);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
            
            // Create info rows
            Row periodRow = sheet.createRow(rowCount++);
            periodRow.createCell(0)
                .setCellValue("Từ ngày: " + fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " - Đến ngày: " + toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            Row generatedRow = sheet.createRow(rowCount++);
            generatedRow.createCell(0).setCellValue("Ngày in: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            if (branchId != null) {
                Branch branch = branchES.findById(branchId)
                    .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                        "Branch not found: " + branchId));
                
                Row branchRow = sheet.createRow(rowCount++);
                branchRow.createCell(0).setCellValue("Chi nhánh: " + branch.getBranchName());
                
                Row branchAddressRow = sheet.createRow(rowCount++);
                branchAddressRow.createCell(0)
                    .setCellValue("Địa chỉ: " + (branch.getAddress() != null ? branch.getAddress() : ""));
                
                Row branchPhoneRow = sheet.createRow(rowCount++);
                branchPhoneRow.createCell(0)
                    .setCellValue("SĐT: " + (branch.getPhone() != null ? branch.getPhone() : ""));
            } else {
                Row branchRow = sheet.createRow(rowCount++);
                branchRow.createCell(0).setCellValue("Phạm vi: Toàn hệ thống");
            }
            rowCount++; // Empty row
            
            // Create header row
            Row headerRow = sheet.createRow(rowCount++);
            String[] headers = {
                "STT", "Mã sản phẩm", "Tên sản phẩm", "Số lượng", "Đơn vị tính", "Đơn giá", "Thành tiền"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Fill data
            int stt = 1;
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (ProductSummary summary : productMap.values()) {
                Row row = sheet.createRow(rowCount++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(stt++);
                cell0.setCellStyle(dataStyle);
                
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(summary.productCode);
                cell1.setCellStyle(dataStyle);
                
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(summary.productName);
                cell2.setCellStyle(dataStyle);
                
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(summary.totalQuantity.doubleValue());
                cell3.setCellStyle(numberStyle);
                
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(summary.unitOfMeasure);
                cell4.setCellStyle(dataStyle);
                
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(summary.unitCost.doubleValue());
                cell5.setCellStyle(currencyStyle);
                
                BigDecimal lineTotal = summary.totalQuantity.multiply(summary.unitCost);
                Cell cell6 = row.createCell(6);
                cell6.setCellValue(lineTotal.doubleValue());
                cell6.setCellStyle(currencyStyle);
                
                totalAmount = totalAmount.add(lineTotal);
            }
            
            // Create total row
            Row totalRow = sheet.createRow(rowCount);
            Cell totalLabelCell = totalRow.createCell(5);
            totalLabelCell.setCellValue("TỔNG CỘNG:");
            CellStyle totalLabelStyle = workbook.createCellStyle();
            totalLabelStyle.cloneStyleFrom(headerStyle);
            totalLabelStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalLabelCell.setCellStyle(totalLabelStyle);
            
            Cell totalAmountCell = totalRow.createCell(6);
            totalAmountCell.setCellValue(totalAmount.doubleValue());
            CellStyle totalAmountStyle = workbook.createCellStyle();
            totalAmountStyle.cloneStyleFrom(currencyStyle);
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalAmountStyle.setFont(totalFont);
            totalAmountCell.setCellStyle(totalAmountStyle);
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }
            
            // Prepare response
            String filename = String.format("BangKeHangNhap_%s_%s_%s.xlsx",
                fromDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                toDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            
            return createExcelResponse(workbook, filename);
            
        } catch (Exception e) {
            log.error("Error exporting purchase report", e);
            throw new RuntimeException("Error exporting purchase report: " + e.getMessage());
        }
    }
    
    // ==================== INVENTORY REPORTS ====================
    
    /**
     * Columns: STT, Chi nhánh, Mã SP, Tên sản phẩm, Loại SP, Thương hiệu, Mã lô, Nhà cung cấp,
     *          Ngày nhập, Hạn sử dụng, SL lô, ĐVT, Đơn giá, Giá trị lô, Tổng tồn, Đang giữ, Có thể bán
     */
    @GetMapping("/inventory/export")
    @SwaggerOperation(
        summary = "Export inventory report to Excel",
        description = "Export inventory report with on-hand, reserved, and available quantities"
    )
    public ResponseEntity<byte[]> exportInventoryReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate,
        @RequestParam UUID branchId) {
        
        try {
            // Get branch info
            Branch branch = branchES.findById(branchId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Branch not found"));
            
            // Get all inventory lots for the branch
            List<InventoryLot> inventoryLots = lotES.findByBranch(branch.getBranchId());
            
            // Build report data - one line per lot
            List<InventoryReportLine> reportLines = new ArrayList<>();
            
            for (InventoryLot lot : inventoryLots) {
                // Skip empty lots
                if (lot.getQuantity() == null || lot.getQuantity() <= 0) continue;
                
                Product product = lot.getProduct();
                if (product == null) continue;
                
                InventoryReportLine line = new InventoryReportLine();
                line.branchName = branch.getBranchName();
                line.productCode = product.getBarcode() != null ? product.getBarcode() :
                    (product.getSku() != null ? product.getSku() : "");
                line.productName = product.getProductName() != null ? product.getProductName() : "";
                line.productType = product.getProductType() != null ?
                    product.getProductType().getProductTypeName() : "";
                line.brand = product.getBrand() != null ? product.getBrand() : "";
                line.unitOfMeasure = product.getUnitOfMeasure() != null ? product.getUnitOfMeasure() : "";
                
                // Lot specific information
                line.lotCode = lot.getLotCode() != null ? lot.getLotCode() : "";
                line.receivedDate = lot.getReceivedAt() != null ?
                    lot.getReceivedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                line.expiryDate = lot.getExpiryDate() != null ?
                    lot.getExpiryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                line.supplierName = lot.getSupplier() != null ? lot.getSupplier().getSupplierName() : "";
                line.quantity = lot.getQuantity();
                line.unitCost = lot.getUnitCost() != null ? lot.getUnitCost() : BigDecimal.ZERO;
                line.totalValue = line.unitCost.multiply(BigDecimal.valueOf(line.quantity));
                
                // Get reserved quantity for this product in this warehouse
                InventoryLevel level = invLevelES.find(lot.getBranch().getBranchId(), product.getProductId())
                    .orElse(null);
                if (level != null) {
                    line.totalOnHand = level.getOnHand() != null ? level.getOnHand() : 0L;
                    line.totalReserved = level.getReserved() != null ? level.getReserved() : 0L;
                    line.totalAvailable = level.getAvailable();
                }
                
                reportLines.add(line);
            }
            
            // Sort by warehouse, product code, then received date
            reportLines.sort((a, b) -> {
                int whCompare = a.branchName.compareTo(b.branchName);
                if (whCompare != 0) return whCompare;
                
                int productCompare = a.productCode.compareTo(b.productCode);
                if (productCompare != 0) return productCompare;
                
                return a.receivedDate.compareTo(b.receivedDate);
            });
            
            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Báo cáo tồn kho chi tiết");
            
            // Create styles - use common methods + some custom
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            // Add wrap text for header
            headerStyle.setWrapText(true);
            
            CellStyle dataStyle = createDataStyle(workbook);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            CellStyle numberStyle = createNumberStyle(workbook);
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);
            
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.cloneStyleFrom(dataStyle);
            dateStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // Row counter
            int rowNum = 0;
            
            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO TỒN KHO CHI TIẾT THEO LÔ");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 16));
            
            // Report date
            Row dateRow = sheet.createRow(rowNum++);
            dateRow.createCell(0).setCellValue("Ngày báo cáo: " +
                reportDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            // Print date
            Row printDateRow = sheet.createRow(rowNum++);
            printDateRow.createCell(0).setCellValue("Ngày in: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            // Branch info
            Row branchRow = sheet.createRow(rowNum++);
            branchRow.createCell(0).setCellValue("Chi nhánh: " + branch.getBranchName());
            
            if (branch.getAddress() != null) {
                Row addressRow = sheet.createRow(rowNum++);
                addressRow.createCell(0).setCellValue("Địa chỉ: " + branch.getAddress());
            }
            
            rowNum++; // Empty row
            
            // Create header row - 17 columns!
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {
                "STT",
                "Kho",
                "Mã SP",
                "Tên sản phẩm",
                "Loại SP",
                "Thương hiệu",
                "ĐVT",
                "Mã lô",
                "Nhà cung cấp",
                "Ngày nhập",
                "Hạn sử dụng",
                "SL lô",
                "Đơn giá",
                "Giá trị lô",
                "Tổng tồn",
                "Đang giữ",
                "Có thể bán"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Fill data
            int stt = 1;
            BigDecimal totalInventoryValue = BigDecimal.ZERO;
            long totalQuantity = 0L;
            
            String currentProduct = "";
            
            for (InventoryReportLine line : reportLines) {
                Row dataRow = sheet.createRow(rowNum++);
                
                // Check if this is a new product (for grouping visual)
                boolean isNewProduct = !currentProduct.equals(line.productCode);
                if (isNewProduct) {
                    currentProduct = line.productCode;
                }
                
                // STT
                Cell cell0 = dataRow.createCell(0);
                cell0.setCellValue(stt++);
                cell0.setCellStyle(dataStyle);
                
                // Warehouse
                Cell cell1 = dataRow.createCell(1);
                cell1.setCellValue(line.branchName);
                cell1.setCellStyle(dataStyle);
                
                // Product Code
                Cell cell2 = dataRow.createCell(2);
                cell2.setCellValue(line.productCode);
                cell2.setCellStyle(dataStyle);
                
                // Product Name
                Cell cell3 = dataRow.createCell(3);
                cell3.setCellValue(line.productName);
                cell3.setCellStyle(dataStyle);
                
                // Product Type
                Cell cell4 = dataRow.createCell(4);
                cell4.setCellValue(line.productType);
                cell4.setCellStyle(dataStyle);
                
                // Brand
                Cell cell5 = dataRow.createCell(5);
                cell5.setCellValue(line.brand);
                cell5.setCellStyle(dataStyle);
                
                // Unit of Measure
                Cell cell6 = dataRow.createCell(6);
                cell6.setCellValue(line.unitOfMeasure);
                cell6.setCellStyle(dataStyle);
                
                // Lot Code
                Cell cell7 = dataRow.createCell(7);
                cell7.setCellValue(line.lotCode);
                cell7.setCellStyle(dataStyle);
                
                // Supplier
                Cell cell8 = dataRow.createCell(8);
                cell8.setCellValue(line.supplierName);
                cell8.setCellStyle(dataStyle);
                
                // Received Date
                Cell cell9 = dataRow.createCell(9);
                cell9.setCellValue(line.receivedDate);
                cell9.setCellStyle(dateStyle);
                
                // Expiry Date
                Cell cell10 = dataRow.createCell(10);
                cell10.setCellValue(line.expiryDate);
                cell10.setCellStyle(dateStyle);
                
                // Lot Quantity
                Cell cell11 = dataRow.createCell(11);
                cell11.setCellValue(line.quantity);
                cell11.setCellStyle(numberStyle);
                totalQuantity += line.quantity;
                
                // Unit Cost
                Cell cell12 = dataRow.createCell(12);
                cell12.setCellValue(line.unitCost.doubleValue());
                cell12.setCellStyle(currencyStyle);
                
                // Total Value (for this lot)
                Cell cell13 = dataRow.createCell(13);
                cell13.setCellValue(line.totalValue.doubleValue());
                cell13.setCellStyle(currencyStyle);
                totalInventoryValue = totalInventoryValue.add(line.totalValue);
                
                // Total On Hand (show only for first lot of each product)
                Cell cell14 = dataRow.createCell(14);
                if (isNewProduct) {
                    cell14.setCellValue(line.totalOnHand);
                } else {
                    cell14.setCellValue("");
                }
                cell14.setCellStyle(numberStyle);
                
                // Total Reserved (show only for first lot of each product)
                Cell cell15 = dataRow.createCell(15);
                if (isNewProduct) {
                    cell15.setCellValue(line.totalReserved);
                } else {
                    cell15.setCellValue("");
                }
                cell15.setCellStyle(numberStyle);
                
                // Total Available (show only for first lot of each product)
                Cell cell16 = dataRow.createCell(16);
                if (isNewProduct) {
                    cell16.setCellValue(line.totalAvailable);
                } else {
                    cell16.setCellValue("");
                }
                cell16.setCellStyle(numberStyle);
            }
            
            // Create summary row
            rowNum++; // Empty row
            Row summaryRow = sheet.createRow(rowNum++);
            
            Cell summaryLabelCell = summaryRow.createCell(10);
            summaryLabelCell.setCellValue("TỔNG CỘNG:");
            CellStyle summaryLabelStyle = workbook.createCellStyle();
            summaryLabelStyle.cloneStyleFrom(headerStyle);
            summaryLabelStyle.setAlignment(HorizontalAlignment.RIGHT);
            summaryLabelCell.setCellStyle(summaryLabelStyle);
            
            // Total Quantity (sum of all lots)
            Cell totalQtyCell = summaryRow.createCell(11);
            totalQtyCell.setCellValue(totalQuantity);
            CellStyle totalNumberStyle = workbook.createCellStyle();
            totalNumberStyle.cloneStyleFrom(numberStyle);
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalNumberStyle.setFont(totalFont);
            totalQtyCell.setCellStyle(totalNumberStyle);
            
            // Empty cell
            summaryRow.createCell(12).setCellStyle(dataStyle);
            
            // Total Value
            Cell totalValueCell = summaryRow.createCell(13);
            totalValueCell.setCellValue(totalInventoryValue.doubleValue());
            CellStyle totalCurrencyStyle = workbook.createCellStyle();
            totalCurrencyStyle.cloneStyleFrom(currencyStyle);
            totalCurrencyStyle.setFont(totalFont);
            totalValueCell.setCellStyle(totalCurrencyStyle);
            
            // Add notes
            rowNum += 2;
            Row note1Row = sheet.createRow(rowNum++);
            Cell noteHeaderCell = note1Row.createCell(0);
            noteHeaderCell.setCellValue("Ghi chú:");
            Font noteFont = workbook.createFont();
            noteFont.setBold(true);
            CellStyle noteHeaderStyle = workbook.createCellStyle();
            noteHeaderStyle.setFont(noteFont);
            noteHeaderCell.setCellStyle(noteHeaderStyle);
            
            sheet.createRow(rowNum++).createCell(0).setCellValue("- Báo cáo hiển thị chi tiết từng lô hàng tồn kho");
            sheet.createRow(rowNum++).createCell(0).setCellValue("- SL lô: Số lượng còn lại của lô hàng cụ thể");
            sheet.createRow(rowNum++).createCell(0).setCellValue("- Giá trị lô: Giá trị của lô = Số lượng lô × Đơn giá");
            sheet.createRow(rowNum++).createCell(0).setCellValue("- Tổng tồn: Tổng số lượng tất cả các lô của sản phẩm");
            sheet.createRow(rowNum++).createCell(0).setCellValue("- Đang giữ: Số lượng đã được đặt trước (reserved) cho các đơn hàng");
            sheet.createRow(rowNum++).createCell(0).setCellValue("- Có thể bán: Số lượng có thể bán = Tổng tồn - Đang giữ");
            sheet.createRow(rowNum++).createCell(0).setCellValue("- Hệ thống sử dụng phương pháp FIFO (First-In-First-Out) để xuất hàng");
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }
            
            // Prepare response
            String filename = String.format("BaoCaoTonKho_ChiTiet_%s_%s.xlsx",
                reportDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            
            return createExcelResponse(workbook, filename);
            
        } catch (Exception e) {
            log.error("Error exporting inventory report", e);
            throw new RuntimeException("Error exporting inventory report: " + e.getMessage());
        }
    }
    
    // ==================== PROMOTION REPORTS ====================
    
    /**
     * Columns: STT, Mã CTKM, Tên CTKM, Loại, Ngày bắt đầu, Ngày kết thúc, Giới hạn sử dụng, Đã sử dụng,
     *          Trung bình giảm giá, Ngân sách dự tính, Ngân sách đã dùng, Còn lại
     */
    @GetMapping("/promotion/export-summary")
    @SwaggerOperation(summary = "Export promotion summary report",
        description = "Export aggregated promotion summary showing budget usage and effectiveness")
    public ResponseEntity<byte[]> exportPromotionSummary(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(required = false) String promotionCode
        ) {
        
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
                            cell6.setCellValue(line.getFreeQuantity() * promotion.getUsageLimit());
                        } else if (line.getDiscountType() == PromotionLine.DiscountType.BUY_X_GET_Y) {
                            cell6.setCellValue(line.getGetQty() * promotion.getUsageLimit());
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
    
    // ==================== INNER CLASSES ====================
    
    private static class StaffSalesSummary {
        String staffName = "";
        String saleDate = "";
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal revenueBeforeDiscount = BigDecimal.ZERO;
        BigDecimal revenueAfterDiscount = BigDecimal.ZERO;
    }
    
    private static class ReturnSummary {
        String returnDate = "";
        String returnId = "";
        String salesOrderId = "";
        String customerName = "";
        String branchName = "";
        String reason = "";
        long totalItems = 0;
        BigDecimal returnValue = BigDecimal.ZERO;
    }
    
    private static class ProductSummary {
        String productCode = "";
        String productName = "";
        String unitOfMeasure = "";
        BigDecimal unitCost = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;
    }
    
    private static class InventoryReportLine {
        String branchName = "";
        String productCode = "";
        String productName = "";
        String productType = "";
        String brand = "";
        String unitOfMeasure = "";
        String lotCode = "";
        String supplierName = "";
        String receivedDate = "";
        String expiryDate = "";
        long quantity = 0L;
        BigDecimal unitCost = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;
        long totalOnHand = 0L;
        long totalReserved = 0L;
        long totalAvailable = 0L;
    }
}
