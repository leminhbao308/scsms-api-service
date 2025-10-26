package com.kltn.scsms_api_service.core.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "otp_codes", schema = GeneralConstant.DB_SCHEMA_DEV)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OtpCode extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "otp_code_id")
    private UUID otpCodeId;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "code", length = 10)
    private String code;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_used")
    private Boolean isUsed = false;

    @Column(name = "attempt_count")
    private Integer attemptCount = 0;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "transaction_id", length = 50)
    private String transactionId;
}
