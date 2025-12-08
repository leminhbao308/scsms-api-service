package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Count users created after date
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdDate >= :date")
    Long countCreatedAfter(@Param("date") LocalDateTime date);

    /**
     * Count users created between dates
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdDate >= :start AND u.createdDate < :end")
    Long countCreatedBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
