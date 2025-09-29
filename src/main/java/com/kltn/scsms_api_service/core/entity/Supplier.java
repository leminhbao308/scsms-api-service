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
@Table(name = "suppliers", schema = GeneralConstant.DB_SCHEMA_DEV)
public class Supplier extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "supplier_id", nullable = false)
    private UUID supplierId;
    
    @Column(name = "supplier_name", nullable = false)
    private String supplierName;
    
    @Column(name = "contact_person")
    private String contactPerson;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "address", length = Integer.MAX_VALUE)
    private String address;
    
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "bank_account")
    private String bankAccount;
}
