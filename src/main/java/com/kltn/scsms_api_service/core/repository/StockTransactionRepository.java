package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {
}
