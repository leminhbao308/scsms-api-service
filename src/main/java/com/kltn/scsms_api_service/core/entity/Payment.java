package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PaymentMethod;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payments", schema = GeneralConstant.DB_SCHEMA_DEV)
public class Payment extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;
    
    @OneToOne
    @JoinColumn(name = "sales_order_id", unique = true, foreignKey = @ForeignKey(name = "fk_payment_sales_order"))
    private SalesOrder salesOrder;
    
    @Column(name = "amount", nullable = false)
    private Integer amount;
    
    @Column(name = "payment_url", length = Integer.MAX_VALUE)
    private String paymentURL;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private PaymentStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50, nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(name = "transaction_id", length = Integer.MAX_VALUE)
    private String transactionId;
    
    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;
    
    // PayOS specific fields
    @Column(name = "order_code")
    private Long orderCode;
    
    @Column(name = "checksum_value", length = 500)
    private String checksumValue;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "callback_url", length = Integer.MAX_VALUE)
    private String callbackUrl;
    
    @Column(name = "return_url", length = Integer.MAX_VALUE)
    private String returnUrl;
}
