package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.BookingPayment;
import com.kltn.scsms_api_service.core.repository.BookingPaymentRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingPaymentService {
    
    private final BookingPaymentRepository bookingPaymentRepository;
    
    public BookingPayment getById(UUID paymentId) {
        return bookingPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Booking payment not found with ID: " + paymentId));
    }
    
    public List<BookingPayment> findAll() {
        return bookingPaymentRepository.findAll();
    }
    
    @Transactional
    public BookingPayment save(BookingPayment bookingPayment) {
        return bookingPaymentRepository.save(bookingPayment);
    }
    
    @Transactional
    public BookingPayment update(BookingPayment bookingPayment) {
        return bookingPaymentRepository.save(bookingPayment);
    }
    
    @Transactional
    public void delete(UUID paymentId) {
        BookingPayment bookingPayment = getById(paymentId);
        bookingPayment.setIsDeleted(true);
        bookingPaymentRepository.save(bookingPayment);
    }
    
    public List<BookingPayment> findByBooking(UUID bookingId) {
        return bookingPaymentRepository.findByBooking_BookingIdOrderByCreatedDateDesc(bookingId);
    }
    
    public List<BookingPayment> findByPaymentMethod(BookingPayment.PaymentMethod paymentMethod) {
        return bookingPaymentRepository.findByPaymentMethodOrderByCreatedDateDesc(paymentMethod);
    }
    
    public List<BookingPayment> findByPaymentStatus(BookingPayment.PaymentStatus paymentStatus) {
        return bookingPaymentRepository.findByPaymentStatusOrderByCreatedDateDesc(paymentStatus);
    }
    
    public Optional<BookingPayment> findByTransactionId(String transactionId) {
        return bookingPaymentRepository.findByTransactionId(transactionId);
    }
    
    public Optional<BookingPayment> findByReferenceCode(String referenceCode) {
        return bookingPaymentRepository.findByReferenceCode(referenceCode);
    }
    
    public List<BookingPayment> findSuccessfulPaymentsByBooking(UUID bookingId) {
        return bookingPaymentRepository.findSuccessfulPaymentsByBooking(bookingId);
    }
    
    public List<BookingPayment> findPendingPayments() {
        return bookingPaymentRepository.findPendingPayments();
    }
    
    public List<BookingPayment> findExpiredPayments(LocalDateTime currentDateTime) {
        return bookingPaymentRepository.findExpiredPayments(currentDateTime);
    }
    
    public List<BookingPayment> findPaymentsInTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return bookingPaymentRepository.findPaymentsInTimeRange(startDateTime, endDateTime);
    }
    
    public List<BookingPayment> findPaymentsByBranchInTimeRange(UUID branchId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return bookingPaymentRepository.findPaymentsByBranchInTimeRange(branchId, startDateTime, endDateTime);
    }
    
    public BigDecimal sumAmountByBooking(UUID bookingId) {
        return bookingPaymentRepository.sumAmountByBooking(bookingId);
    }
    
    public BigDecimal sumAmountByBranchInTimeRange(UUID branchId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return bookingPaymentRepository.sumAmountByBranchInTimeRange(branchId, startDateTime, endDateTime);
    }
    
    public BigDecimal sumAmountByPaymentMethod(BookingPayment.PaymentMethod paymentMethod) {
        return bookingPaymentRepository.sumAmountByPaymentMethod(paymentMethod);
    }
    
    public long countByPaymentStatus(BookingPayment.PaymentStatus paymentStatus) {
        return bookingPaymentRepository.countByPaymentStatus(paymentStatus);
    }
    
    public long countByPaymentMethod(BookingPayment.PaymentMethod paymentMethod) {
        return bookingPaymentRepository.countByPaymentMethod(paymentMethod);
    }
    
    public List<BookingPayment> findRefundedPayments() {
        return bookingPaymentRepository.findRefundedPayments();
    }
    
    public List<BookingPayment> findRefundedPaymentsInTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return bookingPaymentRepository.findRefundedPaymentsInTimeRange(startDateTime, endDateTime);
    }
    
    public BigDecimal sumRefundAmount() {
        return bookingPaymentRepository.sumRefundAmount();
    }
    
    public List<BookingPayment> findByPayerNameContaining(String payerName) {
        return bookingPaymentRepository.findByPayerNameContaining(payerName);
    }
    
    public List<BookingPayment> findByPayerEmailContaining(String payerEmail) {
        return bookingPaymentRepository.findByPayerEmailContaining(payerEmail);
    }
    
    public List<BookingPayment> findByPayerPhoneContaining(String payerPhone) {
        return bookingPaymentRepository.findByPayerPhoneContaining(payerPhone);
    }
    
    public List<BookingPayment> findByCardInfoContaining(String cardInfo) {
        return bookingPaymentRepository.findByCardInfoContaining(cardInfo);
    }
    
    @Transactional
    public void deleteByBooking(UUID bookingId) {
        bookingPaymentRepository.deleteByBooking_BookingId(bookingId);
    }
    
    @Transactional
    public void saveAll(List<BookingPayment> bookingPayments) {
        bookingPaymentRepository.saveAll(bookingPayments);
    }
}
