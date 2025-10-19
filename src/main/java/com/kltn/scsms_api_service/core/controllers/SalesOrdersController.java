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
    private final SalesReturnMapper salesReturnMapper;
    private final SalesBusinessService salesBS;
    private final PaymentBusinessService paymentBS;
    private final SalesOrderEntityService soES;
    private final SalesOrderLineEntityService solES;
    private final SalesReturnEntityService srES;

    private final ProductService productES;
    private final BranchService branchES;
    private final UserService userES;
    private final PromotionService promotionES;

    private final SaleOrderMapper soMapper;
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
                createdLines.add(solES.create(SalesOrderLine.builder()
                        .salesOrder(so)
                        .product(productES.getRefByProductId(l.productId))
                        .quantity(l.getQty())
                        .unitPrice(l.getUnitPrice())
                        .isFreeItem(isFree)
                        .build()));
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
            if (!(so.getStatus() == SalesStatus.FULFILLED || so.getStatus() == SalesStatus.PARTIALLY_RETURNED)) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, "Only fulfilled orders can be returned");
            }

            for (SalesOrderLine line : so.getLines()) {
                items.put(line.getProduct().getProductId(), line.getQuantity());

                unitCosts.put(line.getProduct().getProductId(), line.getUnitPrice());
            }
        }

        SaleReturnInfoDto srDto = srMapper.toSaleReturnInfoDto(salesBS.createReturn(soId, items, unitCosts));

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
        for (UUID promotionId : promotionIds) {
            try {
                Promotion promotion = promotionES.getReferenceById(promotionId);

                // Calculate total discount from this promotion
                // For free items, use the original unit_price from the line (before it was set
                // to 0)
                // Since frontend sends free items with unit_price, we can use that
                BigDecimal discountAmount = lines.stream()
                        .filter(line -> Boolean.TRUE.equals(line.getIsFreeItem()))
                        .map(line -> line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Create usage record
                PromotionUsage usage = PromotionUsage.builder()
                        .promotion(promotion)
                        .customer(customer)
                        .orderId(order.getId())
                        .discountAmount(discountAmount)
                        .usedAt(LocalDateTime.now())
                        .build();

                promotionUsageRepo.save(usage);

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
