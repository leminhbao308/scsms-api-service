package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.core.constants.GeneralConstant;
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
@Table(name = "vehicle_models", schema = GeneralConstant.DB_SCHEMA_DEV)
public class VehicleModel extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "model_id", nullable = false)
    private UUID modelId;
    
    @Column(name = "model_name", unique = true, nullable = false)
    private String modelName;
    
    @Column(name = "model_code", unique = true, nullable = false)
    private String modelCode;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @JoinColumn(name = "brand_id", nullable = false)
    private UUID brandId;
    
    @JoinColumn(name = "type_id", nullable = false)
    private UUID typeId;
}
