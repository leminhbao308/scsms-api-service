package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.core.dto.paymentManagement.request.*;
import com.kltn.scsms_api_service.core.dto.paymentManagement.response.*;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.service.businessService.PaymentBusinessService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller handling Payment operations
 */
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "Payment management endpoints")
public class PaymentController {
    
    private final PaymentBusinessService paymentBS;
    
    /**
     * Initiate payment for a sales order
     */
    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment", description = "Create payment link for sales order")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
        @RequestBody InitiatePaymentRequest request) {
        
        log.info("Initiating payment for sales order: {}", request.getSalesOrderId());
        
        PaymentResponse response = paymentBS.initiatePayment(request);
        
        return ResponseBuilder.success("Payment initiated successfully", response);
    }
    
    /**
     * PayOS webhook handler
     */
    @PostMapping("/webhook/payos")
    @Operation(summary = "PayOS webhook", description = "Handle PayOS payment webhook")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> handlePayOSWebhook(
        @RequestBody PayOSWebhookData webhookData) {
        
        log.info("Received PayOS webhook for order code: {}", webhookData.getOrderCode());
        
        PaymentStatusResponse response = paymentBS.handlePayOSWebhook(webhookData);
        
        return ResponseBuilder.success("Webhook processed successfully", response);
    }
    
    /**
     * Verify payment after return from PayOS
     */
    @GetMapping("/verify/{orderCode}")
    @Operation(summary = "Verify payment", description = "Verify payment status from PayOS")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> verifyPayment(
        @PathVariable Long orderCode) {
        
        log.info("Verifying payment for order code: {}", orderCode);
        
        PaymentStatusResponse response = paymentBS.verifyPayment(orderCode);
        
        return ResponseBuilder.success("Payment verified successfully", response);
    }
    
    /**
     * Complete direct payment (Cash, Bank Transfer)
     */
    @PostMapping("/complete/{paymentId}")
    @Operation(summary = "Complete payment", description = "Mark payment as completed for direct payments")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> completePayment(
        @PathVariable UUID paymentId,
        @RequestParam(required = false) String transactionId) {
        
        log.info("Completing payment: {}", paymentId);
        
        PaymentStatusResponse response = paymentBS.completeDirectPayment(paymentId, transactionId);
        
        return ResponseBuilder.success("Payment completed successfully", response);
    }
    
    /**
     * Cancel payment
     */
    @PostMapping("/cancel/{paymentId}")
    @Operation(summary = "Cancel payment", description = "Cancel pending payment")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> cancelPayment(
        @PathVariable UUID paymentId) {
        
        log.info("Cancelling payment: {}", paymentId);
        
        PaymentStatusResponse response = paymentBS.cancelPayment(paymentId);
        
        return ResponseBuilder.success("Payment cancelled successfully", response);
    }
    
    /**
     * Get payment by sales order ID
     */
    @GetMapping("/sales-order/{salesOrderId}")
    @Operation(summary = "Get payment by sales order", description = "Retrieve payment information by sales order ID")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentBySalesOrder(
        @PathVariable UUID salesOrderId) {
        
        log.info("Getting payment for sales order: {}", salesOrderId);
        
        PaymentStatusResponse response = paymentBS.getPaymentBySalesOrderId(salesOrderId);
        
        return ResponseBuilder.success("Payment retrieved successfully", response);
    }
}
