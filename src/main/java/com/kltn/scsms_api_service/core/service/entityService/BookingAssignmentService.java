package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.BookingAssignment;
import com.kltn.scsms_api_service.core.repository.BookingAssignmentRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingAssignmentService {
    
    private final BookingAssignmentRepository bookingAssignmentRepository;
    
    public BookingAssignment getById(UUID assignmentId) {
        return bookingAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Booking assignment not found with ID: " + assignmentId));
    }
    
    public List<BookingAssignment> findAll() {
        return bookingAssignmentRepository.findAll();
    }
    
    @Transactional
    public BookingAssignment save(BookingAssignment bookingAssignment) {
        return bookingAssignmentRepository.save(bookingAssignment);
    }
    
    @Transactional
    public BookingAssignment update(BookingAssignment bookingAssignment) {
        return bookingAssignmentRepository.save(bookingAssignment);
    }
    
    @Transactional
    public void delete(UUID assignmentId) {
        BookingAssignment bookingAssignment = getById(assignmentId);
        bookingAssignment.setIsDeleted(true);
        bookingAssignmentRepository.save(bookingAssignment);
    }
    
    public List<BookingAssignment> findByBooking(UUID bookingId) {
        return bookingAssignmentRepository.findByBooking_BookingIdOrderByAssignedAtAsc(bookingId);
    }
    
    public List<BookingAssignment> findByStaff(UUID staffId) {
        return bookingAssignmentRepository.findByStaff_UserIdOrderByAssignedFromAsc(staffId);
    }
    
    public List<BookingAssignment> findByRole(BookingAssignment.StaffRole role) {
        return bookingAssignmentRepository.findByRoleOrderByAssignedFromAsc(role);
    }
    
    public List<BookingAssignment> findByAssignmentStatus(BookingAssignment.AssignmentStatus status) {
        return bookingAssignmentRepository.findByAssignmentStatusOrderByAssignedFromAsc(status);
    }
    
    public List<BookingAssignment> findByStaffAndAssignmentStatus(UUID staffId, BookingAssignment.AssignmentStatus status) {
        return bookingAssignmentRepository.findByStaff_UserIdAndAssignmentStatusOrderByAssignedFromAsc(staffId, status);
    }
    
    public List<BookingAssignment> findByBookingAndRole(UUID bookingId, BookingAssignment.StaffRole role) {
        return bookingAssignmentRepository.findByBooking_BookingIdAndRoleOrderByAssignedAtAsc(bookingId, role);
    }
    
    public List<BookingAssignment> findAssignmentsByStaffInTimeRange(UUID staffId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return bookingAssignmentRepository.findAssignmentsByStaffInTimeRange(staffId, startDateTime, endDateTime);
    }
    
    public List<BookingAssignment> findOverlappingAssignments(UUID staffId, LocalDateTime startDateTime, LocalDateTime endDateTime, UUID excludeAssignmentId) {
        return bookingAssignmentRepository.findOverlappingAssignments(staffId, startDateTime, endDateTime, excludeAssignmentId);
    }
    
    public List<BookingAssignment> findByResourceTypeAndResourceId(BookingAssignment.ResourceType resourceType, UUID resourceId) {
        return bookingAssignmentRepository.findByResourceTypeAndResourceIdOrderByAssignedFromAsc(resourceType, resourceId);
    }
    
    public List<BookingAssignment> findAssignmentsByResourceInTimeRange(BookingAssignment.ResourceType resourceType, UUID resourceId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return bookingAssignmentRepository.findAssignmentsByResourceInTimeRange(resourceType, resourceId, startDateTime, endDateTime);
    }
    
    public long countByStaff(UUID staffId) {
        return bookingAssignmentRepository.countByStaff_UserId(staffId);
    }
    
    public long countByAssignmentStatus(BookingAssignment.AssignmentStatus status) {
        return bookingAssignmentRepository.countByAssignmentStatus(status);
    }
    
    public long countByRole(BookingAssignment.StaffRole role) {
        return bookingAssignmentRepository.countByRole(role);
    }
    
    public List<Object[]> findStaffWithLeastAssignments(BookingAssignment.StaffRole role, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return bookingAssignmentRepository.findStaffWithLeastAssignments(role, startDateTime, endDateTime);
    }
    
    public List<BookingAssignment> findCompletedAssignmentsByStaffInTimeRange(UUID staffId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return bookingAssignmentRepository.findCompletedAssignmentsByStaffInTimeRange(staffId, startDateTime, endDateTime);
    }
    
    public List<BookingAssignment> findByBookingAndAssignmentStatus(UUID bookingId, BookingAssignment.AssignmentStatus status) {
        return bookingAssignmentRepository.findByBooking_BookingIdAndAssignmentStatusOrderByAssignedAtAsc(bookingId, status);
    }
    
    public List<BookingAssignment> findActiveAssignmentsByStaff(UUID staffId) {
        return bookingAssignmentRepository.findActiveAssignmentsByStaff(staffId);
    }
    
    public List<BookingAssignment> findByAssignedBy(String assignedBy) {
        return bookingAssignmentRepository.findByAssignedByOrderByAssignedAtDesc(assignedBy);
    }
    
    @Transactional
    public void deleteByBooking(UUID bookingId) {
        bookingAssignmentRepository.deleteByBooking_BookingId(bookingId);
    }
    
    @Transactional
    public void saveAll(List<BookingAssignment> bookingAssignments) {
        bookingAssignmentRepository.saveAll(bookingAssignments);
    }
}
