package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity quản lý quan hệ nhiều-nhiều giữa BookingDraft và Service
 * Cho phép một draft có nhiều dịch vụ
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "booking_draft_services", schema = GeneralConstant.DB_SCHEMA_DEV,
       uniqueConstraints = @UniqueConstraint(name = "uk_draft_service", columnNames = {"draft_id", "service_id"}))
public class DraftService {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "draft_service_id", nullable = false)
    private UUID draftServiceId;
    
    /**
     * Booking draft
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id", nullable = false, foreignKey = @ForeignKey(name = "fk_draft_service_draft"))
    private BookingDraft draft;
    
    @Column(name = "draft_id", nullable = false, insertable = false, updatable = false)
    private UUID draftId;
    
    /**
     * Service
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false, foreignKey = @ForeignKey(name = "fk_draft_service_service"))
    private Service service;
    
    @Column(name = "service_id", nullable = false, insertable = false, updatable = false)
    private UUID serviceId;
    
    /**
     * Tên dịch vụ (snapshot tại thời điểm chọn)
     */
    @Column(name = "service_name", nullable = false, length = 255)
    private String serviceName;
    
    /**
     * Timestamps
     */
    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}

