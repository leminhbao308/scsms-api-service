package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CustomerRank;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PriceListScope;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PriceListStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "price_list_headers", schema = GeneralConstant.DB_SCHEMA_DEV)
public class PriceListHeader extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "price_list_id", nullable = false)
    private UUID priceListId;
    
    @Column(name = "price_list_code", unique = true, nullable = false)
    private String priceListCode;
    
    @Column(name = "price_list_name", nullable = false)
    private String priceListName;
    
    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;
    
    // Phạm vi áp dụng
    @Column(name = "scope")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PriceListScope scope = PriceListScope.CENTER;
    
    // Reference đến Centers hoặc Branchs tùy theo scope
    // Nếu scope = CENTER thì centers không null, branchs null và ngược lại
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "price_list_centers",
        joinColumns = @JoinColumn(name = "price_list_id"),
        inverseJoinColumns = @JoinColumn(name = "center_id")
    )
    private List<Center> centers;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "price_list_branches",
        joinColumns = @JoinColumn(name = "price_list_id"),
        inverseJoinColumns = @JoinColumn(name = "branch_id")
    )
    private List<Branch> branches;
    
    // Thời gian hiệu lực
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;
    
    // Thời gian hết hạn (có thể null nếu không có hạn)
    @Column(name = "expiration_date")
    private LocalDate expirationDate;
    
    // Trạng thái
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PriceListStatus status = PriceListStatus.DRAFT;
    
    // Áp dụng cho hạng khách hàng nào (array of strings)
    @Column(name = "customer_ranks")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<CustomerRank> customerRanks;
    
    // Priority khi có nhiều bảng giá cùng áp dụng
    // Giá trị càng cao thì ưu tiên áp dụng trước
    // 0 là thấp nhất
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;
    
    // Currency
    @Column(name = "currency", nullable = false)
    @Builder.Default
    private String currency = "VND";
    
    // Ghi chú nội bộ
    @Column(name = "internal_notes", length = Integer.MAX_VALUE)
    private String internalNotes;
    
    // Người phê duyệt
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "approved_date")
    private java.time.LocalDateTime approvedDate;
    
    // One-to-many relationship với price list details
    @OneToMany(mappedBy = "priceListHeader", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<PriceListDetail> priceListDetails = new ArrayList<>();
    
    // Utility methods
    public void addPriceListDetail(PriceListDetail detail) {
        priceListDetails.add(detail);
        detail.setPriceListHeader(this);
    }
    
    public void removePriceListDetail(PriceListDetail detail) {
        priceListDetails.remove(detail);
        detail.setPriceListHeader(null);
    }
    
    public boolean isActive() {
        return status == PriceListStatus.ACTIVE &&
            !LocalDate.now().isBefore(effectiveDate) &&
            (expirationDate == null || !LocalDate.now().isAfter(expirationDate));
    }
    
    public void activate() {
        if (status == PriceListStatus.APPROVED) {
            this.status = PriceListStatus.ACTIVE;
        }
    }
    
    public void expire() {
        this.status = PriceListStatus.EXPIRED;
    }
}
