package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.PurchaseOrderInfoDto;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.PurchaseOrderLineInfoDto;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request.CreatePORequest;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.response.PurchaseHistoryResponse;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.PurchaseOrder;
import com.kltn.scsms_api_service.core.entity.PurchaseOrderLine;
import com.kltn.scsms_api_service.core.service.businessService.PurchasingBusinessService;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import com.kltn.scsms_api_service.core.service.entityService.PurchaseOrderEntityService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.PurchaseOrderLineMapper;
import com.kltn.scsms_api_service.mapper.PurchaseOrderMapper;
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
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Controller handling Purchase Order operations
 * Manages creation, submission, and receiving of purchase orders
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Purchase Order Management", description = "Purchase order management endpoints")
public class PurchaseOrdersController {
    private final PurchaseOrderLineMapper purchaseOrderLineMapper;
    
    private final PurchasingBusinessService purchasingBS;
    private final PurchaseOrderEntityService poES;
    private final BranchService branchES;
    
    private final PurchaseOrderMapper poMapper;
    
    @PostMapping("/po/create-po")
    @SwaggerOperation(
        summary = "Create purchase order",
        description = "Create a new purchase order with specified details")
    public ResponseEntity<ApiResponse<PurchaseOrderInfoDto>> createDraft(@RequestBody CreatePORequest req) {
        PurchaseOrder po = purchasingBS.createDraft(req);
        
        PurchaseOrderInfoDto poDto = poMapper.toPurchaseOrderInfoDto(poES.require(po.getId()));
        return ResponseBuilder.success("Purchase order draft created", poDto);
    }
    
    
    @GetMapping("/po/{poId}")
    @SwaggerOperation(
        summary = "Get purchase order by ID",
        description = "Retrieve purchase order details using its unique identifier")
    public ResponseEntity<ApiResponse<PurchaseOrderInfoDto>> get(@PathVariable UUID poId) {
        PurchaseOrder po = poES.require(poId);
        
        PurchaseOrderInfoDto poDto = poMapper.toPurchaseOrderInfoDto(po);
        
        return ResponseBuilder.success("Purchase order retrieved", poDto);
    }
    
    @GetMapping("/po/get-all")
    @SwaggerOperation(
        summary = "Get all purchase orders",
        description = "Retrieve a list of all purchase orders in the system")
    public ResponseEntity<ApiResponse<List<PurchaseOrderInfoDto>>> getAll() {
        List<PurchaseOrder> pos = poES.getAll();
        
        List<PurchaseOrderInfoDto> posDto = pos.stream()
            .map(poMapper::toPurchaseOrderInfoDto).collect(Collectors.toList());
        
        return ResponseBuilder.success("All purchase orders retrieved", posDto);
    }
    
    @GetMapping("po/purchase-history/{productId}")
    @SwaggerOperation(
        summary = "Get purchase history by product ID",
        description = "Retrieve purchase order lines associated with a specific product")
    public ResponseEntity<ApiResponse<PurchaseHistoryResponse>> purchaseHistory(@PathVariable UUID productId) {
        List<PurchaseOrderLine> pols = purchasingBS.getProductPOHistory(productId);
        List<PurchaseOrderLineInfoDto> polsDto = pols.stream().map(purchaseOrderLineMapper::toPurchaseOrderLineInfoDto).collect(Collectors.toList());
        
        // Get peak unit cost
        AtomicReference<BigDecimal> peakUnitCost = new AtomicReference<>(BigDecimal.ZERO);
        polsDto.stream().filter(p -> p.getUnitCost() != null)
            .forEach(p -> {
                if (p.getUnitCost().compareTo(peakUnitCost.get()) > 0) {
                    peakUnitCost.set(p.getUnitCost());
                }
            });
        
        PurchaseHistoryResponse polsResponse = PurchaseHistoryResponse.builder()
            .lines(polsDto)
            .peakUnitCost(peakUnitCost.get())
            .build();
        
        return ResponseBuilder.success("Purchase history retrieved", polsResponse);
    }
    
