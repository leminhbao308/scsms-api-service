package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
}
