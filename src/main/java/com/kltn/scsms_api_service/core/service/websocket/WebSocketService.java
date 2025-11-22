package com.kltn.scsms_api_service.core.service.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    // Topic constants
    public static final String TOPIC_BOOKINGS = "/topic/bookings";
    public static final String TOPIC_VEHICLE_PROFILES = "/topic/vehicle-profiles";
    public static final String TOPIC_CUSTOMERS = "/topic/customers";

    // Message constants
    public static final String MESSAGE_RELOAD_BOOKING = "RELOAD_BOOKING";
    public static final String MESSAGE_RELOAD_VEHICLE_PROFILE = "RELOAD_VEHICLE_PROFILE";
    public static final String MESSAGE_RELOAD_CUSTOMER = "RELOAD_CUSTOMER";

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
}

