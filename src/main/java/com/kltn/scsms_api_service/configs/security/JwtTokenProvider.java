package com.kltn.scsms_api_service.configs.security;

import com.kltn.scsms_api_service.configs.property.JwtTokenProperties;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.exception.ServerSideException;
import com.kltn.scsms_api_service.core.service.entityService.TokenService;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private final JwtTokenProperties jwtTokenProperties;
    private final TokenService tokenService;

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_TYPE = "Bearer";

    public Claims getClaimsFromToken(final String token) {
        return tokenService.getClaimsFromToken(token);
    }

    /** Validate token with request context. */
    public boolean validateToken(final String token, final HttpServletRequest httpServletRequest) {
        try {
            boolean isTokenValid = tokenService.isValidTokenAndNotExpired(token);
            if (!isTokenValid) {
                log.error("JWT - Token validation failed");
                httpServletRequest.setAttribute("invalid", "Token validation failed");
            }
            return isTokenValid;
        } catch (UnsupportedJwtException e) {
            log.error("JWT - Unsupported JWT token!");
            httpServletRequest.setAttribute("unsupported", "Unsupported JWT token!");
        } catch (MalformedJwtException e) {
            log.error("JWT - Invalid JWT token!");
            httpServletRequest.setAttribute("invalid", "Invalid JWT token!");
        } catch (ExpiredJwtException e) {
            log.error("JWT - Expired JWT token!");
            httpServletRequest.setAttribute("expired", "Expired JWT token!");
        } catch (IllegalArgumentException e) {
            log.error("JWT - Jwt claims string is empty");
            httpServletRequest.setAttribute("illegal", "JWT claims string is empty.");
        }

        return false;
    }

    /** Extract jwt from bearer string. */
    public String extractJwtFromBearerString(final String bearer) {
        if (StringUtils.hasText(bearer) && bearer.startsWith(String.format("%s ", TOKEN_TYPE))) {
            return bearer.substring(TOKEN_TYPE.length() + 1);
        }
        return null;
    }

    /** Extract jwt from request. */
    public String extractJwtFromRequest(final HttpServletRequest request) {
        return extractJwtFromBearerString(request.getHeader(TOKEN_HEADER));
    }

    /** Parsing token. */
    private Jws<Claims> parseToken(final String token) {
        try {
            return Jwts.parser().verifyWith(getPublicKey()).build().parseSignedClaims(token);
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            log.error("JWT - Token parsing failed: {}", e.getMessage());
            throw e;
        }
    }

    /** Check token is expired or not. */
    private boolean isTokenExpired(final String token) {
        return parseToken(token).getPayload().getExpiration().before(new Date());
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

    /** Check if token is refresh token */
    public boolean isRefreshToken(String token) {
        return tokenService.isRefreshToken(token);
    }

    /** Check if token is valid */
    public boolean isTokenValid(String token) {
        return tokenService.isTokenExistedAndNotRevoked(token);
    }
}
