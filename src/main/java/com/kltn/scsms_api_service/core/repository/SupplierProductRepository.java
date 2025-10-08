package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.SupplierProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, UUID> {
    List<SupplierProduct> findByProductProductId(UUID productId);
}
