package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "center_business_hours", schema = GeneralConstant.DB_SCHEMA_DEV)
public class CenterBusinessHours extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "business_hours_id", nullable = false)
    private UUID businessHoursId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;
    
    @Column(name = "day_of_week", nullable = false, length = 10)
    private String dayOfWeek;
    
    @Column(name = "open_time")
    private LocalTime openTime;
    
    @Column(name = "close_time")
    private LocalTime closeTime;
    
    @Column(name = "is_closed", nullable = false)
    @Builder.Default
    private Boolean isClosed = false;
    
    // Utility methods
    public void setCenter(Center center) {
        this.center = center;
    }
    
    public boolean isOpen() {
        return !isClosed && openTime != null && closeTime != null;
    }
    
    public boolean isCurrentlyOpen() {
        if (isClosed) return false;
        
        LocalTime now = LocalTime.now();
        return now.isAfter(openTime) && now.isBefore(closeTime);
    }
}
