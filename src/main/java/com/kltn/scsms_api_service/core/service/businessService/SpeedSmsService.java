package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.configs.property.SpeedSmsProperties;
import com.kltn.scsms_api_service.core.dto.OtpVerification.request.SendOtpRequest;
import com.kltn.scsms_api_service.core.dto.OtpVerification.request.VerifyOtpRequest;
import com.kltn.scsms_api_service.core.dto.OtpVerification.response.OtpResponse;
import com.kltn.scsms_api_service.core.dto.speedsms.SpeedSmsResponse;
import com.kltn.scsms_api_service.core.entity.OtpCode;
import com.kltn.scsms_api_service.core.repository.OtpCodeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Service xử lý business logic cho OTP verification với SpeedSMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpeedSmsService {

    private final SpeedSmsProperties speedSmsProperties;
    private final OtpCodeRepository otpCodeRepository;
    private final RestTemplate restTemplate;

    /**
     * Gửi OTP đến số điện thoại
     */
    @Transactional
    public OtpResponse sendOtp(SendOtpRequest request) {
        String phoneNumber = request.getPhoneNumber();
        log.info("Sending OTP to phone number: {}", phoneNumber);

        // 1. Kiểm tra rate limiting (cooldown)
        if (isInCooldown(phoneNumber)) {
            log.warn("Phone number {} is in cooldown period", phoneNumber);
            return OtpResponse.builder()
                    .success(false)
                    .message("Please wait before requesting another OTP")
                    .phoneNumber(phoneNumber)
                    .cooldownSeconds((long) speedSmsProperties.getOtp().getCooldownMinutes() * 60)
                    .build();
        }

        try {
            // 2. Tạo mã OTP
            String otpCode = generateOtpCode();
            String message = String.format("Ma OTP cua ban la: %s. Ma co hieu luc trong %d phut.",
                    otpCode, speedSmsProperties.getOtp().getExpiryMinutes());

            // 3. Gửi SMS qua SpeedSMS API
            SpeedSmsResponse speedSmsResponse = sendSms(phoneNumber, message);

            if (speedSmsResponse != null && "success".equals(speedSmsResponse.getStatus())
                    && "00".equals(speedSmsResponse.getCode())) {

                // 4. Lưu OTP vào database
                OtpCode otpCodeEntity = new OtpCode();
                otpCodeEntity.setPhoneNumber(phoneNumber);
                otpCodeEntity.setCode(otpCode);
                otpCodeEntity.setExpiresAt(LocalDateTime.now()
                        .plusMinutes(speedSmsProperties.getOtp().getExpiryMinutes()));
                otpCodeEntity.setIsUsed(false);
                otpCodeEntity.setAttemptCount(0);
                otpCodeEntity.setIpAddress(getClientIpAddress());
                otpCodeEntity.setTransactionId(speedSmsResponse.getData().getTranId().toString());

                otpCodeRepository.save(otpCodeEntity);

                log.info("OTP sent successfully to {} with transaction ID: {}",
                        phoneNumber, speedSmsResponse.getData().getTranId());

                return OtpResponse.builder()
                        .success(true)
                        .message("OTP sent successfully")
                        .phoneNumber(phoneNumber)
                        .status("pending")
                        .transactionId(speedSmsResponse.getData().getTranId().toString())
                        .build();
            } else {
                log.error("SpeedSMS API error: {}", speedSmsResponse);
                return OtpResponse.builder()
                        .success(false)
                        .message("Failed to send OTP. Please try again.")
                        .phoneNumber(phoneNumber)
                        .build();
            }

        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", phoneNumber, e.getMessage());
            return OtpResponse.builder()
                    .success(false)
                    .message("Failed to send OTP. Please try again later.")
                    .phoneNumber(phoneNumber)
                    .build();
        }
    }

    /**
     * Xác thực OTP code
     */
    @Transactional
    public OtpResponse verifyOtp(VerifyOtpRequest request) {
        String phoneNumber = request.getPhoneNumber();
        String code = request.getOtpCode();
        log.info("Verifying OTP for phone number: {}", phoneNumber);

        // 1. Tìm OTP hợp lệ
        Optional<OtpCode> otpOpt = otpCodeRepository.findValidOtp(phoneNumber, code, LocalDateTime.now());

        if (otpOpt.isEmpty()) {
            // 2. Tìm OTP gần nhất để tăng attempt count
            Optional<OtpCode> latestOtpOpt = otpCodeRepository.findLatestOtpByPhoneNumber(phoneNumber);

            if (latestOtpOpt.isPresent()) {
                OtpCode latestOtp = latestOtpOpt.get();
                latestOtp.setAttemptCount(latestOtp.getAttemptCount() + 1);
                otpCodeRepository.save(latestOtp);

                int remainingAttempts = speedSmsProperties.getOtp().getMaxAttempts() - latestOtp.getAttemptCount();

                if (remainingAttempts <= 0) {
                    log.warn("Maximum attempts exceeded for phone number: {}", phoneNumber);
                    return OtpResponse.builder()
                            .success(false)
                            .message("Maximum attempts exceeded. Please request a new OTP.")
                            .phoneNumber(phoneNumber)
                            .status("denied")
                            .build();
                }

                return OtpResponse.builder()
                        .success(false)
                        .message("Invalid OTP code")
                        .phoneNumber(phoneNumber)
                        .status("pending")
                        .remainingAttempts(remainingAttempts)
                        .build();
            } else {
                log.warn("No OTP found for phone number: {}", phoneNumber);
                return OtpResponse.builder()
                        .success(false)
                        .message("No OTP found. Please request a new OTP.")
                        .phoneNumber(phoneNumber)
                        .build();
            }
        }

        // 3. OTP hợp lệ - đánh dấu đã sử dụng
        OtpCode otpCode = otpOpt.get();
        otpCode.setIsUsed(true);
        otpCodeRepository.save(otpCode);

        log.info("OTP verified successfully for phone number: {}", phoneNumber);

        return OtpResponse.builder()
                .success(true)
                .message("OTP verified successfully")
                .phoneNumber(phoneNumber)
                .status("approved")
                .transactionId(otpCode.getTransactionId())
                .build();
    }

    /**
     * Gửi SMS qua SpeedSMS API
     */
    private SpeedSmsResponse sendSms(String phoneNumber, String message) {
        try {
            String url = speedSmsProperties.getApiUrl();

            // Headers - Sử dụng Basic Auth như SpeedSMS mẫu
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String userCredentials = speedSmsProperties.getAccessToken() + ":x";
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userCredentials.getBytes());
            headers.set("Authorization", basicAuth);

            // Request body - Theo đúng format SpeedSMS mẫu
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("to", new String[] { phoneNumber });
            requestBody.put("content", encodeNonAsciiCharacters(message)); // Encode tiếng Việt
            requestBody.put("type", 4); // Brandname mặc định
            requestBody.put("brandname", "Notify"); // Thử NOTIFY thay vì VERIFY

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Sending SMS to SpeedSMS API: {}", requestBody);
            log.info("Using Access Token: {}", speedSmsProperties.getAccessToken().substring(0, 10) + "...");

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            log.info("SpeedSMS Response Status: {}", response.getStatusCode());
            log.info("SpeedSMS Raw Response: {}", response.getBody());

            // Parse JSON manually để xem chi tiết
            ObjectMapper mapper = new ObjectMapper();
            SpeedSmsResponse speedSmsResponse = mapper.readValue(response.getBody(), SpeedSmsResponse.class);

            return speedSmsResponse;

        } catch (Exception e) {
            log.error("Error calling SpeedSMS API: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Tạo mã OTP ngẫu nhiên
     */
    private String generateOtpCode() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6 digits
        return String.valueOf(otp);
    }

    /**
     * Kiểm tra xem số điện thoại có đang trong cooldown period không
     */
    private boolean isInCooldown(String phoneNumber) {
        LocalDateTime cooldownTime = LocalDateTime.now()
                .minusMinutes(speedSmsProperties.getOtp().getCooldownMinutes());

        long pendingCount = otpCodeRepository.countActiveOtpsByPhoneNumber(phoneNumber, cooldownTime);

        return pendingCount > 0;
    }

    /**
     * Lấy IP address của client
     */
    private String getClientIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Encode non-ASCII characters for SpeedSMS API (copy from SpeedSMS sample)
     */
    private String encodeNonAsciiCharacters(String value) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            int unit = (int) c;
            if (unit > 127) {
                String hex = String.format("%04x", (int) unit);
                String encodedValue = "\\u" + hex;
                sb.append(encodedValue);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Cleanup OTP cũ (có thể gọi từ scheduler)
     */
    @Transactional
    public int cleanupOldOtps() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        int deletedCount = otpCodeRepository.deleteExpiredOtps(cutoffTime);
        log.info("Cleaned up {} old OTP records", deletedCount);
        return deletedCount;
    }
}
