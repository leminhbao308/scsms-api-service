package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "price_book",
    schema = GeneralConstant.DB_SCHEMA_DEV,
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_price_book_code", columnNames = {"code"})
    })
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PriceBook extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    
    @Column(name = "code", length = 64, nullable = false)
    private String code;
    
    
    @Column(name = "name", length = 200, nullable = false)
    private String name;
    
    
    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "VND"; // VND
    
    
    @Column(name = "valid_from")
    @Builder.Default
    private LocalDateTime validFrom = LocalDateTime.now();
    
    
    @Column(name = "valid_to")
    private LocalDateTime validTo;
    
    @Column(name = "branch_id")
    private UUID branchId; // Chi nhánh áp dụng bảng giá (nullable - null = global price book)
    
    
    @OneToMany(mappedBy = "priceBook", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PriceBookItem> items = new ArrayList<>();
}
