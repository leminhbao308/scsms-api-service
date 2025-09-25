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
@Table(name = "vehicle_types", schema = GeneralConstant.DB_SCHEMA_DEV)
public class VehicleType extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "type_id", nullable = false)
    private UUID typeId;
    
    @Column(name = "type_name", unique = true, nullable = false)
    private String typeName;
    
    @Column(name = "type_code", unique = true, nullable = false)
    private String typeCode;
    
    @Column(name = "description", length = 500)
    private String description;
}
