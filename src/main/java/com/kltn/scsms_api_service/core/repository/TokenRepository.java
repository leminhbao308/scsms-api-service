package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Token;
import com.kltn.scsms_api_service.core.entity.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, String> {
    @Query("""
        SELECT t FROM Token t INNER JOIN User u ON t.user.userId = u.userId
        WHERE u.userId = :userId AND (t.expired = false OR t.revoked = false)
        """)
    List<Token> findAllValidTokensByUser(@Param("userId") UUID userId);
    
    Optional<Token> findByToken(String token);
    
    Optional<Token> findByTokenAndTypeAndRevokedFalseAndExpiredFalse(String token, TokenType type);
    
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
    
    @Query("SELECT t FROM Token t WHERE t.user.userId = :userId AND t.type = :type AND t.revoked = false AND t.expired = false")
    List<Token> findValidTokensByUserAndType(@Param("userId") UUID userId, @Param("type") TokenType type);
    
    // Batch update to revoke tokens
    @Modifying
    @Query(
        "UPDATE Token t SET t.revoked = true, t.expired = true WHERE t.user.userId = :userId AND t.revoked = false")
    int revokeAllUserTokens(@Param("userId") UUID userId);
}
