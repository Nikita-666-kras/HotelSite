package com.travelagency.service;

import com.travelagency.domain.Role;
import com.travelagency.domain.User;
import com.travelagency.dto.AdminCreateManagerRequest;
import com.travelagency.dto.AdminSetPasswordRequest;
import com.travelagency.dto.AdminUpdateUserRequest;
import com.travelagency.dto.AdminUserResponse;
import com.travelagency.dto.CrmSalesAnalyticsResponse;
import com.travelagency.repository.BookingRepository;
import com.travelagency.repository.UserRepository;
import com.travelagency.security.UserPrincipal;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final ManagerCrmService managerCrmService;

    public AdminService(
            UserRepository userRepository,
            BookingRepository bookingRepository,
            PasswordEncoder passwordEncoder,
            ManagerCrmService managerCrmService) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
        this.managerCrmService = managerCrmService;
    }

    @Transactional(readOnly = true)
    public CrmSalesAnalyticsResponse salesAnalytics(int days) {
        return managerCrmService.salesAnalytics(days);
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> listUsers(Role role, String q, Pageable pageable) {
        Specification<User> spec = userSpec(role, q);
        return userRepository.findAll(spec, pageable).map(this::toAdminResponse);
    }

    private Specification<User> userSpec(Role role, String q) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("fullName")), pattern)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public AdminUserResponse createManager(AdminCreateManagerRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email already registered");
        }
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setFullName(req.fullName().trim());
        u.setPhone(req.phone() == null || req.phone().isBlank() ? null : req.phone().trim());
        u.setRole(Role.MANAGER);
        userRepository.save(u);
        return toAdminResponse(u);
    }

    @Transactional
    public AdminUserResponse updateUser(UUID id, AdminUpdateUserRequest req, UserPrincipal actor) {
        User u = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));

        if (req.enabled() != null
                && !req.enabled()
                && u.getId().equals(actor.getId())) {
            throw new BadRequestException("Cannot disable your own account");
        }

        if (req.email() != null && !req.email().isBlank()) {
            String e = req.email().trim().toLowerCase();
            if (!e.equalsIgnoreCase(u.getEmail()) && userRepository.existsByEmailIgnoreCase(e)) {
                throw new BadRequestException("Email already in use");
            }
            u.setEmail(e);
        }

        if (req.fullName() != null) {
            String fn = req.fullName().trim();
            if (fn.isEmpty()) {
                throw new BadRequestException("Full name cannot be empty");
            }
            u.setFullName(fn);
        }

        if (req.phone() != null) {
            u.setPhone(req.phone().isBlank() ? null : req.phone().trim());
        }

        if (req.enabled() != null && !req.enabled().equals(u.isEnabled())) {
            if (!req.enabled()) {
                ensureCanDisable(u, actor.getId());
            }
            if (!req.enabled() && u.getRole() == Role.MANAGER && u.isEnabled()) {
                bookingRepository.clearAssignedManagerById(u.getId());
            }
            u.setEnabled(req.enabled());
        }

        userRepository.save(u);
        return toAdminResponse(u);
    }

    @Transactional
    public void setUserPassword(UUID id, AdminSetPasswordRequest req) {
        User u = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        u.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(u);
    }

    @Transactional
    public void disableUser(UUID id, UserPrincipal actor) {
        if (id.equals(actor.getId())) {
            throw new AccessDeniedException("Cannot disable yourself");
        }
        User u = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        if (!u.isEnabled()) {
            return;
        }
        ensureCanDisable(u, actor.getId());
        if (u.getRole() == Role.MANAGER) {
            bookingRepository.clearAssignedManagerById(u.getId());
        }
        u.setEnabled(false);
        userRepository.save(u);
    }

    private void ensureCanDisable(User u, UUID actorId) {
        if (u.getRole() == Role.ADMIN && u.isEnabled()) {
            long otherEnabled =
                    userRepository.count((root, query, cb) -> cb.and(
                            cb.equal(root.get("role"), Role.ADMIN),
                            cb.isTrue(root.get("enabled")),
                            cb.notEqual(root.get("id"), u.getId())));
            if (otherEnabled == 0) {
                throw new BadRequestException("Cannot disable the last admin");
            }
        }
    }

    private AdminUserResponse toAdminResponse(User u) {
        return new AdminUserResponse(
                u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getRole(), u.isEnabled());
    }
}
