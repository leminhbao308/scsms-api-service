package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.SalesOrder;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID>, JpaSpecificationExecutor<SalesOrder> {

    List<SalesOrder> findByStatusNotOrderByCreatedDateDesc(SalesStatus status);

    List<SalesOrder> findByStatusOrderByCreatedDateDesc(SalesStatus status);
}
