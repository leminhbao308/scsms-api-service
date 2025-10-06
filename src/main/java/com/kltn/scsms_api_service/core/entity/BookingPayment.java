package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity quản lý thanh toán cho booking
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "booking_payments", schema = GeneralConstant.DB_SCHEMA_DEV)
public class BookingPayment extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;
    
    /**
     * Booking được thanh toán
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    /**
     * Số tiền thanh toán
     */
    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    /**
     * Phương thức thanh toán
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    /**
     * Trạng thái thanh toán
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    /**
     * ID giao dịch từ hệ thống thanh toán
     */
    @Column(name = "transaction_id", length = 255)
    private String transactionId;
    
    /**
     * Mã tham chiếu giao dịch
     */
    @Column(name = "reference_code", length = 100)
    private String referenceCode;
    
    /**
     * Thời gian thanh toán
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    /**
     * Thời gian hết hạn (nếu có)
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    /**
     * Thông tin thẻ (mã hóa)
     */
    @Column(name = "card_info", length = 500)
    private String cardInfo;
    
    /**
     * Ghi chú thanh toán
     */
    @Column(name = "notes", length = 500)
    private String notes;
    
    /**
     * Phí giao dịch
     */
    @Column(name = "transaction_fee", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal transactionFee = BigDecimal.ZERO;
    
    /**
     * Tỷ giá (nếu thanh toán bằng ngoại tệ)
     */
    @Column(name = "exchange_rate", precision = 10, scale = 6)
    private BigDecimal exchangeRate;
    
    /**
     * Tiền tệ gốc
     */
    @Column(name = "original_currency", length = 3)
    private String originalCurrency;
    
    /**
     * Số tiền gốc (trước quy đổi)
     */
    @Column(name = "original_amount", precision = 15, scale = 2)
    private BigDecimal originalAmount;
    
    /**
     * Thông tin hoàn tiền
     */
    @Column(name = "refund_amount", precision = 15, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(name = "refund_reason", length = 500)
    private String refundReason;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    @Column(name = "refunded_by", length = 255)
    private String refundedBy;
    
    /**
     * Thông tin người thanh toán
     */
    @Column(name = "payer_name", length = 255)
    private String payerName;
    
    @Column(name = "payer_phone", length = 20)
    private String payerPhone;
    
    @Column(name = "payer_email", length = 255)
    private String payerEmail;
    
    // Business methods
    
    /**
     * Kiểm tra thanh toán có thành công không
     */
    public boolean isSuccessful() {
        return paymentStatus == PaymentStatus.SUCCESS;
    }
    
    /**
     * Kiểm tra thanh toán có thất bại không
     */
    public boolean isFailed() {
        return paymentStatus == PaymentStatus.FAILED || paymentStatus == PaymentStatus.CANCELLED;
    }
    
    /**
     * Kiểm tra thanh toán có đang chờ không
     */
    public boolean isPending() {
        return paymentStatus == PaymentStatus.PENDING;
    }
    
    /**
     * Kiểm tra thanh toán có bị hoàn tiền không
     */
    public boolean isRefunded() {
        return paymentStatus == PaymentStatus.REFUNDED;
    }
    
    /**
     * Xác nhận thanh toán thành công
     */
    public void confirmPayment(String transactionId) {
        this.paymentStatus = PaymentStatus.SUCCESS;
        this.transactionId = transactionId;
        this.paidAt = LocalDateTime.now();
    }
    
    /**
     * Đánh dấu thanh toán thất bại
     */
    public void markAsFailed(String reason) {
        this.paymentStatus = PaymentStatus.FAILED;
        this.notes = (this.notes != null ? this.notes + "\n" : "") + "Failed: " + reason;
    }
    
    /**
     * Hủy thanh toán
     */
    public void cancelPayment(String reason) {
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.notes = (this.notes != null ? this.notes + "\n" : "") + "Cancelled: " + reason;
    }
    
    /**
     * Hoàn tiền
     */
    public void refundPayment(BigDecimal refundAmount, String reason, String refundedBy) {
        this.paymentStatus = PaymentStatus.REFUNDED;
        this.refundAmount = refundAmount;
        this.refundReason = reason;
        this.refundedBy = refundedBy;
        this.refundedAt = LocalDateTime.now();
    }
    
    /**
     * Tính tổng tiền thực tế (bao gồm phí giao dịch)
     */
    public BigDecimal getTotalAmount() {
        return amount.add(transactionFee != null ? transactionFee : BigDecimal.ZERO);
    }
    
    /**
     * Enum cho phương thức thanh toán
     */
    public enum PaymentMethod {
        CASH,           // Tiền mặt
        CARD,           // Thẻ
        BANK_TRANSFER,  // Chuyển khoản
        WALLET,         // Ví điện tử
        VOUCHER,        // Voucher
        POINTS,         // Điểm tích lũy
        CREDIT,         // Tín dụng
        OTHER           // Khác
    }
    
    /**
     * Enum cho trạng thái thanh toán
     */
    public enum PaymentStatus {
        PENDING,        // Chờ xử lý
        PROCESSING,     // Đang xử lý
        SUCCESS,        // Thành công
        FAILED,         // Thất bại
        CANCELLED,      // Đã hủy
        REFUNDED,       // Đã hoàn tiền
        EXPIRED         // Hết hạn
    }
}
