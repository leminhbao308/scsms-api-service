package com.kltn.scsms_api_service.core.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.paymentManagement.request.InitiatePaymentRequest;
import com.kltn.scsms_api_service.core.dto.paymentManagement.response.PaymentResponse;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.saleOrderManagement.SaleOrderInfoDto;
import com.kltn.scsms_api_service.core.dto.saleOrderManagement.SaleReturnInfoDto;
import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PaymentMethod;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import com.kltn.scsms_api_service.core.repository.PromotionUsageRepository;
import com.kltn.scsms_api_service.core.service.businessService.BookingManagementService;
import com.kltn.scsms_api_service.core.service.businessService.PaymentBusinessService;
import com.kltn.scsms_api_service.core.service.businessService.SalesBusinessService;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.SaleOrderMapper;
import com.kltn.scsms_api_service.mapper.SalesReturnMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.*;

/**
 * Controller handling Sales Order operations
 * Manages creation, confirmation, fulfillment, and returns of sales orders
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sales Order Management", description = "Sale order management endpoints")
public class SalesOrdersController {
    private final SalesBusinessService salesBS;
    private final PaymentBusinessService paymentBS;
    private final BookingManagementService bookingBS;

    private final SalesOrderEntityService soES;
    private final SalesOrderLineEntityService solES;
    private final SalesReturnEntityService srES;

    private final ProductService productES;
    private final BranchService branchES;
    private final UserService userES;
    private final PromotionService promotionES;

    private final SaleOrderMapper soMapper;
    private final SalesReturnMapper salesReturnMapper;
    private final SalesReturnMapper srMapper;

    private final PromotionUsageRepository promotionUsageRepo;

    @PostMapping("/so/create-draft")
    @Operation(summary = "Create draft order", description = "Create a new draft sales order")
    public ResponseEntity<ApiResponse<SaleOrderInfoDto>> createDraft(@RequestBody CreateSORequest req) {
        User customer = null;
        if (req.getCustomerId() != null)
            customer = userES.getUserRefById(req.getCustomerId());

        SalesOrder so = SalesOrder.builder()
                .branch(branchES.getRefById(req.getBranchId()))
                .customer(customer)
                .status(SalesStatus.DRAFT)
                .shippingFullName(req.getShippingFullName())
                .shippingPhone(req.getShippingPhone())
                .shippingAddress(req.getShippingAddress())
                .shippingWard(req.getShippingWard())
                .shippingDistrict(req.getShippingDistrict())
                .shippingCity(req.getShippingCity())
                .shippingNotes(req.getShippingNotes())
                .build();
        so = salesBS.createDraft(so);

        List<SalesOrderLine> createdLLines = new ArrayList<>();
        for (CreateSOLine l : req.getLines()) {
            createdLLines.add(solES.create(SalesOrderLine.builder()
                    .salesOrder(so)
                    .product(productES.getRefByProductId(l.productId))
                    .quantity(l.getQty())
                    .unitPrice(l.getUnitPrice())
                    .build()));
        }
        so.setLines(createdLLines);

        SaleOrderInfoDto soDto = soMapper.toSaleOrderInfoDto(so);

        return ResponseBuilder.success("Sales order draft created", soDto);
    }

    /**
     * Create draft order and initiate payment (Combined endpoint for POS)
     */
    @PostMapping("/so/create-and-pay")
    @Operation(summary = "Create order and initiate payment", description = "Create sales order, confirm, fulfill, and initiate payment in one call")
    public ResponseEntity<ApiResponse<CreateAndPayResponse>> createAndPay(@RequestBody CreateAndPayRequest req) {
        try {
            // 1. Create draft order
            User customer = null;
            if (req.getCustomerId() != null) {
                customer = userES.getUserRefById(req.getCustomerId());
            }

            SalesOrder so = SalesOrder.builder()
                    .branch(branchES.getRefById(req.getBranchId()))
                    .customer(customer)
                    .status(SalesStatus.DRAFT)
                    .originalAmount(req.getOriginalAmount())
                    .totalDiscountAmount(req.getTotalDiscountAmount())
                    .finalAmount(req.getFinalAmount())
                    .discountPercentage(req.getDiscountPercentage())
                    .promotionSnapshot(req.getPromotionSnapshot())
                    .shippingFullName(req.getShippingFullName())
                    .shippingPhone(req.getShippingPhone())
                    .shippingAddress(req.getShippingAddress())
                    .shippingWard(req.getShippingWard())
                    .shippingDistrict(req.getShippingDistrict())
                    .shippingCity(req.getShippingCity())
                    .shippingNotes(req.getShippingNotes())
                    .build();
            so = salesBS.createDraft(so);

            // 2. Add order lines
            List<SalesOrderLine> createdLines = new ArrayList<>();
            for (CreateSOLine l : req.getLines()) {
                Boolean isFree = (l.getIsFreeItem() != null && l.getIsFreeItem());

                // Log line item details
                log.info(
                        "Processing line item - ProductId: {}, ServiceId: {}, IsServiceItem: {}, OriginalBookingId: {}, OriginalBookingCode: {}",
                        l.getProductId(), l.getServiceId(), l.isServiceItem(), l.getOriginalBookingId(),
                        l.getOriginalBookingCode());

                // Build SalesOrderLine with service item support
                SalesOrderLine.SalesOrderLineBuilder lineBuilder = SalesOrderLine.builder()
                        .salesOrder(so)
                        .quantity(l.getQty())
                        .unitPrice(l.getUnitPrice())
                        .isFreeItem(isFree);

                // Set product reference only for product items (not service items)
                if (l.isProductItem() && l.getProductId() != null) {
                    lineBuilder.product(productES.getRefByProductId(l.productId));
                    log.info("Added product reference - ProductId: {}", l.getProductId());
                } else {
                    // For service items, product will be null (no foreign key constraint)
                    lineBuilder.product(null);
                    log.info("Service item - Product set to NULL (no product reference needed)");
                }

                // Add service item fields if present
                if (l.getServiceId() != null) {
                    lineBuilder.serviceId(l.getServiceId());
                    log.info("Added service item - ServiceId: {}", l.getServiceId());
                }
                if (l.getOriginalBookingId() != null) {
                    lineBuilder.originalBookingId(l.getOriginalBookingId());
                    log.info("Added booking context - BookingId: {}", l.getOriginalBookingId());
                }
                if (l.getOriginalBookingCode() != null) {
                    lineBuilder.originalBookingCode(l.getOriginalBookingCode());
                    log.info("Added booking code - BookingCode: {}", l.getOriginalBookingCode());
                }

                SalesOrderLine createdLine = solES.create(lineBuilder.build());
                createdLines.add(createdLine);

                // Log created line details
                log.info("Created SalesOrderLine - Id: {}, IsServiceItem: {}, ServiceId: {}, OriginalBookingId: {}",
                        createdLine.getId(), createdLine.isServiceItem(), createdLine.getServiceId(),
                        createdLine.getOriginalBookingId());
            }
            so.getLines().clear();
            so.getLines().addAll(createdLines);

            // 3. Create PromotionUsage records for applied promotions
            if (req.getPromotionIds() != null && !req.getPromotionIds().isEmpty()) {
                createPromotionUsageRecords(req.getPromotionIds(), so, customer, createdLines);
            }

            // 4. Confirm order
            so = salesBS.confirm(so.getId());

            // 4.5 Update customer statistics (loyalty points, total orders, total spent)
            if (customer != null) {
                try {
                    // Update loyalty points
                    if (req.getEarnedPoints() != null && req.getEarnedPoints() > 0) {
                        Integer currentPoints = customer.getAccumulatedPoints() != null
                                ? customer.getAccumulatedPoints()
                                : 0;
                        Integer newPoints = currentPoints + req.getEarnedPoints();
                        customer.setAccumulatedPoints(newPoints);

                        log.info("Updated loyalty points for customer {} - Previous: {}, Earned: {}, New Total: {}",
                                customer.getUserId(), currentPoints, req.getEarnedPoints(), newPoints);
                    }

                    // Update total orders count
                    int currentOrderCount = customer.getTotalOrders() != null
                            ? customer.getTotalOrders()
                            : 0;
                    customer.setTotalOrders(currentOrderCount + 1);

                    // Update total spent amount
                    Double currentTotalSpent = customer.getTotalSpent() != null
                            ? customer.getTotalSpent()
                            : 0.0;
                    Double orderAmount = req.getFinalAmount() != null
                            ? req.getFinalAmount().doubleValue()
                            : 0.0;
                    customer.setTotalSpent(currentTotalSpent + orderAmount);

                    // Save all changes
                    userES.saveUser(customer);

                    log.info("Updated customer statistics - UserId: {}, Orders: {} → {}, Spent: {} → {}",
                            customer.getUserId(), currentOrderCount, currentOrderCount + 1,
                            currentTotalSpent, currentTotalSpent + orderAmount);

                } catch (Exception e) {
                    log.error("Failed to update customer statistics for customer {}: {}",
                            customer.getUserId(), e.getMessage());
                    // Don't fail the entire transaction if update fails
                }
            } else if (req.getEarnedPoints() != null && req.getEarnedPoints() > 0) {
                log.warn("Loyalty points ({}) not credited - customer is null or guest", req.getEarnedPoints());
            }

            // 5. Initiate payment
            PaymentResponse paymentResponse = null;
            if (req.getPaymentMethod() != null && !req.getPaymentMethod().equals(PaymentMethod.CASH)) {
                InitiatePaymentRequest paymentRequest = InitiatePaymentRequest.builder()
                        .salesOrderId(so.getId())
                        .paymentMethod(req.getPaymentMethod())
                        .returnUrl(req.getReturnUrl())
                        .cancelUrl(req.getCancelUrl())
                        .build();

                paymentResponse = paymentBS.initiatePayment(paymentRequest);
            } else {
                // For cash payment, create pending payment record
                InitiatePaymentRequest paymentRequest = InitiatePaymentRequest.builder()
                        .salesOrderId(so.getId())
                        .paymentMethod(PaymentMethod.CASH)
                        .build();

                paymentResponse = paymentBS.initiatePayment(paymentRequest);

                // Immediately complete cash payment
                if (paymentResponse != null && paymentResponse.getPaymentId() != null) {
                    paymentBS.completeDirectPayment(
                            paymentResponse.getPaymentId(),
                            "CASH-" + System.currentTimeMillis());
                }
            }

            // 5. Build response
            SaleOrderInfoDto soDto = soMapper.toSaleOrderInfoDto(so);

            CreateAndPayResponse response = CreateAndPayResponse.builder()
                    .order(soDto)
                    .payment(paymentResponse)
                    .build();

            return ResponseBuilder.success("Order created and payment initiated successfully", response);

        } catch (Exception e) {
            log.error("Error in create and pay: ", e);
            throw e;
        }
    }

    @PostMapping("/so/confirm/{soId}")
    @Operation(summary = "Confirm order", description = "Confirm a draft sales order")
    public ResponseEntity<ApiResponse<SaleOrderInfoDto>> confirm(@PathVariable UUID soId) {
        SalesOrder so = salesBS.confirm(soId);

        SaleOrderInfoDto soDto = soMapper.toSaleOrderInfoDto(so);

        return ResponseBuilder.success("Sales order confirmed", soDto);
    }

    @PostMapping("/so/fulfill/{soId}")
    @Operation(summary = "Fulfill order", description = "Fulfill a confirmed sales order")
    public ResponseEntity<ApiResponse<SaleOrderInfoDto>> fulfill(@PathVariable UUID soId) {
        SalesOrder so = salesBS.fulfill(soId);

        SaleOrderInfoDto soDto = soMapper.toSaleOrderInfoDto(so);

        return ResponseBuilder.success("Sales order fulfilled", soDto);
    }

    /**
     * Cancel a sales order
     * - Updates order status to CANCELED
     * - Saves cancellation reason
     * - If order contains booking service items, reverts booking payment status to
     * PENDING
     */
    @PostMapping("/so/cancel/{soId}")
    @Operation(summary = "Cancel order", description = "Cancel a sales order and revert booking payment status if applicable")
    public ResponseEntity<ApiResponse<SaleOrderInfoDto>> cancelOrder(
            @PathVariable UUID soId,
            @RequestBody CancelOrderRequest req) {

        // Validate cancellation reason
        if (req.getCancellationReason() == null || req.getCancellationReason().trim().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Cancellation reason is required");
        }

        if (req.getCancellationReason().length() > 500) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Cancellation reason must not exceed 500 characters");
        }

        // Get order
        SalesOrder so = soES.requireWithDetails(soId);

        // Validate order can be canceled
        if (so.getStatus() == SalesStatus.CANCELLED) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Order is already canceled");
        }

        if (so.getStatus() == SalesStatus.RETURNED) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Returned orders cannot be canceled");
        }

        // Update order status and reason
        so.setStatus(SalesStatus.CANCELLED);
        so.setCancellationReason(req.getCancellationReason().trim());
        so = soES.update(so);

        // Reverse customer statistics when cancel (decrease total_orders, total_spent,
        // and potentially accumulated_points)
        User customer = so.getCustomer();
        if (customer != null && so.getFinalAmount() != null) {
            try {
                BigDecimal cancelAmount = so.getFinalAmount();

                // Calculate points to deduct (based on cancel amount)
                // Formula: cancelAmount / 1,000 (rounded down)
                int pointsToDeduct = cancelAmount.divide(BigDecimal.valueOf(1000), 0, RoundingMode.DOWN).intValue();

                if (pointsToDeduct > 0) {
                    int currentPoints = customer.getAccumulatedPoints() != null
                            ? customer.getAccumulatedPoints()
                            : 0;
                    // Ensure points don't go negative
                    int newPoints = Math.max(0, currentPoints - pointsToDeduct);
                    customer.setAccumulatedPoints(newPoints);
                }

                // Decrease total orders count
                int currentOrderCount = customer.getTotalOrders() != null
                        ? customer.getTotalOrders()
                        : 0;
                // Ensure count doesn't go negative
                customer.setTotalOrders(Math.max(0, currentOrderCount - 1));

                // Decrease total spent amount
                Double currentTotalSpent = customer.getTotalSpent() != null
                        ? customer.getTotalSpent()
                        : 0.0;
                Double cancelAmountDouble = cancelAmount.doubleValue();
                // Ensure total spent doesn't go negative
                customer.setTotalSpent(Math.max(0.0, currentTotalSpent - cancelAmountDouble));

                // Save ALL changes
                userES.saveUser(customer);

                log.info(
                        "Reversed customer statistics for cancel - Points deducted: {}, Orders: {} → {}, Spent: {} → {}",
                        pointsToDeduct,
                        currentOrderCount, Math.max(0, currentOrderCount - 1),
                        currentTotalSpent, Math.max(0.0, currentTotalSpent - cancelAmountDouble));

            } catch (Exception e) {
                log.error("Failed to reverse customer statistics for cancel: {}", e.getMessage());
            }
        }

        // Revert booking payment status if order contains booking service items
        Set<UUID> bookingIds = new HashSet<>();
        for (SalesOrderLine line : so.getLines()) {
            if (line.getOriginalBookingId() != null) {
                bookingIds.add(line.getOriginalBookingId());
            }
        }

        if (!bookingIds.isEmpty()) {
            log.info("Reverting payment status for {} bookings from canceled order {}",
                    bookingIds.size(), soId);
            bookingBS.revertPaymentStatusToPending(bookingIds);
        }

        SaleOrderInfoDto soDto = soMapper.toSaleOrderInfoDto(so);

        return ResponseBuilder.success("Sales order canceled successfully", soDto);
    }

    @PostMapping("/so/return/{soId}")
    @Operation(summary = "Create return", description = "Create a return for a sales order")
    public ResponseEntity<ApiResponse<SaleReturnInfoDto>> createReturn(@PathVariable UUID soId,
            @RequestBody(required = false) CreateReturnRequest req) {
        Map<UUID, Long> items = new LinkedHashMap<>();
        Map<UUID, BigDecimal> unitCosts = new LinkedHashMap<>();
        if (req != null)
            for (ReturnItem i : req.getItems()) {
                items.put(i.getProductId(), i.getQty());
                if (i.getUnitCost() != null)
                    unitCosts.put(i.getProductId(), i.getUnitCost());
            }
        else {
            SalesOrder so = soES.requireWithDetails(soId);
            if (!(so.getStatus() == SalesStatus.FULFILLED)) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, "Only fulfilled orders can be returned");
            }

            for (SalesOrderLine line : so.getLines()) {
                items.put(line.getProduct().getProductId(), line.getQuantity());

                unitCosts.put(line.getProduct().getProductId(), line.getUnitPrice());
            }
        }
        String reason = (req != null) ? req.getReason() : "";

        SaleReturnInfoDto srDto = srMapper.toSaleReturnInfoDto(salesBS.createReturn(soId, reason, items, unitCosts));

        return ResponseBuilder.success("Sales return created", srDto);
    }

    @GetMapping("/so/{soId}")
    @Operation(summary = "Get order", description = "Get sales order by ID")
    public ResponseEntity<ApiResponse<SaleOrderInfoDto>> get(@PathVariable UUID soId) {
        SalesOrder so = soES.requireWithDetails(soId);

        SaleOrderInfoDto soDto = soMapper.toSaleOrderInfoDto(so);

        return ResponseBuilder.success("Sales order retrieved", soDto);
    }

    @GetMapping("/so/get-all")
    @Operation(summary = "Get all orders", description = "Get all sales orders")
    public ResponseEntity<ApiResponse<List<SaleOrderInfoDto>>> getAll() {
        // Use optimized query with JOIN FETCH to prevent N+1
        List<SalesOrder> sos = soES.getAllWithDetails();

        // Use batch processing to fetch all bookings at once
        List<SaleOrderInfoDto> sosDto = soMapper.toSaleOrderInfoDtoList(sos);

        return ResponseBuilder.success("All sales orders retrieved", sosDto);
    }

    @GetMapping("/so/paged")
    @Operation(summary = "Get paged orders", description = "Get sales orders with pagination, optionally filtered by userId")
    public ResponseEntity<ApiResponse<PagedSaleOrderResponse>> getPagedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String userId) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Use optimized three-step query: IDs → Orders → Attribute Values
        // Pass userId to filter by customer (null = all orders for admin)
        Page<SalesOrder> pagedOrders = soES.getPagedOrdersWithDetails(pageable, userId);

        // Map to DTOs (all lazy collections already initialized)
        List<SaleOrderInfoDto> ordersDto = soMapper.toSaleOrderInfoDtoList(pagedOrders.getContent());

        PagedSaleOrderResponse response = PagedSaleOrderResponse.builder()
                .content(ordersDto)
                .page(pagedOrders.getNumber())
                .size(pagedOrders.getSize())
                .totalElements(pagedOrders.getTotalElements())
                .totalPages(pagedOrders.getTotalPages())
                .last(pagedOrders.isLast())
                .build();

        return ResponseBuilder.success("Paged sales orders retrieved", response);
    }

    @GetMapping("/so/get-all-return")
    @Operation(summary = "Get all returned orders", description = "Get all sales orders that have returned")
    public ResponseEntity<ApiResponse<List<SaleReturnInfoDto>>> getAllReturns() {
        List<SalesReturn> sos = srES.getAllReturns();

        List<SaleReturnInfoDto> sosDto = sos.stream().map(salesReturnMapper::toSaleReturnInfoDto).toList();

        return ResponseBuilder.success("All returned orders retrieved", sosDto);
    }

    @GetMapping("/so/get-all-fullfilled")
    @Operation(summary = "Get all fullfilled orders", description = "Get all sales orders that have been fullfilled")
    public ResponseEntity<ApiResponse<List<SaleOrderInfoDto>>> getAllFullfills() {
        // Use optimized query with JOIN FETCH to prevent N+1
        List<SalesOrder> sos = soES.getAllFulfilledWithDetails();

        // Use batch processing to fetch all bookings at once
        List<SaleOrderInfoDto> sosDto = soMapper.toSaleOrderInfoDtoList(sos);

        return ResponseBuilder.success("All fullfilled orders retrieved", sosDto);
    }

    // ===== Helper Methods =====

    /**
     * Create PromotionUsage records for applied promotions
     */
    private void createPromotionUsageRecords(List<UUID> promotionIds, SalesOrder order,
            User customer, List<SalesOrderLine> lines) {

        if (promotionIds == null || promotionIds.isEmpty()) {
            log.debug("No promotions to record for order {}", order.getId());
            return;
        }

        // Calculate discount per promotion (equal split if multiple promotions)
        BigDecimal totalDiscount = order.getTotalDiscountAmount() != null
                ? order.getTotalDiscountAmount()
                : BigDecimal.ZERO;

        BigDecimal discountPerPromotion = promotionIds.size() > 1
                ? totalDiscount.divide(BigDecimal.valueOf(promotionIds.size()), 4, java.math.RoundingMode.HALF_UP)
                : totalDiscount;

        // Parse promotion snapshot to get individual promotion details
        java.util.Map<UUID, String> promotionSnapshotMap = new java.util.HashMap<>();
        if (order.getPromotionSnapshot() != null && !order.getPromotionSnapshot().isBlank()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode promotionsArray = objectMapper
                        .readTree(order.getPromotionSnapshot());

                if (promotionsArray.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode promoNode : promotionsArray) {
                        if (promoNode.has("promotion_id")) {
                            UUID promoId = UUID.fromString(promoNode.get("promotion_id").asText());
                            // Store individual promotion snapshot as JSON string
                            promotionSnapshotMap.put(promoId, objectMapper.writeValueAsString(promoNode));
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse promotion snapshot for order {}: {}", order.getId(), e.getMessage());
            }
        }

        for (UUID promotionId : promotionIds) {
            try {
                Promotion promotion = promotionES.getReferenceById(promotionId);

                // Get individual promotion snapshot or null
                String individualSnapshot = promotionSnapshotMap.get(promotionId);

                PromotionUsage usage = PromotionUsage.builder()
                        .promotion(promotion)
                        .customer(customer)
                        .orderId(order.getId())
                        .discountAmount(discountPerPromotion)
                        .usedAt(LocalDateTime.now())
                        .promotionSnapshot(individualSnapshot) // Store individual promotion snapshot
                        .orderOriginalAmount(order.getOriginalAmount())
                        .orderFinalAmount(order.getFinalAmount())
                        .branch(order.getBranch())
                        .build();

                promotionUsageRepo.save(usage);

                log.info("Created promotion usage record - Promotion: {}, Order: {}, Discount: {}",
                        promotionId, order.getId(), discountPerPromotion);

            } catch (Exception e) {
                log.warn("Failed to create promotion usage for promotion {}: {}",
                        promotionId, e.getMessage());
            }
        }
    }
    
    @GetMapping("/so/export-sales-report")
    @SwaggerOperation(summary = "Export sales report to Excel", description = "Export sales revenue report with date range and branch filters, grouped by sales staff")
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
            CellStyle numberStyle = createNumberStyle(workbook);
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
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            // Prepare response
            String filename = String.format("DoanhSoBanHang_%s_%s_%s.xlsx",
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
            log.error("Error exporting sales report", e);
            throw new RuntimeException("Error exporting sales report: " + e.getMessage());
        }
    }
    
    // Add this method to SalesOrdersController.java
    
    @GetMapping("/so/export-returns-report")
    @SwaggerOperation(summary = "Export sales returns report to Excel",
        description = "Export detailed sales returns report with date range filter for entire system")
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
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
            
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
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            // Prepare response
            String filename = String.format("BaoCaoTraHang_%s_%s_%s.xlsx",
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
            log.error("Error exporting sales returns report", e);
            throw new RuntimeException("Error exporting sales returns report: " + e.getMessage());
        }
    }
    
    // Helper class for return summary
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
    
    // Helper methods for creating styles
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
    
    // Inner class to hold staff sales summary
    private static class StaffSalesSummary {
        String staffName = "";
        String saleDate = "";
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal revenueBeforeDiscount = BigDecimal.ZERO;
        BigDecimal revenueAfterDiscount = BigDecimal.ZERO;
    }

    // ===== DTOs =====
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSORequest {

        @JsonProperty("branch_id")
        private UUID branchId;

        @JsonProperty("customer_id")
        private UUID customerId; // optional

        private List<CreateSOLine> lines;

        // ===== SHIPPING ADDRESS FIELDS =====

        @JsonProperty("shipping_full_name")
        private String shippingFullName;

        @JsonProperty("shipping_phone")
        private String shippingPhone;

        @JsonProperty("shipping_address")
        private String shippingAddress;

        @JsonProperty("shipping_ward")
        private String shippingWard;

        @JsonProperty("shipping_district")
        private String shippingDistrict;

        @JsonProperty("shipping_city")
        private String shippingCity;

        @JsonProperty("shipping_notes")
        private String shippingNotes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateSOLine {

        @JsonProperty("product_id")
        private UUID productId;

        private Long qty;

        @JsonProperty("unit_price")
        private BigDecimal unitPrice; // optional, auto-resolve if null

        @JsonProperty("is_free_item")
        @Builder.Default
        private Boolean isFreeItem = false; // true if item is free from promotion

        // ===== SERVICE ITEM SUPPORT =====

        /**
         * Service ID if this line item represents a service (null for product items)
         */
        @JsonProperty("service_id")
        private UUID serviceId;

        /**
         * Original booking ID if this service item comes from a booking
         */
        @JsonProperty("original_booking_id")
        private UUID originalBookingId;

        /**
         * Original booking code for display purposes
         */
        @JsonProperty("original_booking_code")
        private String originalBookingCode;

        // ===== HELPER METHODS =====

        /**
         * Check if this line item represents a service
         *
         * @return true if serviceId is not null
         */
        public boolean isServiceItem() {
            return serviceId != null;
        }

        /**
         * Check if this line item represents a product
         *
         * @return true if serviceId is null
         */
        public boolean isProductItem() {
            return serviceId == null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateAndPayRequest {

        @JsonProperty("branch_id")
        private UUID branchId;

        @JsonProperty("customer_id")
        private UUID customerId; // optional

        @JsonProperty("promotion_ids")
        private List<UUID> promotionIds; // optional

        private List<CreateSOLine> lines;

        @JsonProperty("payment_method")
        private PaymentMethod paymentMethod;

        @JsonProperty("return_url")
        private String returnUrl;

        @JsonProperty("cancel_url")
        private String cancelUrl;

        // ===== DISCOUNT TRACKING FIELDS =====

        @JsonProperty("original_amount")
        private BigDecimal originalAmount; // Total before discounts

        @JsonProperty("total_discount_amount")
        private BigDecimal totalDiscountAmount; // Total discount applied

        @JsonProperty("final_amount")
        private BigDecimal finalAmount; // Total after discounts (amount to pay)

        @JsonProperty("discount_percentage")
        private BigDecimal discountPercentage; // Overall discount % (if applicable)

        @JsonProperty("promotion_snapshot")
        private String promotionSnapshot; // JSON array of applied promotions snapshot

        // ===== LOYALTY POINTS FIELD =====

        @JsonProperty("earned_points")
        private Integer earnedPoints; // Points earned from this purchase (10,000 VNĐ = 1 point)

        // ===== SHIPPING ADDRESS FIELDS =====

        @JsonProperty("shipping_full_name")
        private String shippingFullName;

        @JsonProperty("shipping_phone")
        private String shippingPhone;

        @JsonProperty("shipping_address")
        private String shippingAddress;

        @JsonProperty("shipping_ward")
        private String shippingWard;

        @JsonProperty("shipping_district")
        private String shippingDistrict;

        @JsonProperty("shipping_city")
        private String shippingCity;

        @JsonProperty("shipping_notes")
        private String shippingNotes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.Builder
    public static class CreateAndPayResponse {
        private SaleOrderInfoDto order;
        private PaymentResponse payment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReturnRequest {
        private List<ReturnItem> items;
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelOrderRequest {

        @JsonProperty("cancellation_reason")
        private String cancellationReason; // Required, max 500 characters
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagedSaleOrderResponse {
        private List<SaleOrderInfoDto> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean last;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnItem {

        @JsonProperty("product_id")
        private UUID productId;

        private Long qty;

        @JsonProperty("unit_cost")
        private BigDecimal unitCost;
    }
}