    @GetMapping("/po/export-purchase-report")
    @SwaggerOperation(
        summary = "Export purchase order report to Excel",
        description = "Export purchase order items report with date range and branch filters")
    public ResponseEntity<byte[]> exportPurchaseReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(required = false) UUID branchId) {
        
        try {
            // Get filtered purchase orders
            List<PurchaseOrder> purchaseOrders = purchasingBS.getPurchaseOrdersByDateAndBranch(
                fromDate.atStartOfDay(),
                toDate.atTime(23, 59, 59),
                branchId
            );
            
            // Group products and sum quantities
            Map<String, ProductSummary> productMap = new LinkedHashMap<>();
            
            for (PurchaseOrder po : purchaseOrders) {
                for (PurchaseOrderLine line : po.getLines()) {
                    String productCode = line.getProduct() != null && line.getProduct().getBarcode() != null ?
                        line.getProduct().getBarcode() : "";
                    
                    if (!productCode.isEmpty()) {
                        ProductSummary summary = productMap.getOrDefault(productCode, new ProductSummary());
                        summary.productCode = productCode;
                        summary.productName = line.getProduct().getProductName() != null ?
                            line.getProduct().getProductName() : "";
                        summary.unitOfMeasure = line.getProduct().getUnitOfMeasure() != null ?
                            line.getProduct().getUnitOfMeasure() : "";
                        summary.unitCost = line.getUnitCost() != null ? line.getUnitCost() : BigDecimal.ZERO;
                        summary.totalQuantity = summary.totalQuantity.add(
                            line.getQuantityOrdered() != null ? BigDecimal.valueOf(line.getQuantityOrdered()) : BigDecimal.ZERO
                        );
                        
                        productMap.put(productCode, summary);
                    }
                }
            }
            
            // Row counter
            int rowCount = 0;
            
            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Bảng kê hàng nhập");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            
            // Create number style
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.cloneStyleFrom(dataStyle);
            DataFormat format = workbook.createDataFormat();
            numberStyle.setDataFormat(format.getFormat("#,##0"));
            
            // Create currency style
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.cloneStyleFrom(dataStyle);
            currencyStyle.setDataFormat(format.getFormat("#,##0"));
            
            // Create title row
            Row titleRow = sheet.createRow(rowCount++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BẢNG KÊ HÀNG HÓA NHẬP VÀO");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
            
            // Create info rows
            Row periodRow = sheet.createRow(rowCount++);
            periodRow.createCell(0).setCellValue("Từ ngày: " + fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                " - Đến ngày: " + toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            Row generatedRow = sheet.createRow(rowCount++);
            generatedRow.createCell(0).setCellValue("Ngày in: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            if (branchId != null) {
                Branch branch = branchES.findById(branchId).orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                    "Branch not found: " + branchId));
                
                Row branchRow = sheet.createRow(rowCount++);
                branchRow.createCell(0).setCellValue("Chi nhánh: " + branch.getBranchName());
                
                Row branchAddressRow = sheet.createRow(rowCount++);
                branchAddressRow.createCell(0).setCellValue("Địa chỉ: " + (branch.getAddress() != null ? branch.getAddress() : ""));
                
                Row branchPhoneRow = sheet.createRow(rowCount++);
                branchPhoneRow.createCell(0).setCellValue("SĐT: " + (branch.getPhone() != null ? branch.getPhone() : ""));
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
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            // Prepare response
            String filename = String.format("BangKeHangNhap_%s_%s_%s.xlsx",
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
            log.error("Error exporting purchase report", e);
            throw new RuntimeException("Error exporting purchase report: " + e.getMessage());
        }
    }
    
    // Inner class to hold product summary
    private static class ProductSummary {
        String productCode = "";
        String productName = "";
        String unitOfMeasure = "";
        BigDecimal unitCost = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;
    }
}
