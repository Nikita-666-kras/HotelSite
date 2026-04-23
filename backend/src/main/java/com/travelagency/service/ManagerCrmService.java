package com.travelagency.service;

import com.travelagency.domain.Booking;
import com.travelagency.domain.BookingStatus;
import com.travelagency.domain.Role;
import com.travelagency.domain.User;
import com.travelagency.dto.BookingResponse;
import com.travelagency.dto.ManagerUpdateBookingRequest;
import com.travelagency.dto.UserResponse;
import com.travelagency.repository.BookingRepository;
import com.travelagency.repository.UserRepository;
import com.travelagency.security.UserPrincipal;
import java.time.Instant;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManagerCrmService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;

    public ManagerCrmService(
            BookingRepository bookingRepository, UserRepository userRepository, BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.bookingService = bookingService;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> pipeline(BookingStatus status) {
        if (status == null) {
            return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(bookingService::toResponseInternal)
                    .toList();
        }
        return bookingRepository.findByStatusOrderByCreatedAtAsc(status).stream()
                .map(bookingService::toResponseInternal)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> myAssignments(UserPrincipal principal) {
        return bookingRepository.findByAssignedManagerIdOrderByUpdatedAtDesc(principal.getId()).stream()
                .map(bookingService::toResponseInternal)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listManagers(UserPrincipal actor) {
        if (actor.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admin can redistribute");
        }
        return userRepository.findByRoleAndEnabledTrueOrderByIdAsc(Role.MANAGER).stream()
                .map(u -> new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getRole()))
                .toList();
    }

    @Transactional
    public BookingResponse update(Long id, ManagerUpdateBookingRequest req, UserPrincipal actor) {
        Booking b = bookingRepository.findById(id).orElseThrow(() -> new NotFoundException("Booking not found"));
        if (req.status() != null) {
            b.setStatus(req.status());
        }
        if (req.notes() != null) {
            b.setNotes(req.notes());
        }
        if (req.assignedManagerId() != null) {
            if (actor.getRole() != Role.ADMIN) {
                throw new AccessDeniedException("Only admin can redistribute");
            }
            User mgr =
                    userRepository
                            .findById(req.assignedManagerId())
                            .orElseThrow(() -> new BadRequestException("Manager not found"));
            if (mgr.getRole() != Role.MANAGER) {
                throw new BadRequestException("User is not a manager");
            }
            b.setAssignedManager(mgr);
            if (b.getStatus() == BookingStatus.NEW) {
                b.setStatus(BookingStatus.ASSIGNED);
            }
        }
        b.setUpdatedAt(Instant.now());
        bookingRepository.save(b);
        return bookingService.toResponseInternal(b);
    }
}
