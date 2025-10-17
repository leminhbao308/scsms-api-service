package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.configs.property.JwtTokenProperties;
import com.kltn.scsms_api_service.core.entity.Token;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.entity.enumAttribute.TokenType;
import com.kltn.scsms_api_service.core.repository.TokenRepository;
import com.kltn.scsms_api_service.core.repository.UserRepository;
import com.kltn.scsms_api_service.core.utils.SensitiveValueMasker;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.exception.ServerSideException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TokenService {
    
    private final JwtTokenProperties jwtTokenProperties;
    private final SensitiveValueMasker sensitiveValueMasker;
    
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    
    private void saveTokens(UUID userId, Map<TokenType, String> tokens) {
        User user = userRepository.getReferenceById(userId);
        
        // Batch insert tokens
        List<Token> tokenEntities = new ArrayList<>();
        tokens.forEach((tokenType, tokenValue) -> {
            long tokenExpirationTime =
                tokenType == TokenType.ACCESS
                    ? jwtTokenProperties.getAccessTokenExpiresIn()
                    : jwtTokenProperties.getRefreshTokenExpiresIn();
            
            Token token =
                Token.builder()
                    .token(tokenValue)
                    .user(user)
                    .type(tokenType)
                    .revoked(false)
                    .expired(false)
                    .expiresAt(LocalDateTime.now().plusSeconds(tokenExpirationTime / 1000))
                    .build();
            
            tokenEntities.add(token);
        });
        
        tokenRepository.saveAll(tokenEntities);
        log.info("JWT - Saved {} tokens for userId: {}", tokenEntities.size(), userId);
    }
    
    public void revokeAllUserTokens(UUID userId) {
        int revokeResult = tokenRepository.revokeAllUserTokens(userId);
        log.info("JWT - Revoked {} tokens for userId: {}", revokeResult, userId);
    }
    
    public boolean isTokenExistedAndNotRevoked(String tokenValue) {
        return tokenRepository
            .findByToken(tokenValue)
            .map(token -> !token.isExpired() && !token.isRevoked())
            .orElse(false);
    }
    
    public void revokeToken(String tokenValue) {
        tokenRepository
            .findByToken(tokenValue)
            .ifPresent(
                token -> {
                    token.setRevoked(true);
                    token.setExpired(true);
                    tokenRepository.save(token);
                    log.info(
                        "Token revoked: {}",
                        sensitiveValueMasker.maskSensitiveStringValues(tokenValue));
                });
    }
    
    public UUID findUserIdByRefreshToken(String refreshToken) {
        User user =
            tokenRepository
                .findByTokenAndTypeAndRevokedFalseAndExpiredFalse(refreshToken, TokenType.REFRESH)
                .map(Token::getUser)
                .orElse(null);
        
        if (user != null) return user.getUserId();
        else throw new ServerSideException(ErrorCode.NOT_FOUND);
    }
    
    public void cleanupExpiredTokens() {
        tokenRepository.deleteAllByExpiredAndRevokedAndExpiresAtBefore(true, true, LocalDateTime.now());
        log.info("JWT - Cleaned up expired tokens");
    }
    
    public Map<TokenType, String> generateAndSaveTokens(User user) {
        Map<String, Object> accessTokenClaims = new HashMap<>();
        accessTokenClaims.put("sub", user.getUserId().toString());
        accessTokenClaims.put("full_name", user.getFullName());
        accessTokenClaims.put("email", user.getEmail());
        accessTokenClaims.put("phone", user.getPhoneNumber());
        accessTokenClaims.put("role", user.getRole().getRoleCode());
        accessTokenClaims.put("type", TokenType.ACCESS.toString());
        accessTokenClaims.put("permissions", permissionService.getUserPermissionCodes(user));
        
        Map<String, Object> refreshTokenClaims = new HashMap<>();
        refreshTokenClaims.put("sub", user.getUserId().toString());
        refreshTokenClaims.put("type", TokenType.REFRESH.toString());
        
        String accessToken = generateToken(accessTokenClaims, jwtTokenProperties.getAccessTokenExpiresIn());
        String refreshToken = generateToken(refreshTokenClaims, jwtTokenProperties.getRefreshTokenExpiresIn());
        
        Map<TokenType, String> tokens = new HashMap<>();
        tokens.put(TokenType.ACCESS, accessToken);
        tokens.put(TokenType.REFRESH, refreshToken);
        
        saveTokens(user.getUserId(), tokens);
        return tokens;
    }
    
    public boolean isValidTokenAndNotExpired(final String token) {
        parseToken(token);
        return !isTokenExpired(token);
    }
    
    public Long getTokenExpiresIn() {
        return jwtTokenProperties.getAccessTokenExpiresIn();
    }
    
    public Long getRefreshTokenExpiresIn() {
        return jwtTokenProperties.getRefreshTokenExpiresIn();
    }
    
    public String getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        
        if (claims.get("type").equals(TokenType.REFRESH.toString()))
            return claims.get("sub", String.class);
        
        return claims.get("userId", String.class);
    }
    
    public Claims getClaimsFromToken(final String token) {
        return parseToken(token).getPayload();
    }
    
    /**
     * Generate token with claims and expiration
     */
    private String generateToken(Map<String, Object> claims, Long expirationTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);
        
        return Jwts.builder()
            .claims(claims)
            .id(UUID.randomUUID().toString())
            .issuedAt(now)
            .expiration(expiration)
            .signWith(getSigningKey())
            .compact();
    }
    
    private PrivateKey getSigningKey() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            byte[] privateKeyBytes = Base64.getDecoder().decode(jwtTokenProperties.getSecretKey());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new ServerSideException(ErrorCode.RESPONSE_ERROR, "Failed to load private key");
        }
    }
    
    private PublicKey getPublicKey() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            byte[] publicKeyBytes = Base64.getDecoder().decode(jwtTokenProperties.getPublicKey());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new ServerSideException(ErrorCode.RESPONSE_ERROR, "Failed to load public key");
        }
    }
    
    private Jws<Claims> parseToken(final String token) {
        try {
            return Jwts.parser().verifyWith(getPublicKey()).build().parseSignedClaims(token);
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            log.error("JWT - Token parsing failed: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Check token is expired or not.
     */
    private boolean isTokenExpired(final String token) {
        return parseToken(token).getPayload().getExpiration().before(new Date());
    }
    
    public boolean isRefreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return TokenType.REFRESH.toString().equals(claims.get("type"));
    }
    
    public Map<TokenType, String> refreshTokens(User user) {
        
        // Revoke all existing tokens
        revokeAllUserTokens(user.getUserId());
        
        // Generate and save new tokens
        return generateAndSaveTokens(user);
    }
}
