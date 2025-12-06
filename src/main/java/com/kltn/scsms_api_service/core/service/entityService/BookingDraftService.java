package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.BookingDraft;
import com.kltn.scsms_api_service.core.entity.DraftService;
import com.kltn.scsms_api_service.core.entity.enumAttribute.DraftStatus;
import com.kltn.scsms_api_service.core.repository.BookingDraftRepository;
import com.kltn.scsms_api_service.core.repository.BookingDraftServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service quản lý booking drafts
 * Xử lý tạo, cập nhật, và cleanup các draft
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingDraftService {
    
    private final BookingDraftRepository draftRepository;
    private final BookingDraftServiceRepository draftServiceRepository;
    private final ServiceService serviceService;
    
    /**
     * Tạo draft mới hoặc lấy draft hiện có của session
     * Nếu user đã có draft IN_PROGRESS → Mark old drafts as ABANDONED
     */
    @Transactional
    public BookingDraft getOrCreateDraft(String sessionId, UUID customerId) {
        // Tìm draft hiện có của session
        Optional<BookingDraft> existingDraft = draftRepository.findBySessionIdAndStatus(
            sessionId, 
            DraftStatus.IN_PROGRESS
        );
        
        if (existingDraft.isPresent()) {
            log.info("Found existing draft: draft_id={}, session_id={}, current_step={}", 
                    existingDraft.get().getDraftId(), sessionId, existingDraft.get().getCurrentStep());
            return existingDraft.get();
        }
        
        // Nếu có customerId, tìm và mark old drafts as ABANDONED
        if (customerId != null) {
            List<BookingDraft> oldDrafts = draftRepository.findByCustomerIdAndStatus(
                customerId, 
                DraftStatus.IN_PROGRESS
            );
            
            if (!oldDrafts.isEmpty()) {
                log.info("Found {} old IN_PROGRESS drafts for customer_id={}, marking as ABANDONED", 
                        oldDrafts.size(), customerId);
                
                oldDrafts.forEach(draft -> {
                    draft.setStatus(DraftStatus.ABANDONED);
                    draftRepository.save(draft);
                    log.debug("   → Marked draft_id={} as ABANDONED", draft.getDraftId());
                });
            }
        }
        
        // Tạo draft mới
        BookingDraft newDraft = BookingDraft.builder()
            .sessionId(sessionId)
            .customerId(customerId)
            .status(DraftStatus.IN_PROGRESS)
            .currentStep(1)
            .expiresAt(LocalDateTime.now().plusHours(24)) // 24 hours TTL
            .lastActivityAt(LocalDateTime.now())
            .build();
        
        BookingDraft saved = draftRepository.save(newDraft);
        
        log.info("Created new draft: draft_id={}, session_id={}, customer_id={}", 
                saved.getDraftId(), sessionId, customerId);
        
        return saved;
    }
    
    /**
     * Lấy draft theo ID
     */
    public BookingDraft getDraft(UUID draftId) {
        return draftRepository.findById(draftId)
            .orElseThrow(() -> new RuntimeException("Draft not found: " + draftId));
    }
    
    /**
     * Reset draft về trạng thái ban đầu (step 1, clear tất cả data)
     * Dùng khi user muốn bắt đầu đặt lịch mới
     */
    @Transactional
    public BookingDraft resetDraft(UUID draftId) {
        BookingDraft draft = getDraft(draftId);
        
        log.info("Resetting draft: draft_id={}, current_step={}, status={}", 
                draftId, draft.getCurrentStep(), draft.getStatus());
        
        // Clear tất cả data
        draft.setVehicleId(null);
        draft.setVehicleLicensePlate(null);
        draft.setDateTime(null);
        draft.setBranchId(null);
        draft.setBranchName(null);
        draft.setServiceId(null);
        draft.setServiceType(null);
        draft.setBayId(null);
        draft.setBayName(null);
        draft.setTimeSlot(null);
        
        // Reset về step 1
        draft.setCurrentStep(1);
        draft.setStatus(DraftStatus.IN_PROGRESS);
        draft.setLastActivityAt(LocalDateTime.now());
        draft.setExpiresAt(LocalDateTime.now().plusHours(24)); // Reset TTL
        
        // Xóa tất cả services trong bảng quan hệ
        List<DraftService> draftServices = draftServiceRepository.findByDraftId(draftId);
        if (!draftServices.isEmpty()) {
            draftServiceRepository.deleteAll(draftServices);
            log.info("Deleted {} draft services", draftServices.size());
        }
        
        BookingDraft saved = draftRepository.save(draft);
        
        log.info("Draft reset successfully: draft_id={}, current_step={}, status={}", 
                saved.getDraftId(), saved.getCurrentStep(), saved.getStatus());
        
        return saved;
    }
    
    /**
     * Lấy draft theo session ID
     */
    public Optional<BookingDraft> getDraftBySession(String sessionId) {
        return draftRepository.findBySessionIdAndStatus(sessionId, DraftStatus.IN_PROGRESS);
    }
    
    /**
     * Cập nhật draft với logging chi tiết
     * 
     * @param draftId ID của draft
     * @param updateType Loại cập nhật (VEHICLE, DATE, BRANCH, SERVICE, BAY, TIME)
     * @param userMessage Tin nhắn của user (để log)
     * @param updates Dữ liệu cập nhật
     */
    @Transactional
    public BookingDraft updateDraft(
            UUID draftId, 
            String updateType,
            String userMessage,
            DraftUpdate updates) {
        
        BookingDraft draft = getDraft(draftId);
        
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("UPDATING DRAFT: draft_id={}, update_type={}", draftId, updateType);
        log.info("USER MESSAGE: {}", userMessage);
        log.info("DRAFT BEFORE UPDATE:");
        logDraftState(draft);
        
        // Lưu giá trị cũ để check có thay đổi không (cho cascade update)
        UUID oldBranchId = draft.getBranchId();
        LocalDateTime oldDateTime = draft.getDateTime();
        UUID oldServiceId = draft.getServiceId();
        UUID oldBayId = draft.getBayId();
        
        // Cập nhật các fields
        boolean hasChanges = false;
        
        if (updates.getVehicleId() != null) {
            draft.setVehicleId(updates.getVehicleId());
            hasChanges = true;
            log.info("Updated vehicle_id: {}", updates.getVehicleId());
        }
        
        if (updates.getVehicleLicensePlate() != null) {
            draft.setVehicleLicensePlate(updates.getVehicleLicensePlate());
            hasChanges = true;
            log.info("Updated vehicle_license_plate: {}", updates.getVehicleLicensePlate());
        }
        
        if (updates.getDateTime() != null) {
            draft.setDateTime(updates.getDateTime());
            hasChanges = true;
            log.info("Updated date_time: {}", updates.getDateTime());
        }
        
        if (updates.getBranchId() != null) {
            draft.setBranchId(updates.getBranchId());
            hasChanges = true;
            log.info("Updated branch_id: {}", updates.getBranchId());
        }
        
        if (updates.getBranchName() != null) {
            draft.setBranchName(updates.getBranchName());
            hasChanges = true;
            log.info("Updated branch_name: {}", updates.getBranchName());
        }
        
        // Xử lý dịch vụ: Thêm vào bảng quan hệ (nếu có serviceId và serviceType)
        if (updates.getServiceId() != null && updates.getServiceType() != null) {
            // Kiểm tra xem dịch vụ đã tồn tại chưa
            if (!draftServiceRepository.existsByDraftIdAndServiceId(draftId, updates.getServiceId())) {
                // Load Service entity từ database (cần thiết vì serviceId có insertable=false)
                com.kltn.scsms_api_service.core.entity.Service service = serviceService.findById(updates.getServiceId())
                    .orElseThrow(() -> new RuntimeException("Service not found: " + updates.getServiceId()));
                
                // Thêm dịch vụ mới vào bảng quan hệ
                DraftService draftService = DraftService.builder()
                    .draft(draft)
                    .service(service) // Set service object để JPA tự động set service_id
                    .serviceName(updates.getServiceType())
                    .createdDate(LocalDateTime.now())
                    .build();
                draftServiceRepository.save(draftService);
                log.info("Added service to draft: service_id={}, service_name={}", 
                        updates.getServiceId(), updates.getServiceType());
            } else {
                log.info("Service already exists in draft: service_id={}, service_name={}", 
                        updates.getServiceId(), updates.getServiceType());
            }
            
            // Cập nhật service_id và service_type trong draft (cho dịch vụ đầu tiên/chính)
            // Nếu chưa có dịch vụ nào → Set làm dịch vụ chính
            if (draft.getServiceId() == null) {
                draft.setServiceId(updates.getServiceId());
                draft.setServiceType(updates.getServiceType());
                hasChanges = true;
                log.info("Set as primary service: service_id={}, service_name={}", 
                        updates.getServiceId(), updates.getServiceType());
            }
        }
        
        if (updates.getBayId() != null) {
            draft.setBayId(updates.getBayId());
            hasChanges = true;
            log.info("Updated bay_id: {}", updates.getBayId());
        }
        
        if (updates.getBayName() != null) {
            draft.setBayName(updates.getBayName());
            hasChanges = true;
            log.info("Updated bay_name: {}", updates.getBayName());
        }
        
        if (updates.getTimeSlot() != null) {
            draft.setTimeSlot(updates.getTimeSlot());
            hasChanges = true;
            log.info("Updated time_slot: {}", updates.getTimeSlot());
        }
        
        // CRITICAL: Cascade update - Reset dependent fields khi có thay đổi
        // Phải check TRƯỚC khi update current_step
        if (hasChanges) {
            // Check từng loại thay đổi và reset dependent fields
            if (oldBranchId != null && updates.getBranchId() != null && 
                !oldBranchId.equals(updates.getBranchId())) {
                log.info("Branch changed from {} to {}, resetting dependent fields: service, bay, time, date", 
                        oldBranchId, updates.getBranchId());
                resetDependentFields(draft, draftId, "BRANCH");
            } else if (oldDateTime != null && updates.getDateTime() != null && 
                       !oldDateTime.equals(updates.getDateTime())) {
                log.info("Date changed from {} to {}, resetting dependent fields: service, bay, time", 
                        oldDateTime, updates.getDateTime());
                resetDependentFields(draft, draftId, "DATE");
            } else if (oldServiceId != null && updates.getServiceId() != null && 
                       !oldServiceId.equals(updates.getServiceId())) {
                log.info("Service changed from {} to {}, resetting dependent fields: bay, time", 
                        oldServiceId, updates.getServiceId());
                resetDependentFields(draft, draftId, "SERVICE");
            } else if (oldBayId != null && updates.getBayId() != null && 
                       !oldBayId.equals(updates.getBayId())) {
                log.info("Bay changed from {} to {}, resetting dependent fields: time", 
                        oldBayId, updates.getBayId());
                resetDependentFields(draft, draftId, "BAY");
            }
            // Vehicle và Time không cần reset gì
        }
        
        if (hasChanges) {
            // Cập nhật current_step và last_activity
            draft.updateCurrentStep();
            draft.updateActivity();
            
            BookingDraft saved = draftRepository.save(draft);
            
            log.info("DRAFT AFTER UPDATE:");
            logDraftState(saved);
            log.info("═══════════════════════════════════════════════════════════════");
            
            return saved;
        } else {
            log.warn("No changes detected in update request");
            log.info("═══════════════════════════════════════════════════════════════");
            return draft;
        }
    }
    
    /**
     * Reset các fields phụ thuộc khi có thay đổi
     * Logic cascade update:
     * - Đổi branch → Reset: service, bay, time, date
     * - Đổi date → Reset: service, bay, time
     * - Đổi service → Reset: bay, time
     * - Đổi bay → Reset: time
     * - Đổi vehicle/time → Không reset gì
     */
    private void resetDependentFields(BookingDraft draft, UUID draftId, String updateType) {
        log.info("Resetting dependent fields for updateType: {}", updateType);
        
        switch (updateType) {
            case "BRANCH":
                // Reset: service, bay, time, date
                log.info("Resetting service, bay, time, date due to branch change");
                draft.setServiceId(null);
                draft.setServiceType(null);
                draft.setBayId(null);
                draft.setBayName(null);
                draft.setTimeSlot(null);
                draft.setDateTime(null);
                
                // Xóa tất cả services trong bảng quan hệ
                List<DraftService> draftServices = draftServiceRepository.findByDraftId(draftId);
                if (!draftServices.isEmpty()) {
                    draftServiceRepository.deleteAll(draftServices);
                    log.info("Deleted {} draft services due to branch change", draftServices.size());
                }
                break;
                
            case "DATE":
                // Reset: service, bay, time
                log.info("Resetting service, bay, time due to date change");
                draft.setServiceId(null);
                draft.setServiceType(null);
                draft.setBayId(null);
                draft.setBayName(null);
                draft.setTimeSlot(null);
                
                // Xóa tất cả services trong bảng quan hệ
                draftServices = draftServiceRepository.findByDraftId(draftId);
                if (!draftServices.isEmpty()) {
                    draftServiceRepository.deleteAll(draftServices);
                    log.info("Deleted {} draft services due to date change", draftServices.size());
                }
                break;
                
            case "SERVICE":
                // Reset: bay, time
                log.info("Resetting bay, time due to service change");
                draft.setBayId(null);
                draft.setBayName(null);
                draft.setTimeSlot(null);
                
                // Xóa tất cả services trong bảng quan hệ (sẽ được thêm lại sau)
                draftServices = draftServiceRepository.findByDraftId(draftId);
                if (!draftServices.isEmpty()) {
                    draftServiceRepository.deleteAll(draftServices);
                    log.info("Deleted {} draft services due to service change", draftServices.size());
                }
                break;
                
            case "BAY":
                // Reset: time
                log.info("Resetting time due to bay change");
                draft.setTimeSlot(null);
                break;
                
            case "VEHICLE":
            case "TIME":
                // Không reset gì
                log.info("No dependent fields to reset for updateType: {}", updateType);
                break;
                
            default:
                log.warn("Unknown updateType: {}, no dependent fields reset", updateType);
                break;
        }
    }
    
    /**
     * Log chi tiết state của draft
     */
    private void logDraftState(BookingDraft draft) {
        log.info("   ┌─ Draft State ──────────────────────────────────────────────");
        log.info("   │ draft_id: {}", draft.getDraftId());
        log.info("   │ session_id: {}", draft.getSessionId());
        log.info("   │ customer_id: {}", draft.getCustomerId());
        log.info("   │ current_step: {}", draft.getCurrentStep());
        log.info("   │ status: {}", draft.getStatus());
        log.info("   ├─ Selected Data ────────────────────────────────────────────");
        log.info("   │ vehicle_id: {} | vehicle_license_plate: {}", 
                draft.getVehicleId(), draft.getVehicleLicensePlate());
        log.info("   │ date_time: {}", draft.getDateTime());
        log.info("   │ branch_id: {} | branch_name: {}", 
                draft.getBranchId(), draft.getBranchName());
        // Hiển thị tất cả dịch vụ từ bảng quan hệ (ưu tiên)
        List<DraftService> draftServices = draftServiceRepository.findByDraftId(draft.getDraftId());
        if (!draftServices.isEmpty()) {
            log.info("   │ services ({}):", draftServices.size());
            for (DraftService ds : draftServices) {
                // Lấy service_id từ service relationship vì serviceId có insertable=false
                UUID serviceId = ds.getService() != null ? ds.getService().getServiceId() : null;
                log.info("   │   → service_id: {}, service_name: {}", 
                        serviceId, ds.getServiceName());
            }
        } else if (draft.getServiceId() != null || draft.getServiceType() != null) {
            // Fallback: Nếu không có trong bảng quan hệ, hiển thị từ draft (tương thích ngược)
            log.info("   │ service_id: {} | service_type: {}", 
                    draft.getServiceId(), draft.getServiceType());
        } else {
            log.info("   │ service_id: null | service_type: null");
        }
        log.info("   │ bay_id: {} | bay_name: {}", 
                draft.getBayId(), draft.getBayName());
        log.info("   │ time_slot: {}", draft.getTimeSlot());
        log.info("   ├─ Validation ────────────────────────────────────────────────");
        log.info("   │ hasVehicle: {} | hasDate: {} | hasBranch: {}", 
                draft.hasVehicle(), draft.hasDate(), draft.hasBranch());
        log.info("   │ hasService: {} | hasBay: {} | hasTime: {}", 
                draft.hasService(), draft.hasBay(), draft.hasTime());
        log.info("   │ isComplete: {}", draft.isComplete());
        log.info("   └────────────────────────────────────────────────────────────");
    }
    
    /**
     * Mark draft as COMPLETED (khi đã tạo booking thành công)
     */
    @Transactional
    public void completeDraft(UUID draftId) {
        BookingDraft draft = getDraft(draftId);
        draft.setStatus(DraftStatus.COMPLETED);
        draftRepository.save(draft);
        
        log.info("Marked draft as COMPLETED: draft_id={}", draftId);
    }
    
    /**
     * Mark draft as ABANDONED (soft delete - dùng cho cleanup job)
     */
    @Transactional
    public void abandonDraft(UUID draftId) {
        BookingDraft draft = getDraft(draftId);
        draft.setStatus(DraftStatus.ABANDONED);
        draftRepository.save(draft);
        
        log.info("Marked draft as ABANDONED: draft_id={}", draftId);
    }
    
    /**
     * Xóa draft thực sự khỏi database (hard delete)
     * Dùng khi user chủ động xóa draft từ UI
     * Sẽ xóa cả các DraftService records liên quan (cascade)
     */
    @Transactional
    public void deleteDraft(UUID draftId) {
        log.info("Deleting draft (hard delete): draft_id={}", draftId);
        
        // Xóa tất cả DraftService records liên quan trước (để tránh foreign key constraint)
        draftServiceRepository.deleteByDraftId(draftId);
        log.info("Deleted all DraftService records for draft: draft_id={}", draftId);
        
        // Xóa draft chính
        draftRepository.deleteById(draftId);
        
        log.info("Successfully deleted draft: draft_id={}", draftId);
    }
    
    /**
     * Xóa draft theo session ID (hard delete)
     * Dùng khi user chủ động xóa draft từ UI
     */
    @Transactional
    public void deleteDraftBySession(String sessionId) {
        log.info("Deleting draft by session (hard delete): session_id={}", sessionId);
        
        Optional<BookingDraft> draftOpt = getDraftBySession(sessionId);
        if (draftOpt.isPresent()) {
            UUID draftId = draftOpt.get().getDraftId();
            deleteDraft(draftId);
            log.info("Successfully deleted draft by session: draft_id={}, session_id={}", draftId, sessionId);
        } else {
            log.warn("No active draft found for session to delete: session_id={}", sessionId);
        }
    }
    
    /**
     * Thêm dịch vụ vào draft (nếu chưa có)
     */
    @Transactional
    public void addServiceToDraft(UUID draftId, UUID serviceId, String serviceName) {
        if (draftServiceRepository.existsByDraftIdAndServiceId(draftId, serviceId)) {
            log.info("Service already exists in draft: draft_id={}, service_id={}", draftId, serviceId);
            return;
        }
        
        BookingDraft draft = getDraft(draftId);
        
        // Load Service entity từ database (cần thiết vì serviceId có insertable=false)
        com.kltn.scsms_api_service.core.entity.Service service = serviceService.findById(serviceId)
            .orElseThrow(() -> new RuntimeException("Service not found: " + serviceId));
        
        DraftService draftService = DraftService.builder()
            .draft(draft)
            .service(service) // Set service object để JPA tự động set service_id
            .serviceName(serviceName)
            .createdDate(LocalDateTime.now())
            .build();
        
        draftServiceRepository.save(draftService);
        log.info("Added service to draft: draft_id={}, service_id={}, service_name={}", 
                draftId, serviceId, serviceName);
    }
    
    /**
     * Xóa dịch vụ khỏi draft
     */
    @Transactional
    public void removeServiceFromDraft(UUID draftId, UUID serviceId) {
        draftServiceRepository.deleteByDraftIdAndServiceId(draftId, serviceId);
        log.info("Removed service from draft: draft_id={}, service_id={}", draftId, serviceId);
    }
    
    /**
     * Lấy tất cả dịch vụ của draft
     */
    public List<DraftService> getDraftServices(UUID draftId) {
        return draftServiceRepository.findByDraftId(draftId);
    }
    
    /**
     * Lấy danh sách service_id của draft
     */
    public List<UUID> getDraftServiceIds(UUID draftId) {
        return draftServiceRepository.findByDraftId(draftId).stream()
            .map(DraftService::getServiceId)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách service_name của draft
     */
    public List<String> getDraftServiceNames(UUID draftId) {
        return draftServiceRepository.findByDraftId(draftId).stream()
            .map(DraftService::getServiceName)
            .collect(Collectors.toList());
    }
    
    /**
     * Xóa tất cả dịch vụ khỏi draft
     */
    @Transactional
    public void clearDraftServices(UUID draftId) {
        draftServiceRepository.deleteByDraftId(draftId);
        log.info("Cleared all services from draft: draft_id={}", draftId);
    }
    
    /**
     * Inner class để chứa dữ liệu cập nhật
     */
    @lombok.Data
    @lombok.Builder
    public static class DraftUpdate {
        private UUID vehicleId;
        private String vehicleLicensePlate;
        private LocalDateTime dateTime;
        private UUID branchId;
        private String branchName;
        private UUID serviceId;
        private String serviceType;
        private UUID bayId;
        private String bayName;
        private String timeSlot;
    }
}

