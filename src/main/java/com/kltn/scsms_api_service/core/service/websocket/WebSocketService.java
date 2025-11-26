package com.kltn.scsms_api_service.core.service.websocket;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.ServiceProcessTrackingInfoDto;
import com.kltn.scsms_api_service.core.dto.websocket.BookingEventDto;
import com.kltn.scsms_api_service.core.dto.websocket.TrackingEventDto;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.entity.ServiceProcessTracking;
import com.kltn.scsms_api_service.core.service.businessService.BookingInfoService;
import com.kltn.scsms_api_service.mapper.ServiceProcessTrackingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final BookingInfoService bookingInfoService;
    private final ServiceProcessTrackingMapper serviceProcessTrackingMapper;

    // Topic constants
    public static final String TOPIC_BOOKINGS = "/topic/bookings";
    public static final String TOPIC_VEHICLE_PROFILES = "/topic/vehicle-profiles";
    public static final String TOPIC_CUSTOMERS = "/topic/customers";
    public static final String TOPIC_TRACKINGS = "/topic/trackings";
    public static final String TOPIC_AUTH = "/topic/auth";

    // Message constants
    public static final String MESSAGE_RELOAD_BOOKING = "RELOAD_BOOKING";
    public static final String MESSAGE_RELOAD_VEHICLE_PROFILE = "RELOAD_VEHICLE_PROFILE";
    public static final String MESSAGE_RELOAD_CUSTOMER = "RELOAD_CUSTOMER";
    public static final String MESSAGE_PASSWORD_CHANGED = "PASSWORD_CHANGED";

    /**
     * Notify tất cả clients về thay đổi booking
     * Gửi signal "RELOAD_BOOKING" đến topic /topic/bookings
     */
    public void notifyBookingReload() {
        try {
            log.info("WebSocket: Sending booking reload notification to {}", TOPIC_BOOKINGS);
            messagingTemplate.convertAndSend(TOPIC_BOOKINGS, MESSAGE_RELOAD_BOOKING);
            log.debug("WebSocket: Booking reload notification sent successfully");
        } catch (Exception ex) {
            log.error("WebSocket: Failed to send booking reload notification: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Notify tất cả clients về thay đổi vehicle profile
     * Gửi signal "RELOAD_VEHICLE_PROFILE" đến topic /topic/vehicle-profiles
     */
    public void notifyVehicleProfileReload() {
        try {
            log.info("WebSocket: Sending vehicle profile reload notification to {}", TOPIC_VEHICLE_PROFILES);
            messagingTemplate.convertAndSend(TOPIC_VEHICLE_PROFILES, MESSAGE_RELOAD_VEHICLE_PROFILE);
            log.debug("WebSocket: Vehicle profile reload notification sent successfully");
        } catch (Exception ex) {
            log.error("WebSocket: Failed to send vehicle profile reload notification: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Notify tất cả clients về thay đổi customer
     * Gửi signal "RELOAD_CUSTOMER" đến topic /topic/customers
     */
    public void notifyCustomerReload() {
        try {
            log.info("WebSocket: Sending customer reload notification to {}", TOPIC_CUSTOMERS);
            messagingTemplate.convertAndSend(TOPIC_CUSTOMERS, MESSAGE_RELOAD_CUSTOMER);
            log.debug("WebSocket: Customer reload notification sent successfully");
        } catch (Exception ex) {
            log.error("WebSocket: Failed to send customer reload notification: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Send message đến một user cụ thể (nếu cần)
     * Sử dụng /queue/notifications/{userId} cho private messages
     */
    public void sendNotificationToUser(String userId, String message) {
        try {
            String destination = "/queue/notifications/" + userId;
            log.info("WebSocket: Sending notification to user {} at {}", userId, destination);
            messagingTemplate.convertAndSend(destination, message);
            log.debug("WebSocket: Notification sent to user {} successfully", userId);
        } catch (Exception ex) {
            log.error("WebSocket: Failed to send notification to user {}: {}", userId, ex.getMessage(), ex);
        }
    }

    /**
     * Notify user that password has been changed
     * Sends notification to all devices of the user (except the current device)
     * This will trigger logout on other devices
     */
    public void notifyPasswordChanged(String userId) {
        try {
            log.info("WebSocket: Sending password changed notification to user {}", userId);
            // Send to user-specific topic so all their devices receive it
            String destination = "/topic/auth/" + userId;
            messagingTemplate.convertAndSend(destination, MESSAGE_PASSWORD_CHANGED);
            log.debug("WebSocket: Password changed notification sent to user {} successfully", userId);
        } catch (Exception ex) {
            log.error("WebSocket: Failed to send password changed notification to user {}: {}", userId, ex.getMessage(), ex);
        }
    }

    // ========== Structured Booking Event Methods ==========

    /**
     * Gửi structured booking event với thông tin chi tiết
     * Frontend có thể update specific booking thay vì reload toàn bộ
     */
    private void sendBookingEvent(BookingEventDto event) {
        try {
            log.info("WebSocket: Sending booking event {} for booking {} to {}", 
                event.getEventType(), event.getBookingId(), TOPIC_BOOKINGS);
            messagingTemplate.convertAndSend(TOPIC_BOOKINGS, event);
            log.debug("WebSocket: Booking event sent successfully");
        } catch (Exception ex) {
            log.error("WebSocket: Failed to send booking event: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Notify booking created
     */
    public void notifyBookingCreated(Booking booking) {
        BookingInfoDto bookingData = bookingInfoService.toBookingInfoDto(booking);
        BookingEventDto event = BookingEventDto.builder()
            .eventType(BookingEventDto.BookingEventType.CREATED)
            .bookingId(booking.getBookingId())
            .bookingCode(booking.getBookingCode())
            .bookingData(bookingData)
            .timestamp(LocalDateTime.now())
            .message(String.format("Booking #%s đã được tạo", booking.getBookingCode()))
            .build();
        sendBookingEvent(event);
    }

    /**
     * Notify booking confirmed (PENDING → CONFIRMED)
     */
    public void notifyBookingConfirmed(Booking booking) {
        BookingInfoDto bookingData = bookingInfoService.toBookingInfoDto(booking);
        BookingEventDto event = BookingEventDto.builder()
            .eventType(BookingEventDto.BookingEventType.CONFIRMED)
            .bookingId(booking.getBookingId())
            .bookingCode(booking.getBookingCode())
            .bookingData(bookingData)
            .timestamp(LocalDateTime.now())
            .message(String.format("Booking #%s đã được xác nhận", booking.getBookingCode()))
            .build();
        sendBookingEvent(event);
    }

    /**
     * Notify booking cancelled
     */
    public void notifyBookingCancelled(Booking booking) {
        BookingInfoDto bookingData = bookingInfoService.toBookingInfoDto(booking);
        BookingEventDto event = BookingEventDto.builder()
            .eventType(BookingEventDto.BookingEventType.CANCELLED)
            .bookingId(booking.getBookingId())
            .bookingCode(booking.getBookingCode())
            .bookingData(bookingData)
            .timestamp(LocalDateTime.now())
            .message(String.format("Booking #%s đã bị hủy", booking.getBookingCode()))
            .build();
        sendBookingEvent(event);
    }

    /**
     * Notify booking checked in (CONFIRMED → CHECKED_IN)
     */
    public void notifyBookingCheckedIn(Booking booking) {
        BookingInfoDto bookingData = bookingInfoService.toBookingInfoDto(booking);
        BookingEventDto event = BookingEventDto.builder()
            .eventType(BookingEventDto.BookingEventType.CHECKED_IN)
            .bookingId(booking.getBookingId())
            .bookingCode(booking.getBookingCode())
            .bookingData(bookingData)
            .timestamp(LocalDateTime.now())
            .message(String.format("Booking #%s đã được check-in", booking.getBookingCode()))
            .build();
        sendBookingEvent(event);
    }

    /**
     * Notify booking started (CHECKED_IN → IN_PROGRESS)
     */
    public void notifyBookingStarted(Booking booking) {
        BookingInfoDto bookingData = bookingInfoService.toBookingInfoDto(booking);
        BookingEventDto event = BookingEventDto.builder()
            .eventType(BookingEventDto.BookingEventType.STARTED)
            .bookingId(booking.getBookingId())
            .bookingCode(booking.getBookingCode())
            .bookingData(bookingData)
            .timestamp(LocalDateTime.now())
            .message(String.format("Dịch vụ cho booking #%s đã bắt đầu", booking.getBookingCode()))
            .build();
        sendBookingEvent(event);
    }

    /**
     * Notify booking completed (IN_PROGRESS → COMPLETED)
     */
    public void notifyBookingCompleted(Booking booking) {
        BookingInfoDto bookingData = bookingInfoService.toBookingInfoDto(booking);
        BookingEventDto event = BookingEventDto.builder()
            .eventType(BookingEventDto.BookingEventType.COMPLETED)
            .bookingId(booking.getBookingId())
            .bookingCode(booking.getBookingCode())
            .bookingData(bookingData)
            .timestamp(LocalDateTime.now())
            .message(String.format("Dịch vụ cho booking #%s đã hoàn thành", booking.getBookingCode()))
            .build();
        sendBookingEvent(event);
    }

    /**
     * Notify booking updated (thông tin, schedule, items, etc.)
     */
    public void notifyBookingUpdated(Booking booking) {
        BookingInfoDto bookingData = bookingInfoService.toBookingInfoDto(booking);
        BookingEventDto event = BookingEventDto.builder()
            .eventType(BookingEventDto.BookingEventType.UPDATED)
            .bookingId(booking.getBookingId())
            .bookingCode(booking.getBookingCode())
            .bookingData(bookingData)
            .timestamp(LocalDateTime.now())
            .message(String.format("Booking #%s đã được cập nhật", booking.getBookingCode()))
            .build();
        sendBookingEvent(event);
    }

    /**
     * Notify booking updated với custom message
     */
    public void notifyBookingUpdated(Booking booking, String customMessage) {
        BookingInfoDto bookingData = bookingInfoService.toBookingInfoDto(booking);
        BookingEventDto event = BookingEventDto.builder()
            .eventType(BookingEventDto.BookingEventType.UPDATED)
            .bookingId(booking.getBookingId())
            .bookingCode(booking.getBookingCode())
            .bookingData(bookingData)
            .timestamp(LocalDateTime.now())
            .message(customMessage)
            .build();
        sendBookingEvent(event);
    }

    /**
     * Notify booking deleted
     * Note: Booking đã bị xóa nên không có bookingData
     */
    public void notifyBookingDeleted(UUID bookingId, String bookingCode) {
        BookingEventDto event = BookingEventDto.builder()
            .eventType(BookingEventDto.BookingEventType.CANCELLED) // Use CANCELLED type for deleted
            .bookingId(bookingId)
            .bookingCode(bookingCode)
            .bookingData(null) // Booking đã bị xóa
            .timestamp(LocalDateTime.now())
            .message(String.format("Booking #%s đã bị xóa", bookingCode))
            .build();
        sendBookingEvent(event);
    }

    // ========== Structured Tracking Event Methods ==========

    /**
     * Gửi structured tracking event với thông tin chi tiết
     * Frontend có thể update specific tracking thay vì reload toàn bộ
     */
    private void sendTrackingEvent(TrackingEventDto event) {
        try {
            log.info("WebSocket: Sending tracking event {} for tracking {} to {}", 
                event.getEventType(), event.getTrackingId(), TOPIC_TRACKINGS);
            messagingTemplate.convertAndSend(TOPIC_TRACKINGS, event);
            log.debug("WebSocket: Tracking event sent successfully");
        } catch (Exception ex) {
            log.error("WebSocket: Failed to send tracking event: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Notify tracking created
     */
    public void notifyTrackingCreated(ServiceProcessTracking tracking) {
        ServiceProcessTrackingInfoDto trackingData = serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(tracking);
        String bookingCode = tracking.getBooking() != null ? tracking.getBooking().getBookingCode() : "N/A";
        TrackingEventDto event = TrackingEventDto.builder()
            .eventType(TrackingEventDto.TrackingEventType.CREATED)
            .trackingId(tracking.getTrackingId())
            .bookingId(tracking.getBooking() != null ? tracking.getBooking().getBookingId() : null)
            .bookingCode(bookingCode)
            .trackingData(trackingData)
            .timestamp(LocalDateTime.now())
            .message(String.format("Tracking mới đã được tạo cho booking #%s", bookingCode))
            .build();
        sendTrackingEvent(event);
    }

    /**
     * Notify tracking started (PENDING → IN_PROGRESS)
     */
    public void notifyTrackingStarted(ServiceProcessTracking tracking) {
        ServiceProcessTrackingInfoDto trackingData = serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(tracking);
        String bookingCode = tracking.getBooking() != null ? tracking.getBooking().getBookingCode() : "N/A";
        String stepName = tracking.getServiceStep() != null ? tracking.getServiceStep().getName() : "N/A";
        TrackingEventDto event = TrackingEventDto.builder()
            .eventType(TrackingEventDto.TrackingEventType.STARTED)
            .trackingId(tracking.getTrackingId())
            .bookingId(tracking.getBooking() != null ? tracking.getBooking().getBookingId() : null)
            .bookingCode(bookingCode)
            .trackingData(trackingData)
            .timestamp(LocalDateTime.now())
            .message(String.format("Bước '%s' đã bắt đầu cho booking #%s", stepName, bookingCode))
            .build();
        sendTrackingEvent(event);
    }

    /**
     * Notify tracking updated (notes, progress, etc.)
     */
    public void notifyTrackingUpdated(ServiceProcessTracking tracking) {
        ServiceProcessTrackingInfoDto trackingData = serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(tracking);
        String bookingCode = tracking.getBooking() != null ? tracking.getBooking().getBookingCode() : "N/A";
        TrackingEventDto event = TrackingEventDto.builder()
            .eventType(TrackingEventDto.TrackingEventType.UPDATED)
            .trackingId(tracking.getTrackingId())
            .bookingId(tracking.getBooking() != null ? tracking.getBooking().getBookingId() : null)
            .bookingCode(bookingCode)
            .trackingData(trackingData)
            .timestamp(LocalDateTime.now())
            .message(String.format("Tracking đã được cập nhật cho booking #%s", bookingCode))
            .build();
        sendTrackingEvent(event);
    }

    /**
     * Notify tracking updated với custom message
     */
    public void notifyTrackingUpdated(ServiceProcessTracking tracking, String customMessage) {
        ServiceProcessTrackingInfoDto trackingData = serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(tracking);
        String bookingCode = tracking.getBooking() != null ? tracking.getBooking().getBookingCode() : "N/A";
        TrackingEventDto event = TrackingEventDto.builder()
            .eventType(TrackingEventDto.TrackingEventType.UPDATED)
            .trackingId(tracking.getTrackingId())
            .bookingId(tracking.getBooking() != null ? tracking.getBooking().getBookingId() : null)
            .bookingCode(bookingCode)
            .trackingData(trackingData)
            .timestamp(LocalDateTime.now())
            .message(customMessage)
            .build();
        sendTrackingEvent(event);
    }

    /**
     * Notify tracking completed (IN_PROGRESS → COMPLETED)
     */
    public void notifyTrackingCompleted(ServiceProcessTracking tracking) {
        ServiceProcessTrackingInfoDto trackingData = serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(tracking);
        String bookingCode = tracking.getBooking() != null ? tracking.getBooking().getBookingCode() : "N/A";
        String stepName = tracking.getServiceStep() != null ? tracking.getServiceStep().getName() : "N/A";
        TrackingEventDto event = TrackingEventDto.builder()
            .eventType(TrackingEventDto.TrackingEventType.COMPLETED)
            .trackingId(tracking.getTrackingId())
            .bookingId(tracking.getBooking() != null ? tracking.getBooking().getBookingId() : null)
            .bookingCode(bookingCode)
            .trackingData(trackingData)
            .timestamp(LocalDateTime.now())
            .message(String.format("Bước '%s' đã hoàn thành cho booking #%s", stepName, bookingCode))
            .build();
        sendTrackingEvent(event);
    }

    /**
     * Notify tracking cancelled
     */
    public void notifyTrackingCancelled(ServiceProcessTracking tracking) {
        ServiceProcessTrackingInfoDto trackingData = serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(tracking);
        String bookingCode = tracking.getBooking() != null ? tracking.getBooking().getBookingCode() : "N/A";
        String stepName = tracking.getServiceStep() != null ? tracking.getServiceStep().getName() : "N/A";
        TrackingEventDto event = TrackingEventDto.builder()
            .eventType(TrackingEventDto.TrackingEventType.CANCELLED)
            .trackingId(tracking.getTrackingId())
            .bookingId(tracking.getBooking() != null ? tracking.getBooking().getBookingId() : null)
            .bookingCode(bookingCode)
            .trackingData(trackingData)
            .timestamp(LocalDateTime.now())
            .message(String.format("Bước '%s' đã bị hủy cho booking #%s", stepName, bookingCode))
            .build();
        sendTrackingEvent(event);
    }
}

