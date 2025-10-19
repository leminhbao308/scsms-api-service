package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, UUID> {

  List<PromotionUsage> findByOrderId(UUID orderId);

  List<PromotionUsage> findByCustomer_UserId(UUID customerId);
}
