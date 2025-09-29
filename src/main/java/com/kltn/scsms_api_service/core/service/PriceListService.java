package com.kltn.scsms_api_service.core.service;

import com.kltn.scsms_api_service.core.dto.priceManagement.param.PriceListFilterParam;
import com.kltn.scsms_api_service.core.dto.priceManagement.request.PriceListHeaderRequest;
import com.kltn.scsms_api_service.core.dto.priceManagement.response.PriceListHeaderResponse;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CustomerRank;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface PriceListService {

    // Create a new price list
    PriceListHeaderResponse createPriceList(PriceListHeaderRequest request);
    
    // Get price list by ID
    PriceListHeaderResponse getPriceListById(UUID priceListId);
    
    // Get price list by code
    PriceListHeaderResponse getPriceListByCode(String priceListCode);
    
    // Update a price list
    PriceListHeaderResponse updatePriceList(UUID priceListId, PriceListHeaderRequest request);
    
    // Delete a price list
    void deletePriceList(UUID priceListId);
    
    // Search and filter price lists
    Page<PriceListHeaderResponse> searchPriceLists(PriceListFilterParam filterParam);
    
    // Update price list status (e.g., draft, active, inactive)
    PriceListHeaderResponse updatePriceListStatus(UUID priceListId, String status);
    
    // Get active price lists for a branch
    List<PriceListHeaderResponse> getActivePriceListsByBranch(UUID branchId);
    
    // Get active price lists for a branch and customer rank
    List<PriceListHeaderResponse> getActivePriceListsByBranchAndCustomerRank(UUID branchId, CustomerRank customerRank);
}
