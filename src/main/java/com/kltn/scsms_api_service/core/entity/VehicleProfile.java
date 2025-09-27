package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vehicle_profiles", schema = GeneralConstant.DB_SCHEMA_DEV)
public class VehicleProfile extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;
    
    @Column(name = "license_plate", nullable = false)
    private String licensePlate;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @JoinColumn(name = "vehicle_brand_id", nullable = false)
    private UUID vehicleBrandId;
    
    @JoinColumn(name = "vehicle_type_id", nullable = false)
    private UUID vehicleTypeId;
    
    @JoinColumn(name = "vehicle_model_id", nullable = false)
    private UUID vehicleModelId;
    
    @JoinColumn(name = "owner_id", nullable = false)
    private UUID ownerId;
    
    @Column(name = "distance_traveled")
    @Builder.Default
    private Double distanceTraveled = 0.0;
}
