package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    Optional<Payment> findBySalesOrder_Id(UUID salesOrderId);
    
    Optional<Payment> findByOrderCode(Long orderCode);
    
    Optional<Payment> findByTransactionId(String transactionId);
}
