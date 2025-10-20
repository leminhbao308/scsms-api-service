package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.paymentManagement.request.InitiatePaymentRequest;
import com.kltn.scsms_api_service.core.dto.paymentManagement.request.PayOSWebhookData;
import com.kltn.scsms_api_service.core.dto.paymentManagement.response.PaymentResponse;
import com.kltn.scsms_api_service.core.dto.paymentManagement.response.PaymentStatusResponse;
import com.kltn.scsms_api_service.core.entity.Payment;
import com.kltn.scsms_api_service.core.entity.SalesOrder;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PaymentMethod;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PaymentStatus;
import com.kltn.scsms_api_service.core.service.entityService.PaymentEntityService;
import com.kltn.scsms_api_service.core.service.entityService.SalesOrderEntityService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.SaleOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentBusinessService {
    
    private final PaymentEntityService paymentES;
    private final SalesOrderEntityService soES;
    private final PayOS payOS;
    
    private final SaleOrderMapper soMapper;
    
    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;
    
    /**
     * Initiate payment for a sales order
     */
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request) {
        try {
            // 1. Validate and get sales order
            SalesOrder salesOrder = soES.require(request.getSalesOrderId());
            
            // Check if payment already exists
            Payment existingPayment = paymentES.findBySaleOrderId(salesOrder.getId());
            if (existingPayment != null && existingPayment.getStatus() == PaymentStatus.COMPLETED) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, "Payment already completed for this order");
            }
            
            // 2. Calculate total amount
            int totalAmount = calculateOrderAmount(salesOrder);
            
            // 3. Create payment based on method
            PaymentMethod paymentMethod = request.getPaymentMethod();
            
            if (paymentMethod == PaymentMethod.BANK) {
                return createPayOSPayment(salesOrder, totalAmount, request);
            } else {
                return createDirectPayment(salesOrder, totalAmount, paymentMethod);
            }
            
        } catch (Exception e) {
            log.error("Error initiating payment: ", e);
            throw new ClientSideException(ErrorCode.SYSTEM_ERROR, "Failed to initiate payment: " + e.getMessage());
        }
    }
    
    /**
     * Create PayOS payment link
     */
    private PaymentResponse createPayOSPayment(SalesOrder salesOrder, int totalAmount, InitiatePaymentRequest request) {
        try {
            // Generate unique order code
            long orderCode = System.currentTimeMillis();
            
            // Create item list for PayOS
            List<ItemData> items = salesOrder.getLines().stream()
                .map(line -> {
                    String itemName;
                    if (line.isProductItem() && line.getProduct() != null) {
                        // Product item - use product name
                        itemName = line.getProduct().getProductName();
                    } else if (line.isServiceItem()) {
                        // Service item - create descriptive name
                        String serviceIdShort = line.getServiceId() != null ? 
                            line.getServiceId().toString().substring(0, 8) : "Unknown";
                        String bookingCode = line.getOriginalBookingCode() != null ? 
                            line.getOriginalBookingCode() : "N/A";
                        itemName = String.format("Dịch vụ %s (Booking: %s)", serviceIdShort, bookingCode);
                    } else {
                        // Fallback for unknown item types
                        itemName = "Unknown Item";
                    }
                    
                    return ItemData.builder()
                        .name(itemName)
                        .quantity(line.getQuantity().intValue())
                        .price(line.getUnitPrice().intValue())
                        .build();
                })
                .toList();
            
            // Create payment data
            PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(totalAmount)
                .description(fromUuidToBase36(salesOrder.getId()))
                .items(items)
                .returnUrl(request.getReturnUrl() != null ? request.getReturnUrl() : baseUrl + "/payment/return")
                .cancelUrl(request.getCancelUrl() != null ? request.getCancelUrl() : baseUrl + "/payment/cancel")
                .build();
            
            // Create payment link with PayOS
            CheckoutResponseData paymentLink = payOS.createPaymentLink(paymentData);
            
            // Save payment record
            Payment payment = Payment.builder()
                .salesOrder(salesOrder)
                .amount(totalAmount)
                .paymentURL(paymentLink.getCheckoutUrl())
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.BANK)
                .orderCode(orderCode)
                .description("Payment for order #" + salesOrder.getId())
                .returnUrl(request.getReturnUrl())
                .build();
            
            payment = paymentES.save(payment);
            
            // Return response
            return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .salesOrder(soMapper.toSaleOrderInfoDto(salesOrder))
                .amount(totalAmount)
                .paymentURL(paymentLink.getCheckoutUrl())
                .orderCode(orderCode)
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.BANK)
                .qrCode(paymentLink.getQrCode())
                .build();
            
        } catch (Exception e) {
            log.error("Error creating PayOS payment: ", e);
            throw new ClientSideException(ErrorCode.SYSTEM_ERROR, "Failed to create PayOS payment: " + e.getMessage());
        }
    }
    
    /**
     * Create direct payment (Cash, Bank Transfer)
     */
    private PaymentResponse createDirectPayment(SalesOrder salesOrder, int totalAmount, PaymentMethod paymentMethod) {
        Payment payment = Payment.builder()
            .salesOrder(salesOrder)
            .amount(totalAmount)
            .status(PaymentStatus.PENDING)
            .paymentMethod(paymentMethod)
            .description("Direct payment for order #" + salesOrder.getId())
            .build();
        
        payment = paymentES.save(payment);
        
        return PaymentResponse.builder()
            .paymentId(payment.getPaymentId())
            .salesOrder(soMapper.toSaleOrderInfoDto(salesOrder))
            .amount(totalAmount)
            .status(PaymentStatus.PENDING)
            .paymentMethod(paymentMethod)
            .build();
    }
    
    /**
     * Handle PayOS webhook callback
     */
    @Transactional
    public PaymentStatusResponse handlePayOSWebhook(PayOSWebhookData webhookData) {
        try {
            log.info("Processing PayOS webhook for order code: {}", webhookData.getOrderCode());
            
            // Find payment by order code
            Payment payment = paymentES.findByOrderCode(webhookData.getOrderCode());
            
            if (payment == null) {
                throw new ClientSideException(ErrorCode.NOT_FOUND,
                    "Payment not found for order code: " + webhookData.getOrderCode());
            }
            
            // Update payment status based on webhook code
            if ("00".equals(webhookData.getCode())) {
                // Payment successful
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setTransactionId(webhookData.getReference());
                payment.setPaidAt(LocalDateTime.parse(webhookData.getTransactionDateTime(),
                    DateTimeFormatter.ISO_DATE_TIME));
            } else {
                // Payment failed
                payment.setStatus(PaymentStatus.FAILED);
            }
            
            payment = paymentES.save(payment);
            
            log.info("Payment status updated to {} for order code: {}", payment.getStatus(),
                webhookData.getOrderCode());
            
            return buildPaymentStatusResponse(payment, "Payment status updated successfully");
            
        } catch (Exception e) {
            log.error("Error processing PayOS webhook: ", e);
            throw new ClientSideException(ErrorCode.SYSTEM_ERROR, "Failed to process webhook: " + e.getMessage());
        }
    }
    
    /**
     * Verify payment from return URL
     */
    @Transactional
    public PaymentStatusResponse verifyPayment(Long orderCode) {
        try {
            // Get payment info from PayOS
            PaymentLinkData paymentInfo = payOS.getPaymentLinkInformation(orderCode);
            
            // Find payment by order code
            Payment payment = paymentES.findByOrderCode(orderCode);
            
            if (payment == null) {
                throw new ClientSideException(ErrorCode.NOT_FOUND, "Payment not found for order code: " + orderCode);
            }
            
            // Update payment status
            String payosStatus = paymentInfo.getStatus();
            
            if ("PAID".equals(payosStatus)) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
            } else if ("CANCELLED".equals(payosStatus)) {
                payment.setStatus(PaymentStatus.CANCELED);
                payment.setCancelledAt(LocalDateTime.now());
            } else if ("EXPIRED".equals(payosStatus)) {
                payment.setStatus(PaymentStatus.EXPIRED);
            }
            
            payment = paymentES.save(payment);
            
            return buildPaymentStatusResponse(payment, "Payment verified successfully");
            
        } catch (Exception e) {
            log.error("Error verifying payment: ", e);
            throw new ClientSideException(ErrorCode.SYSTEM_ERROR, "Failed to verify payment: " + e.getMessage());
        }
    }
    
    /**
     * Complete direct payment (Cash, Bank Transfer)
     */
    @Transactional
    public PaymentStatusResponse completeDirectPayment(UUID paymentId, String transactionId) {
        Payment payment = paymentES.require(paymentId);
        
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Payment already completed");
        }
        
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(transactionId);
        payment.setPaidAt(LocalDateTime.now());
        
        payment = paymentES.save(payment);
        
        return buildPaymentStatusResponse(payment, "Payment completed successfully");
    }
    
    /**
     * Cancel payment
     */
    @Transactional
    public PaymentStatusResponse cancelPayment(UUID paymentId) {
        Payment payment = paymentES.require(paymentId);
        
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Cannot cancel paid payment");
        }
        
        // If PayOS payment, cancel on PayOS side
        if (payment.getPaymentMethod() == PaymentMethod.BANK && payment.getOrderCode() != null) {
            try {
                payOS.cancelPaymentLink(payment.getOrderCode(), "User requested cancellation");
            } catch (Exception e) {
                log.error("Error cancelling PayOS payment: ", e);
            }
        }
        
        payment.setStatus(PaymentStatus.CANCELED);
        payment.setCancelledAt(LocalDateTime.now());
        
        payment = paymentES.save(payment);
        
        return buildPaymentStatusResponse(payment, "Payment cancelled successfully");
    }
    
    /**
     * Get payment by sales order ID
     */
    public PaymentStatusResponse getPaymentBySalesOrderId(UUID salesOrderId) {
        Payment payment = paymentES.findBySaleOrderId(salesOrderId);
        
        if (payment == null) {
            throw new ClientSideException(ErrorCode.NOT_FOUND, "Payment not found for sales order: " + salesOrderId);
        }
        
        return buildPaymentStatusResponse(payment, "Payment retrieved successfully");
    }
    
    // Helper methods
    
    private int calculateOrderAmount(SalesOrder salesOrder) {
        // Only count non-free items (exclude free items from promotions)
        return salesOrder.getLines().stream()
            .filter(line -> !Boolean.TRUE.equals(line.getIsFreeItem())) // Exclude free items
            .mapToInt(line -> line.getUnitPrice().multiply(
                java.math.BigDecimal.valueOf(line.getQuantity())).intValue())
            .sum();
    }
    
    private PaymentStatusResponse buildPaymentStatusResponse(Payment payment, String message) {
        return PaymentStatusResponse.builder()
            .paymentId(payment.getPaymentId().toString())
            .salesOrderId(payment.getSalesOrder().getId().toString())
            .status(payment.getStatus())
            .amount(payment.getAmount())
            .transactionId(payment.getTransactionId())
            .paidAt(payment.getPaidAt())
            .message(message)
            .build();
    }
    
    public String getPaymentLinkBySalesOrderId(UUID salesOrderId) {
        Payment payment = paymentES.findBySaleOrderId(salesOrderId);
        
        if (payment == null) {
            throw new ClientSideException(ErrorCode.NOT_FOUND, "Payment not found for sales order: " + salesOrderId);
        }
        
        if (payment.getPaymentMethod() != PaymentMethod.BANK || payment.getPaymentURL() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "No payment link available for this payment method");
        }
        
        return payment.getPaymentURL();
    }
    
    private String fromUuidToBase36(UUID uuid) {
        int len = 25;
        
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        BigInteger bi = new BigInteger(1, bb.array()); // luôn dương
        String s = bi.toString(36); // 0-9a-z
        // đảm bảo đúng 25 ký tự
        if (s.length() < len) {
            StringBuilder sb = new StringBuilder(len);
            for (int i = s.length(); i < len; i++)
                sb.append('0');
            sb.append(s);
            s = sb.toString();
        }
        return s.toUpperCase();
    }
}
