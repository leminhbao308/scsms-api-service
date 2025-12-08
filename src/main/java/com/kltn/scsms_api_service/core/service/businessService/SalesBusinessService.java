package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.entity.SalesOrder;
import com.kltn.scsms_api_service.core.entity.SalesOrderLine;
import com.kltn.scsms_api_service.core.entity.SalesReturn;
import com.kltn.scsms_api_service.core.entity.SalesReturnLine;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesBusinessService {
    private final SalesOrderEntityService salesOrderEntityService;
    private final SalesOrderLineEntityService solES;
    private final SalesReturnEntityService srES;
    private final SalesReturnLineEntityService srlES;
    private final InventoryBusinessService inventoryBS;
    private final PricingBusinessService pricingBS;
    private final ProductService productES;
    private final UserService userES;

    @Transactional
    public SalesOrder createDraft(SalesOrder so) {
        so.setStatus(SalesStatus.DRAFT);
        return salesOrderEntityService.create(so);
    }

    /**
     * Confirm sales order và reserve stock
     * 
     * NGHIỆP VỤ QUAN TRỌNG:
     * - Nếu sales order được tạo từ booking (có service items với originalBookingId):
     *   → KHÔNG reserve stock từ sales order vì booking đã reserve stock khi đặt chỗ (PENDING)
     *   → Sales order từ booking CHỈ thanh toán tiền (total price), KHÔNG động chạm đến tồn kho
     *   → Chỉ update status và resolve price cho product items (nếu có)
     * - Nếu sales order là order thông thường:
     *   → Reserve stock bình thường cho product items
     */
    @Transactional
    public SalesOrder confirm(UUID soId) {
        SalesOrder so = salesOrderEntityService.requireWithDetails(soId);
        List<SalesOrderLine> allLines = solES.byOrder(so.getId());
        
        // QUAN TRỌNG: Kiểm tra xem sales order này có được tạo từ booking không
        // Nếu CÓ BẤT KỲ line nào với originalBookingId → KHÔNG reserve
        // Vì booking đã reserve stock rồi
        boolean isFromBooking = allLines.stream()
            .anyMatch(line -> line.getOriginalBookingId() != null);
        
        if (isFromBooking) {
            // NGHIỆP VỤ: Sales order từ booking CHỈ thanh toán tiền, KHÔNG động chạm đến tồn kho
            // - Booking đã reserve stock khi đặt chỗ (PENDING status)
            // - Booking sẽ fulfill stock khi bắt đầu thực hiện dịch vụ (IN_PROGRESS status)
            // - Sales order chỉ thanh toán total price, KHÔNG BAO GIỜ reserve/fulfill inventory
            log.warn("CRITICAL: Sales order {} is created from booking(s) (has originalBookingId). " +
                "NGHIỆP VỤ: Sales order từ booking CHỈ thanh toán tiền, KHÔNG động chạm đến tồn kho. " +
                "Stock was already reserved by booking when booking was created (PENDING). " +
                "Stock will be fulfilled by booking when service starts (IN_PROGRESS). " +
                "SKIPPING ALL inventory reservation - only resolving prices and updating status.",
                soId);
        }
        
        // Price resolution + reservation (chỉ cho sales order thông thường)
        for (SalesOrderLine line : allLines) {
            // Only process product items (skip service items)
            if (line.isProductItem() && line.getProduct() != null) {
                // Resolve price nếu chưa có
                if (line.getUnitPrice() == null) {
                    line.setUnitPrice(pricingBS.resolveUnitPrice(line.getProduct().getProductId()));
                    solES.update(line);
                }
                
                // Chỉ reserve stock nếu KHÔNG phải sales order từ booking
                if (!isFromBooking) {
                    inventoryBS.reserveStock(
                            so.getBranch().getBranchId(),
                            line.getProduct().getProductId(),
                            line.getQuantity(),
                            so.getId(),
                            StockRefType.SALE_ORDER);
                } else {
                    log.warn("CRITICAL: Skipping stock reservation for product {} in sales order {} - " +
                        "order is from booking (has originalBookingId), stock already reserved by booking. " +
                        "This prevents duplicate inventory reservation.",
                        line.getProduct().getProductId(), soId);
                }
            } else if (line.isServiceItem()) {
                // For service items, we don't need to reserve stock or resolve pricing
                // Service items are already priced and stock was already reserved by booking
                log.info("Skipping inventory reservation for service item - ServiceId: {}, OriginalBookingId: {}. " +
                    "Stock was already reserved by booking.",
                    line.getServiceId(), line.getOriginalBookingId());
            }
        }
        so.setStatus(SalesStatus.CONFIRMED);
        return salesOrderEntityService.update(so);
    }

    /**
     * Fulfill stock cho sales order
     * 
     * NGHIỆP VỤ QUAN TRỌNG:
     * - Nếu sales order được tạo từ booking (có service items với originalBookingId):
     *   → KHÔNG fulfill stock từ sales order vì booking đã fulfill stock khi bắt đầu thực hiện dịch vụ (IN_PROGRESS)
     *   → Sales order từ booking CHỈ thanh toán tiền (total price), KHÔNG động chạm đến tồn kho
     *   → Chỉ update status của sales order, không động chạm đến tồn kho
     * - Nếu sales order là order thông thường (không có service items):
     *   → Fulfill stock bình thường cho product items
     */
    @Transactional
    public SalesOrder fulfill(UUID soId) {
        SalesOrder so = salesOrderEntityService.requireWithDetails(soId);
        List<SalesOrderLine> allLines = solES.byOrder(so.getId());
        
        // QUAN TRỌNG: Kiểm tra xem sales order này có được tạo từ booking không
        // Nếu CÓ BẤT KỲ line nào với originalBookingId (service item hoặc product item) → KHÔNG fulfill
        // Vì booking đã xử lý tồn kho rồi
        boolean isFromBooking = allLines.stream()
            .anyMatch(line -> line.getOriginalBookingId() != null);
        
        if (isFromBooking) {
            // NGHIỆP VỤ: Sales order từ booking CHỈ thanh toán tiền, KHÔNG động chạm đến tồn kho
            // - Booking đã reserve stock khi đặt chỗ (PENDING status)
            // - Booking đã fulfill stock khi bắt đầu thực hiện dịch vụ (IN_PROGRESS status)
            // - Sales order chỉ thanh toán total price, KHÔNG BAO GIỜ fulfill inventory
            // - Chỉ update status, không fulfill stock
            log.warn("CRITICAL: Sales order {} is created from booking(s) (has originalBookingId). " +
                "NGHIỆP VỤ: Sales order từ booking CHỈ thanh toán tiền, KHÔNG động chạm đến tồn kho. " +
                "Stock was already handled by booking (reserved at PENDING, fulfilled at IN_PROGRESS). " +
                "SKIPPING ALL inventory fulfillment - only updating sales order status to FULFILLED. " +
                "This prevents duplicate inventory deduction.",
                soId);
            
            // Chỉ update status, KHÔNG BAO GIỜ fulfill stock
            so.setStatus(SalesStatus.FULFILLED);
            return salesOrderEntityService.update(so);
        }
        
        // Sales order thông thường (KHÔNG từ booking): fulfill stock cho product items
        for (SalesOrderLine line : allLines) {
            // Only process product items (skip service items)
            if (line.isProductItem() && line.getProduct() != null) {
                UUID productId = line.getProduct().getProductId();
                UUID branchId = so.getBranch().getBranchId();
                Long qty = line.getQuantity();
                
                log.info("Fulfilling {} units of product {} for sales order {} (regular order, not from booking)", 
                    qty, productId, soId);
                inventoryBS.fulfillStockFIFO(
                        branchId,
                        productId,
                        qty,
                        so.getId(),
                        StockRefType.SALE_ORDER);
            } else if (line.isServiceItem()) {
                // For service items, we don't need to fulfill stock
                // Service items don't have physical inventory
                log.info("Skipping stock fulfillment for service item - ServiceId: {}, OriginalBookingId: {}",
                    line.getServiceId(), line.getOriginalBookingId());
            }
        }
        so.setStatus(SalesStatus.FULFILLED);
        return salesOrderEntityService.update(so);
    }

    @Transactional
    public SalesReturn createReturn(UUID soId, String reason, Map<UUID, Long> productQtyMap,
            Map<UUID, BigDecimal> unitCostOptional) {
        SalesOrder so = salesOrderEntityService.requireWithDetails(soId);

        // Calculate discount ratio for the entire order
        // This ratio represents what percentage of original price the customer actually
        // paid
        BigDecimal discountRatio = BigDecimal.ONE;
        if (so.getOriginalAmount() != null && so.getFinalAmount() != null
                && so.getOriginalAmount().compareTo(BigDecimal.ZERO) > 0) {
            // discountRatio = final_amount / original_amount
            // Example: 560,000 / 700,000 = 0.8 (customer paid 80% of original price)
            discountRatio = so.getFinalAmount().divide(so.getOriginalAmount(), 4, RoundingMode.HALF_UP);
        }

        // Calculate return amount based on ACTUAL PRICE PAID (after discount)
        BigDecimal returnAmount = BigDecimal.ZERO;
        for (Map.Entry<UUID, Long> entry : productQtyMap.entrySet()) {
            UUID productId = entry.getKey();
            Long returnQty = entry.getValue();

            // Find the original line to get unit price (only for product items)
            SalesOrderLine originalLine = so.getLines().stream()
                    .filter(line -> line.isProductItem() && line.getProduct() != null
                            && line.getProduct().getProductId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST,
                            "Product not found in original order"));

            // Calculate return amount using DISCOUNTED price
            // Formula: unit_price × quantity × discount_ratio
            // This ensures customer gets refund based on what they actually paid
            BigDecimal lineReturnAmount = originalLine.getUnitPrice()
                    .multiply(BigDecimal.valueOf(returnQty))
                    .multiply(discountRatio)
                    .setScale(0, RoundingMode.HALF_UP); // Round to nearest VND
            returnAmount = returnAmount.add(lineReturnAmount);
        }

        // Update sales order status and final_amount
        so.setStatus(SalesStatus.RETURNED);

        // If final_amount exists, reduce it by return amount
        if (so.getFinalAmount() != null) {
            BigDecimal newFinalAmount = so.getFinalAmount().subtract(returnAmount);
            so.setFinalAmount(newFinalAmount);
        }

        // If original_amount exists, reduce it by return amount
        if (so.getOriginalAmount() != null) {
            BigDecimal newOriginalAmount = so.getOriginalAmount().subtract(returnAmount);
            so.setOriginalAmount(newOriginalAmount);
        }

        salesOrderEntityService.update(so);

        // 4.5 Reverse customer statistics when return (decrease total_orders,
        // total_spent, and potentially accumulated_points)
        User customer = so.getCustomer();
        if (customer != null) {
            try {
                // Calculate points to deduct (based on return amount)
                // Formula: returnAmount / 1,000 (rounded down)
                int pointsToDeduct = returnAmount.divide(BigDecimal.valueOf(1000), 0, RoundingMode.DOWN).intValue();

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
                Double returnAmountDouble = returnAmount.doubleValue();
                // Ensure total spent doesn't go negative
                customer.setTotalSpent(Math.max(0.0, currentTotalSpent - returnAmountDouble));

                // Save ALL changes
                userES.saveUser(customer);

                log.info(
                        "Reversed customer statistics for return - Points deducted: {}, Orders: {} → {}, Spent: {} → {}",
                        pointsToDeduct,
                        currentOrderCount, Math.max(0, currentOrderCount - 1),
                        currentTotalSpent, Math.max(0.0, currentTotalSpent - returnAmountDouble));

            } catch (Exception e) {
                log.error("Failed to reverse customer statistics for return: {}", e.getMessage());
            }
        }

        SalesReturn sr = srES
                .create(SalesReturn.builder().reason(reason).salesOrder(so).branch(so.getBranch()).build());
        productQtyMap.forEach((productId, qty) -> {
            srlES.create(SalesReturnLine.builder()
                    .salesReturn(sr)
                    .product(productES.getRefByProductId(productId))
                    .quantity(qty)
                    .build());
            BigDecimal unitCost = unitCostOptional != null ? unitCostOptional.get(productId) : null;
            inventoryBS.returnToStock(
                    so.getBranch().getBranchId(),
                    productId,
                    qty,
                    unitCost,
                    sr.getId(),
                    StockRefType.SALE_RETURN);
        });
        return sr;
    }
    
    public List<SalesOrder> getSalesOrdersByDateAndBranch(
        LocalDateTime fromDate,
        LocalDateTime toDate,
        UUID branchId) {
        
        log.info("Fetching sales orders for report - From: {}, To: {}, Branch: {}",
            fromDate, toDate, branchId);
        
        // Query fulfilled orders within date range
        List<SalesOrder> orders = salesOrderEntityService.findSalesOrdersForReport(
            fromDate,
            toDate,
            branchId,
            SalesStatus.FULFILLED);
        
        log.info("Found {} fulfilled sales orders for report", orders.size());
        
        return orders;
    }
}
