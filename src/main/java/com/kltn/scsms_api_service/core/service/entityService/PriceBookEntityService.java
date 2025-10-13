package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.PriceBook;
import com.kltn.scsms_api_service.core.repository.PriceBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceBookEntityService {
    
    private final PriceBookRepository repo;
    
    /**
     * Get active price books in the given time range.
     * If 'from' is null, it defaults to now.
     * If 'to' is null, it fetches all active price books from 'from' onwards.
     *
     * @param from If null, defaults to now
     * @param to   If null, fetches all active price books from 'from' onwards
     * @return List of active PriceBooks
     */
    public List<PriceBook> getActivePriceInRange(LocalDateTime from, LocalDateTime to) {
        LocalDateTime fromCheck = from;
        
        if (fromCheck == null) fromCheck = LocalDateTime.now();
        
        if (to == null)
            return repo.findByIsActiveAndValidFromLessThanEqual(true, fromCheck);
        
        return repo.findByIsActiveAndValidFromLessThanEqualAndValidToGreaterThanEqual(true, fromCheck, to);
    }
    
    public PriceBook create(PriceBook priceBook) {
        return repo.save(priceBook);
    }
    
    public PriceBook require(UUID bookId) {
        return repo.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Price book not found"));
    }
    
    public PriceBook getRefById(UUID bookId) {
        return repo.getReferenceById(bookId);
    }
    
    public List<PriceBook> getActivePriceBooksInRange(LocalDateTime validFrom, LocalDateTime validTo) {
        if (validFrom == null) validFrom = LocalDateTime.now();
        if (validTo == null) return repo.findByIsActiveAndValidToGreaterThanEqual(true, validFrom);
        return repo.findByIsActiveAndValidFromLessThanEqualAndValidToGreaterThanEqual(true, validFrom, validTo);
    }
    
    /**
     * Get active price books for a specific branch in the given time range
     * @param branchId Branch ID
     * @param from If null, defaults to now
     * @param to If null, fetches all active price books from 'from' onwards
     * @return List of active PriceBooks for the branch
     */
    public List<PriceBook> getActivePriceInRangeForBranch(UUID branchId, LocalDateTime from, LocalDateTime to) {
        LocalDateTime fromCheck = from;
        
        if (fromCheck == null) fromCheck = LocalDateTime.now();
        
        if (to == null)
            return repo.findByBranchIdAndIsActiveAndValidFromLessThanEqual(branchId, true, fromCheck);
        
        return repo.findByBranchIdAndIsActiveAndValidFromLessThanEqualAndValidToGreaterThanEqual(branchId, true, fromCheck, to);
    }
}
