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
@Table(name = "promotion_types", schema = GeneralConstant.DB_SCHEMA_DEV)
public class PromotionType extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "promotion_type_id", nullable = false)
    private UUID promotionTypeId;
    
    @Column(name = "type_code", unique = true, nullable = false, length = 50)
    private String typeCode;
    
    @Column(name = "type_name", nullable = false, length = 150)
    private String typeName;
    
    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;
}
