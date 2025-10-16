package com.kltn.scsms_api_service.core.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.inventoryManagement.InventoryLevelInfoDto;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.InventoryLevel;
import com.kltn.scsms_api_service.core.entity.InventoryLot;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType;
import com.kltn.scsms_api_service.core.service.businessService.InventoryBusinessService;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import com.kltn.scsms_api_service.core.service.entityService.InventoryLevelEntityService;
import com.kltn.scsms_api_service.core.service.entityService.InventoryLotEntityService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.InventoryLevelMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
import java.util.*;

/**
 * Controller handling Inventory operations
 * Manages stock levels, reservations, fulfillments, and returns
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Management", description = "Inventory management endpoints")
public class InventoryController {
    private final InventoryBusinessService inventoryBS;
    private final InventoryLevelEntityService invLevelES;
    private final BranchService branchES;
    private final InventoryLotEntityService lotES;
    
    private final InventoryLevelMapper invLevelMapper;
    
    
    @GetMapping("/inv/level")
    public ResponseEntity<ApiResponse<InventoryLevelInfoDto>> level(@RequestParam UUID branchId, @RequestParam UUID productId) {
        InventoryLevel invLevel = invLevelES.find(branchId, productId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Inventory level not found"));
        
        InventoryLevelInfoDto invLevelDto = invLevelMapper.toInventoryLevelInfoDto(invLevel);
        
        return ResponseBuilder.success("Fetch inventory level successfully", invLevelDto);
    }
    
    
    @PostMapping("/inv/add-stock")
    public ResponseEntity<ApiResponse<Void>> add(@RequestBody AddStockRequest req) {
        inventoryBS.addStock(req.getBranchId(), req.getProductId(), req.getQty(), req.getUnitCost(), req.getLotCode(), req.getRefId(), req.getRefType());
        return ResponseBuilder.success("Add stock successfully");
    }
    
    
    @PostMapping("/inv/reserve")
    public ResponseEntity<ApiResponse<Void>> reserve(@RequestBody MoveRequest req) {
        inventoryBS.reserveStock(req.getBranchId(), req.getProductId(), req.getQty(), req.getRefId(), req.getRefType());
        return ResponseBuilder.success("Reserve stock successfully");
    }
    
    
    @PostMapping("/inv/release")
    public ResponseEntity<ApiResponse<Void>> release(@RequestBody MoveRequest req) {
        inventoryBS.releaseReservation(req.getBranchId(), req.getProductId(), req.getQty(), req.getRefId(), req.getRefType());
        return ResponseBuilder.success("Release reservation successfully");
    }
    
    
    @PostMapping("/inv/fulfill")
    public ResponseEntity<ApiResponse<Void>> fulfill(@RequestBody MoveRequest req) {
        inventoryBS.fulfillStockFIFO(req.getBranchId(), req.getProductId(), req.getQty(), req.getRefId(), req.getRefType());
        return ResponseBuilder.success("Fulfill stock successfully");
    }
    
    
    @PostMapping("/inv/return")
    public ResponseEntity<ApiResponse<Void>> returnToStock(@RequestBody AddStockRequest req) {
        inventoryBS.returnToStock(req.getBranchId(), req.getProductId(), req.getQty(), req.getUnitCost(), req.getRefId(), req.getRefType());
        return ResponseBuilder.success("Return to stock successfully");
    }
    
    @PostMapping("/inv/levels-batch")
    public ResponseEntity<ApiResponse<InventoryLevelsBatchResponse>> levelsBatch(@RequestBody InventoryLevelsBatchRequest req) {
        Map<UUID, InventoryView> map = new LinkedHashMap<>();
        for (UUID productId : req.getProductIds()) {
            InventoryLevel level = invLevelES.find(req.getBranchId(), productId).orElse(null);
            long onHand = level != null ? Optional.ofNullable(level.getOnHand()).orElse(0L) : 0L;
            long reserved = level != null ? Optional.ofNullable(level.getReserved()).orElse(0L) : 0L;
            map.put(productId, new InventoryView(onHand, reserved, onHand - reserved));
        }
        return ResponseBuilder.success("Fetch batch inventory levels successfully", new InventoryLevelsBatchResponse(map));
    }
    
    @GetMapping("/inv/export-inventory-report")
    @SwaggerOperation(
        summary = "Export inventory report to Excel",
        description = "Export inventory report with on-hand, reserved, and available quantities")
    public ResponseEntity<byte[]> exportInventoryReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate,
        @RequestParam UUID branchId) {
        
        try {
            // Get branch info
            Branch branch = branchES.findById(branchId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Branch not found"));
            
            // Get all inventory lots for the branch (or all branches if branchId is null)
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
            
            // Create styles
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setWrapText(true);
            
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.cloneStyleFrom(dataStyle);
            DataFormat format = workbook.createDataFormat();
            numberStyle.setDataFormat(format.getFormat("#,##0"));
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);
            
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.cloneStyleFrom(dataStyle);
            currencyStyle.setDataFormat(format.getFormat("#,##0"));
            currencyStyle.setAlignment(HorizontalAlignment.RIGHT);
            
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
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 15));
            
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
            
            // Create header row
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
            
            // Empty cells
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
            
            Row note2Row = sheet.createRow(rowNum++);
            note2Row.createCell(0).setCellValue("- Báo cáo hiển thị chi tiết từng lô hàng tồn kho");
            
            Row note3Row = sheet.createRow(rowNum++);
            note3Row.createCell(0).setCellValue("- SL lô: Số lượng còn lại của lô hàng cụ thể");
            
            Row note4Row = sheet.createRow(rowNum++);
            note4Row.createCell(0).setCellValue("- Giá trị lô: Giá trị của lô = Số lượng lô × Đơn giá");
            
            Row note5Row = sheet.createRow(rowNum++);
            note5Row.createCell(0).setCellValue("- Tổng tồn: Tổng số lượng tất cả các lô của sản phẩm");
            
            Row note6Row = sheet.createRow(rowNum++);
            note6Row.createCell(0).setCellValue("- Đang giữ: Số lượng đã được đặt trước (reserved) cho các đơn hàng");
            
            Row note7Row = sheet.createRow(rowNum++);
            note7Row.createCell(0).setCellValue("- Có thể bán: Số lượng có thể bán = Tổng tồn - Đang giữ");
            
            Row note8Row = sheet.createRow(rowNum++);
            note8Row.createCell(0).setCellValue("- Hệ thống sử dụng phương pháp FIFO (First-In-First-Out) để xuất hàng");
            
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
            String filename = String.format("BaoCaoTonKho_ChiTiet_%s_%s.xlsx",
                reportDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            httpHeaders.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(outputStream.toByteArray());
            
        } catch (Exception e) {
            log.error("Error exporting inventory report", e);
            throw new RuntimeException("Error exporting inventory report: " + e.getMessage());
        }
    }
    
    // Inner class to hold inventory report line data (per lot)
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
        // Summary info per product (not per lot)
        long totalOnHand = 0L;
        long totalReserved = 0L;
        long totalAvailable = 0L;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddStockRequest {
        
        @JsonProperty("branch_id")
        private UUID branchId;
        
        @JsonProperty("product_id")
        private UUID productId;
        
        private Long qty;
        
        @JsonProperty("unit_cost")
        private BigDecimal unitCost;
        
        @JsonProperty("lot_code")
        private String lotCode;
        
        @JsonProperty("ref_id")
        private UUID refId;
        
        @JsonProperty("ref_type")
        private StockRefType refType;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoveRequest {
        
        @JsonProperty("branch_id")
        private UUID branchId;
        
        @JsonProperty("product_id")
        private UUID productId;
        
        private Long qty;
        
        @JsonProperty("ref_id")
        private UUID refId;
        
        @JsonProperty("ref_type")
        private StockRefType refType;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryLevelsBatchRequest {
        
        @JsonProperty("branch_id")
        private UUID branchId;
        
        @JsonProperty("product_ids")
        private List<UUID> productIds;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryLevelsBatchResponse {
        
        private Map<UUID, InventoryView> items = new LinkedHashMap<>();
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryView {
        
        @JsonProperty("on_hand")
        private long onHand;
        
        @JsonProperty("reserved")
        private long reserved;
        
        @JsonProperty("available")
        private long available;
    }
}
