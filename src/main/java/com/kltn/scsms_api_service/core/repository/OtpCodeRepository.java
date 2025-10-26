package com.kltn.scsms_api_service.core.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.scsms_api_service.core.entity.OtpCode;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    // Tìm OTP hợp lệ (chưa hết hạn, chưa sử dụng)
    @Query("SELECT o FROM OtpCode o WHERE o.phoneNumber = :phoneNumber " +
            "AND o.code = :code AND o.expiresAt > :now AND o.isUsed = false")
    Optional<OtpCode> findValidOtp(@Param("phoneNumber") String phoneNumber,
            @Param("code") String code,
            @Param("now") LocalDateTime now);

    // Đếm số OTP chưa hết hạn cho một số điện thoại
    @Query("SELECT COUNT(o) FROM OtpCode o WHERE o.phoneNumber = :phoneNumber " +
            "AND o.expiresAt > :now AND o.isUsed = false")
    long countActiveOtpsByPhoneNumber(@Param("phoneNumber") String phoneNumber,
            @Param("now") LocalDateTime now);

    // Xóa OTP hết hạn
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now")
    int deleteExpiredOtps(@Param("now") LocalDateTime now);

    // Tìm OTP gần nhất cho một số điện thoại
    @Query("SELECT o FROM OtpCode o WHERE o.phoneNumber = :phoneNumber " +
            "ORDER BY o.createdDate DESC")
    Optional<OtpCode> findLatestOtpByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}