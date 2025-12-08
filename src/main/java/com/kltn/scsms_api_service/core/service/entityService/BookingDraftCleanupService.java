package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.BookingDraft;
import com.kltn.scsms_api_service.core.entity.enumAttribute.DraftStatus;
import com.kltn.scsms_api_service.core.repository.BookingDraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service để cleanup các abandoned drafts
 * Chạy định kỳ để mark và xóa các draft bị bỏ dở
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingDraftCleanupService {
    
    private final BookingDraftRepository draftRepository;
    
    /**
     * Cleanup job chạy mỗi giờ
     * Tìm và mark các draft bị abandoned (không có activity > 24h hoặc hết hạn)
     */
    @Scheduled(cron = "0 0 * * * ?") // Mỗi giờ
    @Transactional
    public void cleanupAbandonedDrafts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredThreshold = now.minusHours(24);
        
        log.info("Starting cleanup job for abandoned drafts...");
        
        // Tìm các draft IN_PROGRESS đã hết hạn hoặc không có activity > 24h
        List<BookingDraft> abandonedDrafts = draftRepository.findAbandonedDrafts(
            now, // expires_at < now
            expiredThreshold // last_activity_at < 24h ago
        );
        
        if (abandonedDrafts.isEmpty()) {
            log.debug("No abandoned drafts found");
            return;
        }
        
        log.info("Found {} abandoned drafts, marking as ABANDONED", abandonedDrafts.size());
        
        // Batch update để tối ưu performance
        abandonedDrafts.forEach(draft -> {
            draft.setStatus(DraftStatus.ABANDONED);
            draft.setModifiedDate(now);
        });
        
        draftRepository.saveAll(abandonedDrafts);
        
        log.info("Successfully marked {} drafts as ABANDONED", abandonedDrafts.size());
    }
    
    /**
     * Cleanup job chạy hàng ngày lúc 2 AM
     * Xóa các draft ABANDONED cũ hơn 30 ngày (optional - để giảm storage)
     */
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
    @Transactional
    public void deleteOldAbandonedDrafts() {
        LocalDateTime deleteThreshold = LocalDateTime.now().minusDays(30);
        
        log.info("Starting cleanup job to delete old abandoned drafts (>30 days)...");
        
        // Chỉ xóa draft ABANDONED cũ hơn 30 ngày
        // Giữ lại COMPLETED để analytics
        List<BookingDraft> oldAbandoned = draftRepository.findByStatusAndModifiedDateBefore(
            DraftStatus.ABANDONED,
            deleteThreshold
        );
        
        if (oldAbandoned.isEmpty()) {
            log.debug("No old abandoned drafts to delete");
            return;
        }
        
        log.info("Found {} old abandoned drafts (>30 days), deleting...", oldAbandoned.size());
        
        draftRepository.deleteAll(oldAbandoned);
        
        log.info("Successfully deleted {} old abandoned drafts", oldAbandoned.size());
    }
}

