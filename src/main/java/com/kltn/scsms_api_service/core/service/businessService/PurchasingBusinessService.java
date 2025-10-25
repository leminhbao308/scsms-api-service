package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request.ConfirmImportRequestDto;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request.CreatePOLine;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request.CreatePORequest;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request.ExcelImportLineDto;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.response.ExcelImportErrorDto;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.response.ExcelImportPreviewResponseDto;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.response.ExcelImportPreviewRowDto;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.PurchaseOrder;
import com.kltn.scsms_api_service.core.entity.PurchaseOrderLine;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.PurchaseOrderLineMapper;
import com.kltn.scsms_api_service.mapper.PurchaseOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchasingBusinessService {
    private final PurchaseOrderEntityService purchaseOrderEntityService;
    private final PurchaseOrderLineEntityService polES;
    private final InventoryBusinessService inventoryBS;

    private final ProductService productES;
    private final SupplierService supplierES;
    private final BranchService branchES;

    private final PurchaseOrderMapper poMapper;
    private final PurchaseOrderLineMapper polMapper;

    @Transactional
    public PurchaseOrder createDraft(CreatePORequest poReq) {
        PurchaseOrder createdPO = purchaseOrderEntityService.create(
                poMapper.toEntity(poReq, branchES));

        // Create and process lines
        for (CreatePOLine lineReq : poReq.getLines()) {
            if (lineReq.getQty() <= 0)
                continue;

            // Create line with proper associations
            PurchaseOrderLine newPol = polMapper.toEntity(lineReq, productES, supplierES);
            newPol.setPurchaseOrder(createdPO);
            polES.create(newPol);

            // Update inventory & create lot
            inventoryBS.addStock(
                    createdPO.getBranch().getBranchId(),
                    lineReq.getProductId(),
                    lineReq.getQty(),
                    lineReq.getUnitCost(),
                    lineReq.getLotCode(),
                    createdPO.getId(),
                    StockRefType.PURCHASE_ORDER);
        }

        return createdPO;
    }

    public List<PurchaseOrderLine> getProductPOHistory(UUID productId) {
        return polES.getByProductId(productId);
    }

    public List<PurchaseOrder> getPurchaseOrdersByDateAndBranch(LocalDateTime fromDate, LocalDateTime toDate,
            UUID branchId) {
        return purchaseOrderEntityService.getByDateAndBranch(fromDate, toDate, branchId);
    }

    // Excel Import Methods

    /**
     * Generate Excel template for purchase order import
     * 
     * @param productIds Optional list of product IDs to pre-fill
     * @param supplierId Optional supplier ID to filter products
     * @return Excel file as byte array
     */
    public byte[] generateExcelTemplate(List<UUID> productIds, UUID supplierId) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("PurchaseOrder");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Mã sản phẩm (*)", "Tên sản phẩm", "Nhà cung cấp",
                    "Số lượng (*)", "Đơn giá (*)", "Mã lô", "Hạn sử dụng (dd/MM/yyyy)"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Pre-fill products if provided
            if (productIds != null && !productIds.isEmpty()) {
                int rowNum = 1;
                for (UUID productId : productIds) {
                    Product product = productES.findById(productId).orElse(null);
                    if (product != null) {
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(product.getBarcode() != null ? product.getBarcode() : "");
                        row.createCell(1).setCellValue(product.getProductName());
                        row.createCell(2).setCellValue(""); // Supplier name will be filled by user
                        row.createCell(3).setCellValue(""); // Quantity
                        row.createCell(4).setCellValue(""); // Unit cost
                        row.createCell(5).setCellValue(""); // Lot code
                        row.createCell(6).setCellValue(""); // Expiry date
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 2000);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generating Excel template", e);
            throw new RuntimeException("Error generating Excel template: " + e.getMessage());
        }
    }

    /**
     * Preview Excel import file and validate data
     * 
     * @param file     Excel file to preview
     * @param branchId Branch ID for context
     * @return Preview response with validation results
     */
    public ExcelImportPreviewResponseDto previewExcelImport(MultipartFile file, UUID branchId) {
        List<ExcelImportPreviewRowDto> previewData = new ArrayList<>();
        List<ExcelImportErrorDto> errors = new ArrayList<>();
        int totalRows = 0;
        int validRows = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                totalRows++;
                boolean rowValid = true;

                // Extract data
                String productCode = getCellValueAsString(row.getCell(0));
                Long quantity = getCellValueAsLong(row.getCell(3));
                BigDecimal unitCost = getCellValueAsBigDecimal(row.getCell(4));
                String lotCode = getCellValueAsString(row.getCell(5));
                String expiryDateStr = getCellValueAsString(row.getCell(6));

                // Validate required fields
                if (productCode == null || productCode.trim().isEmpty()) {
                    errors.add(ExcelImportErrorDto.builder()
                            .row(i + 1)
                            .field("Mã sản phẩm")
                            .message("Mã sản phẩm không được để trống")
                            .build());
                    rowValid = false;
                }

                if (quantity == null || quantity <= 0) {
                    errors.add(ExcelImportErrorDto.builder()
                            .row(i + 1)
                            .field("Số lượng")
                            .message("Số lượng phải lớn hơn 0")
                            .build());
                    rowValid = false;
                }

                if (unitCost == null || unitCost.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add(ExcelImportErrorDto.builder()
                            .row(i + 1)
                            .field("Đơn giá")
                            .message("Đơn giá phải lớn hơn 0")
                            .build());
                    rowValid = false;
                }

                // Validate product exists
                Product product = null;
                if (productCode != null && !productCode.trim().isEmpty()) {
                    product = productES.findByBarcode(productCode).orElse(null);
                    if (product == null) {
                        errors.add(ExcelImportErrorDto.builder()
                                .row(i + 1)
                                .field("Mã sản phẩm")
                                .message("Không tìm thấy sản phẩm với mã: " + productCode)
                                .build());
                        rowValid = false;
                    }
                }

                if (rowValid) {
                    validRows++;
                }

                // Add to preview data
                ExcelImportPreviewRowDto previewRow = ExcelImportPreviewRowDto.builder()
                        .productCode(productCode)
                        .productName(product != null ? product.getProductName() : "")
                        .supplierName(getCellValueAsString(row.getCell(2)))
                        .quantity(quantity != null ? quantity : 0L)
                        .unitCost(unitCost != null ? unitCost : BigDecimal.ZERO)
                        .lotCode(lotCode)
                        .expiryDate(expiryDateStr != null && !expiryDateStr.isEmpty() ? parseExpiryDate(expiryDateStr)
                                : null)
                        .build();

                previewData.add(previewRow);
            }

        } catch (IOException e) {
            log.error("Error reading Excel file", e);
            throw new RuntimeException("Error reading Excel file: " + e.getMessage());
        }

        return ExcelImportPreviewResponseDto.builder()
                .totalRows(totalRows)
                .validRows(validRows)
                .invalidRows(totalRows - validRows)
                .errors(errors)
                .previewData(previewData)
                .build();
    }

    /**
     * Confirm and execute Excel import
     * 
     * @param request Import request with validated data
     * @return Created purchase order
     */
    @Transactional
    public PurchaseOrder confirmExcelImport(ConfirmImportRequestDto request) {
        // Convert to CreatePORequest
        List<CreatePOLine> lines = new ArrayList<>();

        for (ExcelImportLineDto lineDto : request.getLines()) {
            // Find product by barcode
            Product product = productES.findByBarcode(lineDto.getProductCode())
                    .orElseThrow(() -> new ClientSideException(
                            ErrorCode.NOT_FOUND,
                            "Product not found: " + lineDto.getProductCode()));

            CreatePOLine line = new CreatePOLine();
            line.setProductId(product.getProductId());
            line.setSupplierId(product.getSupplierId()); // Use default supplier from product
            line.setQty(lineDto.getQuantity());
            line.setUnitCost(lineDto.getUnitCost());
            line.setLotCode(lineDto.getLotCode());
            line.setExpiryDate(lineDto.getExpiryDate());

            lines.add(line);
        }

        CreatePORequest poRequest = new CreatePORequest();
        poRequest.setBranchId(request.getBranchId());
        poRequest.setLines(lines);

        // Create purchase order using existing method
        return createDraft(poRequest);
    }

    // Helper methods

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private Long getCellValueAsLong(Cell cell) {
        if (cell == null)
            return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (long) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                return value.isEmpty() ? null : Long.parseLong(value);
            }
        } catch (Exception e) {
            log.warn("Error parsing cell as Long: {}", e.getMessage());
        }
        return null;
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null)
            return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                return value.isEmpty() ? null : new BigDecimal(value);
            }
        } catch (Exception e) {
            log.warn("Error parsing cell as BigDecimal: {}", e.getMessage());
        }
        return null;
    }

    private LocalDateTime parseExpiryDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty())
            return null;

        try {
            // Try parsing dd/MM/yyyy format
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return java.time.LocalDate.parse(dateStr.trim(), formatter).atStartOfDay();
        } catch (Exception e) {
            log.warn("Error parsing expiry date: {}", dateStr);
            return null;
        }
    }
}
