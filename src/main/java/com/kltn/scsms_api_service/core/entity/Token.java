package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.core.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.TokenType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens", schema = GeneralConstant.DB_SCHEMA_DEV)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    @Id
    @Column(length = Integer.MAX_VALUE)
    private String token;

    @ManyToOne private User user;

    private boolean revoked;

    private boolean expired;

    @Column(name = "token_type")
    @Enumerated(EnumType.STRING)
    private TokenType type;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
