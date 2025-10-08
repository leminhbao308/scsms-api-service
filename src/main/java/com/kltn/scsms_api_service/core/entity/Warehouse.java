package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "warehouse",
    schema = GeneralConstant.DB_SCHEMA_DEV,
    uniqueConstraints = {
    @UniqueConstraint(name = "uq_warehouse_branch", columnNames = {"branch_id"})
})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    
    @OneToOne(optional = false)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_warehouse_branch"))
    private Branch branch;
    
    
    // Không cho xoá/disable kho — enforced ở service; cờ để chặn logic
    @Column(name = "locked", nullable = false)
    @Builder.Default
    private boolean locked = true;
}
