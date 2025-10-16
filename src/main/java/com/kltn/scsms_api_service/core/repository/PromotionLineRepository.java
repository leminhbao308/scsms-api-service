package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.PromotionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PromotionLineRepository extends JpaRepository<PromotionLine, UUID> {
}
