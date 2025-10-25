package com.kltn.scsms_api_service.core.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

            // 4. Initiate payment
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
        SalesOrder so = soES.require(soId);

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
            SalesOrder so = soES.require(soId);
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
        SalesOrder so = soES.require(soId);

        SaleOrderInfoDto soDto = soMapper.toSaleOrderInfoDto(so);

        return ResponseBuilder.success("Sales order retrieved", soDto);
    }

    @GetMapping("/so/get-all")
    @Operation(summary = "Get all orders", description = "Get all sales orders")
    public ResponseEntity<ApiResponse<List<SaleOrderInfoDto>>> getAll() {
        List<SalesOrder> sos = soES.getAll();

        List<SaleOrderInfoDto> sosDto = sos.stream()
                .map(soMapper::toSaleOrderInfoDto).toList();

        return ResponseBuilder.success("All sales orders retrieved", sosDto);
    }

    @GetMapping("/so/paged")
    @Operation(summary = "Get paged orders", description = "Get sales orders with pagination")
    public ResponseEntity<ApiResponse<PagedSaleOrderResponse>> getPagedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<SalesOrder> pagedOrders = soES.getPagedOrders(pageable);

        List<SaleOrderInfoDto> ordersDto = pagedOrders.getContent().stream()
                .map(soMapper::toSaleOrderInfoDto)
                .toList();

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
        List<SalesOrder> sos = soES.getAllFullfills();

        List<SaleOrderInfoDto> sosDto = sos.stream()
                .map(soMapper::toSaleOrderInfoDto).toList();

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
