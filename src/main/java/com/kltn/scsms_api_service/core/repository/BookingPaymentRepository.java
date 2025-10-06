package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.BookingPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingPaymentRepository extends JpaRepository<BookingPayment, UUID> {
    
    /**
     * Tìm payments theo booking
     */
    List<BookingPayment> findByBooking_BookingIdOrderByCreatedDateDesc(UUID bookingId);
    
    /**
     * Tìm payments theo phương thức thanh toán
     */
    List<BookingPayment> findByPaymentMethodOrderByCreatedDateDesc(BookingPayment.PaymentMethod paymentMethod);
    
    /**
     * Tìm payments theo trạng thái
     */
    List<BookingPayment> findByPaymentStatusOrderByCreatedDateDesc(BookingPayment.PaymentStatus paymentStatus);
    
    /**
     * Tìm payments theo transaction ID
     */
    Optional<BookingPayment> findByTransactionId(String transactionId);
    
    /**
     * Tìm payments theo reference code
     */
    Optional<BookingPayment> findByReferenceCode(String referenceCode);
    
    /**
     * Tìm payments thành công theo booking
     */
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.booking.bookingId = :bookingId " +
           "AND bp.paymentStatus = 'SUCCESS' " +
           "ORDER BY bp.paidAt DESC")
    List<BookingPayment> findSuccessfulPaymentsByBooking(@Param("bookingId") UUID bookingId);
    
    /**
     * Tìm payments đang chờ xử lý
     */
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.paymentStatus IN ('PENDING', 'PROCESSING') " +
           "ORDER BY bp.createdDate ASC")
    List<BookingPayment> findPendingPayments();
    
    /**
     * Tìm payments hết hạn
     */
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.paymentStatus = 'PENDING' " +
           "AND bp.expiresAt < :currentDateTime " +
           "ORDER BY bp.expiresAt ASC")
    List<BookingPayment> findExpiredPayments(@Param("currentDateTime") LocalDateTime currentDateTime);
    
    /**
     * Tìm payments trong khoảng thời gian
     */
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.paidAt BETWEEN :startDateTime AND :endDateTime " +
           "AND bp.paymentStatus = 'SUCCESS' " +
           "ORDER BY bp.paidAt DESC")
    List<BookingPayment> findPaymentsInTimeRange(
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Tìm payments theo chi nhánh trong khoảng thời gian
     */
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.booking.branch.branchId = :branchId " +
           "AND bp.paidAt BETWEEN :startDateTime AND :endDateTime " +
           "AND bp.paymentStatus = 'SUCCESS' " +
           "ORDER BY bp.paidAt DESC")
    List<BookingPayment> findPaymentsByBranchInTimeRange(
        @Param("branchId") UUID branchId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Tính tổng số tiền thanh toán theo booking
     */
    @Query("SELECT SUM(bp.amount) FROM BookingPayment bp WHERE bp.booking.bookingId = :bookingId " +
           "AND bp.paymentStatus = 'SUCCESS'")
    java.math.BigDecimal sumAmountByBooking(@Param("bookingId") UUID bookingId);
    
    /**
     * Tính tổng số tiền thanh toán theo chi nhánh trong khoảng thời gian
     */
    @Query("SELECT SUM(bp.amount) FROM BookingPayment bp WHERE bp.booking.branch.branchId = :branchId " +
           "AND bp.paidAt BETWEEN :startDateTime AND :endDateTime " +
           "AND bp.paymentStatus = 'SUCCESS'")
    java.math.BigDecimal sumAmountByBranchInTimeRange(
        @Param("branchId") UUID branchId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Tính tổng số tiền thanh toán theo phương thức
     */
    @Query("SELECT SUM(bp.amount) FROM BookingPayment bp WHERE bp.paymentMethod = :paymentMethod " +
           "AND bp.paymentStatus = 'SUCCESS'")
    java.math.BigDecimal sumAmountByPaymentMethod(@Param("paymentMethod") BookingPayment.PaymentMethod paymentMethod);
    
    /**
     * Đếm payments theo trạng thái
     */
    long countByPaymentStatus(BookingPayment.PaymentStatus paymentStatus);
    
    /**
     * Đếm payments theo phương thức
     */
    long countByPaymentMethod(BookingPayment.PaymentMethod paymentMethod);
    
    /**
     * Tìm payments bị hoàn tiền
     */
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.paymentStatus = 'REFUNDED' " +
           "ORDER BY bp.refundedAt DESC")
    List<BookingPayment> findRefundedPayments();
    
    /**
     * Tìm payments bị hoàn tiền trong khoảng thời gian
     */
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.paymentStatus = 'REFUNDED' " +
           "AND bp.refundedAt BETWEEN :startDateTime AND :endDateTime " +
           "ORDER BY bp.refundedAt DESC")
    List<BookingPayment> findRefundedPaymentsInTimeRange(
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Tính tổng số tiền hoàn tiền
     */
    @Query("SELECT SUM(bp.refundAmount) FROM BookingPayment bp WHERE bp.paymentStatus = 'REFUNDED'")
    java.math.BigDecimal sumRefundAmount();
    
    /**
     * Tìm payments theo người thanh toán
     */
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.payerName LIKE %:payerName% " +
           "ORDER BY bp.paidAt DESC")
    List<BookingPayment> findByPayerNameContaining(@Param("payerName") String payerName);
    
    /**
     * Tìm payments theo email người thanh toán
     */
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.payerEmail LIKE %:payerEmail% " +
           "ORDER BY bp.paidAt DESC")
    List<BookingPayment> findByPayerEmailContaining(@Param("payerEmail") String payerEmail);
    
    /**
     * Tìm payments theo số điện thoại người thanh toán
     */
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.payerPhone LIKE %:payerPhone% " +
           "ORDER BY bp.paidAt DESC")
    List<BookingPayment> findByPayerPhoneContaining(@Param("payerPhone") String payerPhone);
    
    /**
     * Tìm payments theo thông tin thẻ
     */
    @Query("SELECT bp FROM BookingPayment bp WHERE bp.cardInfo LIKE %:cardInfo% " +
           "ORDER BY bp.paidAt DESC")
    List<BookingPayment> findByCardInfoContaining(@Param("cardInfo") String cardInfo);
    
    /**
     * Xóa payments theo booking
     */
    void deleteByBooking_BookingId(UUID bookingId);
}
