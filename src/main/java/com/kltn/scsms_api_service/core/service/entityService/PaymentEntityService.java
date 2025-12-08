package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.Payment;
import com.kltn.scsms_api_service.core.repository.PaymentRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentEntityService {
    
    private final PaymentRepository paymentRepository;
    
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }
    
    public Payment require(UUID id) {
        return paymentRepository.findById(id)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Payment not found with id: " + id));
    }
    
    public Payment findBySaleOrderId(UUID saleOrderId) {
        return paymentRepository.findBySalesOrder_Id(saleOrderId)
            .orElse(null);
    }
    
    public Payment findByOrderCode(Long orderCode) {
        return paymentRepository.findByOrderCode(orderCode)
            .orElse(null);
    }
    
    public Payment update(UUID id, Payment payment) {
        return paymentRepository.findById(id)
            .map(existingPayment -> {
                existingPayment.setAmount(payment.getAmount());
                existingPayment.setPaymentMethod(payment.getPaymentMethod());
                existingPayment.setStatus(payment.getStatus());
                existingPayment.setTransactionId(payment.getTransactionId());
                existingPayment.setPaymentURL(payment.getPaymentURL());
                existingPayment.setPaidAt(payment.getPaidAt());
                existingPayment.setCancelledAt(payment.getCancelledAt());
                return paymentRepository.save(existingPayment);
            })
            .orElse(null);
    }
}
