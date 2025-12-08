package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vehicle_brands", schema = GeneralConstant.DB_SCHEMA_DEV)
public class VehicleBrand extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "brand_id", nullable = false)
    private UUID brandId;
    
    @Column(name = "brand_name", unique = true, nullable = false)
    private String brandName;
    
    @Column(name = "brand_code", unique = true, nullable = false)
    private String brandCode;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "brand_logo_url", length = Integer.MAX_VALUE)
    private String brandLogoUrl;
}
