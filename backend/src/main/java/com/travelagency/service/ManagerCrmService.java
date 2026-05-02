package com.travelagency.service;

import com.travelagency.domain.Booking;
import com.travelagency.domain.BookingComment;
import com.travelagency.domain.BookingStatus;
import com.travelagency.domain.CrmNotification;
import com.travelagency.domain.CrmTask;
import com.travelagency.domain.CrmTaskStatus;
import com.travelagency.domain.Role;
import com.travelagency.domain.User;
import com.travelagency.dto.BookingCommentResponse;
import com.travelagency.dto.BookingPipelinePageResponse;
import com.travelagency.dto.BookingResponse;
import com.travelagency.dto.CreateBookingCommentRequest;
import com.travelagency.dto.CreateCrmTaskRequest;
import com.travelagency.dto.CrmNotificationResponse;
import com.travelagency.dto.CrmReminderResponse;
import com.travelagency.dto.CrmSalesAnalyticsResponse;
import com.travelagency.dto.CrmTaskResponse;
import com.travelagency.dto.ManagerUpdateBookingRequest;
import com.travelagency.dto.SendCrmNotificationRequest;
import com.travelagency.dto.UpdateCrmTaskRequest;
import com.travelagency.dto.UserResponse;
import com.travelagency.repository.BookingRepository;
import com.travelagency.repository.BookingSpecifications;
import com.travelagency.repository.BookingCommentRepository;
import com.travelagency.repository.CrmNotificationRepository;
import com.travelagency.repository.CrmTaskRepository;
import com.travelagency.repository.UserRepository;
import com.travelagency.security.UserPrincipal;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManagerCrmService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CrmTaskRepository crmTaskRepository;
    private final CrmNotificationRepository crmNotificationRepository;
    private final BookingCommentRepository bookingCommentRepository;
    private final BookingService bookingService;
    private final CrmMailService crmMailService;

    public ManagerCrmService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            CrmTaskRepository crmTaskRepository,
            CrmNotificationRepository crmNotificationRepository,
            BookingCommentRepository bookingCommentRepository,
            BookingService bookingService,
            CrmMailService crmMailService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.crmTaskRepository = crmTaskRepository;
        this.crmNotificationRepository = crmNotificationRepository;
        this.bookingCommentRepository = bookingCommentRepository;
        this.bookingService = bookingService;
        this.crmMailService = crmMailService;
    }

    public boolean isEmailDeliveryConfigured() {
        return crmMailService.isAvailable();
    }

    @Transactional(readOnly = true)
    public BookingPipelinePageResponse pipelinePage(
            BookingStatus status,
            UUID managerId,
            UUID tourId,
            LocalDate createdFrom,
            LocalDate createdTo,
            String q,
            Pageable pageable) {
        Specification<Booking> base =
                BookingSpecifications.pipelineFilter(status, managerId, tourId, createdFrom, createdTo, q);
        Page<Booking> page = bookingRepository.findAll(base, pageable);
        Specification<Booking> inWorkSpec = base.and((root, cq, cb) -> cb.or(
                cb.equal(root.get("status"), BookingStatus.NEW),
                cb.equal(root.get("status"), BookingStatus.ASSIGNED)));
        Specification<Booking> confirmedSpec =
                base.and((root, cq, cb) -> cb.equal(root.get("status"), BookingStatus.CONFIRMED));
        long inWork = bookingRepository.count(inWorkSpec);
        long confirmed = bookingRepository.count(confirmedSpec);
        return new BookingPipelinePageResponse(
                page.getContent().stream().map(bookingService::toResponseInternal).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                inWork,
                confirmed);
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

    @Transactional(readOnly = true)
    public BookingResponse bookingDetails(UUID id, UserPrincipal actor) {
        ensureStaffAccess(actor);
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new NotFoundException("Booking not found"));
        return bookingService.toResponseInternal(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingCommentResponse> bookingComments(UUID bookingId, UserPrincipal actor) {
        ensureStaffAccess(actor);
        ensureBookingExists(bookingId);
        return bookingCommentRepository.findByBookingIdOrderByCreatedAtAsc(bookingId).stream()
                .map(this::toCommentResponse)
                .toList();
    }

    @Transactional
    public BookingCommentResponse addBookingComment(UUID bookingId, CreateBookingCommentRequest req, UserPrincipal actor) {
        ensureStaffAccess(actor);
        Booking booking = ensureBookingExists(bookingId);
        User author = userRepository.findById(actor.getId()).orElseThrow(() -> new NotFoundException("User not found"));
        BookingComment comment = new BookingComment();
        comment.setBooking(booking);
        comment.setAuthor(author);
        comment.setBody(req.body().trim());
        comment.setCreatedAt(Instant.now());
        bookingCommentRepository.save(comment);
        return toCommentResponse(comment);
    }

    @Transactional
    public BookingResponse update(UUID id, ManagerUpdateBookingRequest req, UserPrincipal actor) {
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
            createNotification(
                    mgr,
                    "IN_APP",
                    "Новая назначенная заявка",
                    "Вам назначена заявка #" + b.getId() + " по туру: " + b.getTour().getTitle());
            if (b.getStatus() == BookingStatus.NEW) {
                b.setStatus(BookingStatus.ASSIGNED);
            }
        }
        if (req.status() == BookingStatus.CONFIRMED && b.getAssignedManager() != null) {
            createNotification(
                    b.getAssignedManager(),
                    "IN_APP",
                    "Заявка подтверждена",
                    "Заявка #" + b.getId() + " перешла в статус CONFIRMED.");
        }
        b.setUpdatedAt(Instant.now());
        bookingRepository.save(b);
        return bookingService.toResponseInternal(b);
    }

    @Transactional(readOnly = true)
    public List<CrmTaskResponse> listTasks(UserPrincipal actor, boolean mineOnly) {
        List<CrmTask> tasks = mineOnly || actor.getRole() == Role.MANAGER
                ? crmTaskRepository.findByAssigneeIdOrderByCreatedAtDesc(actor.getId())
                : crmTaskRepository.findAllByOrderByCreatedAtDesc();
        return tasks.stream().map(this::toTaskResponse).toList();
    }

    @Transactional
    public CrmTaskResponse createTask(CreateCrmTaskRequest req, UserPrincipal actor) {
        if (actor.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admin can create tasks");
        }
        UUID assigneeId = req.assigneeId();
        if (assigneeId == null) {
            throw new BadRequestException("assigneeId is required");
        }
        User assignee = userRepository.findById(assigneeId).orElseThrow(() -> new BadRequestException("Assignee not found"));
        if (assignee.getRole() != Role.MANAGER && assignee.getRole() != Role.ADMIN) {
            throw new BadRequestException("Assignee must be manager/admin");
        }
        CrmTask task = new CrmTask();
        task.setTitle(req.title().trim());
        task.setDescription(req.description() == null ? null : req.description().trim());
        task.setAssignee(assignee);
        task.setDueDate(req.dueDate());
        if (req.bookingId() != null) {
            Booking booking = bookingRepository.findById(req.bookingId()).orElseThrow(() -> new BadRequestException("Booking not found"));
            task.setBooking(booking);
        }
        task.setUpdatedAt(Instant.now());
        crmTaskRepository.save(task);
        createNotification(
                assignee,
                "IN_APP",
                "Новая CRM задача",
                "Задача: " + task.getTitle() + (task.getDueDate() != null ? " (до " + task.getDueDate() + ")" : ""));
        return toTaskResponse(task);
    }

    @Transactional
    public CrmTaskResponse updateTask(UUID id, UpdateCrmTaskRequest req, UserPrincipal actor) {
        CrmTask task = crmTaskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task not found"));
        if (actor.getRole() == Role.MANAGER && !task.getAssignee().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Manager can update only own tasks");
        }
        if (req.title() != null) {
            task.setTitle(req.title().trim());
        }
        if (req.description() != null) {
            task.setDescription(req.description().trim());
        }
        if (req.status() != null) {
            task.setStatus(req.status());
        }
        if (req.dueDate() != null) {
            task.setDueDate(req.dueDate());
        }
        task.setUpdatedAt(Instant.now());
        crmTaskRepository.save(task);
        return toTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public List<CrmNotificationResponse> listNotifications(UserPrincipal actor, boolean unreadOnly) {
        List<CrmNotification> notifications = unreadOnly
                ? crmNotificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(actor.getId())
                : crmNotificationRepository.findByRecipientIdOrderByCreatedAtDesc(actor.getId());
        return notifications.stream().map(this::toNotificationResponse).toList();
    }

    @Transactional
    public CrmNotificationResponse sendNotification(SendCrmNotificationRequest req, UserPrincipal actor) {
        if (actor.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admin can send notifications");
        }
        UUID recipientId = req.recipientId();
        if (recipientId == null) {
            throw new BadRequestException("recipientId is required");
        }
        User recipient =
                userRepository.findById(recipientId).orElseThrow(() -> new BadRequestException("Recipient not found"));
        String subject = req.subject().trim();
        String body = req.body().trim();
        String channel = req.channel().trim().toUpperCase();
        if ("EMAIL".equals(channel) && !crmMailService.isAvailable()) {
            throw new BadRequestException(
                    "Email delivery is not configured. Set app.resend.api-key and app.mail.from, or use IN_APP.");
        }
        CrmNotification n = createNotification(recipient, channel, subject, body);
        if ("EMAIL".equals(channel)) {
            crmMailService.sendPlain(recipient.getEmail(), subject, body);
        }
        return toNotificationResponse(n);
    }

    @Transactional
    public CrmNotificationResponse markNotificationRead(UUID id, UserPrincipal actor) {
        CrmNotification n =
                crmNotificationRepository.findById(id).orElseThrow(() -> new NotFoundException("Notification not found"));
        if (!n.getRecipient().getId().equals(actor.getId()) && actor.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Cannot update another user's notification");
        }
        n.setRead(true);
        crmNotificationRepository.save(n);
        return toNotificationResponse(n);
    }

    @Transactional(readOnly = true)
    public List<CrmReminderResponse> reminders(UserPrincipal actor) {
        LocalDate soon = LocalDate.now().plusDays(2);
        List<CrmReminderResponse> result = crmTaskRepository
                .findByStatusInAndDueDateLessThanEqualOrderByDueDateAsc(List.of(CrmTaskStatus.OPEN, CrmTaskStatus.IN_PROGRESS), soon)
                .stream()
                .filter(task -> actor.getRole() == Role.ADMIN || task.getAssignee().getId().equals(actor.getId()))
                .map(task -> new CrmReminderResponse(
                        "TASK_DUE",
                        "Срок по задаче \"" + task.getTitle() + "\" скоро истекает",
                        task.getBooking() != null ? task.getBooking().getId() : null,
                        task.getId(),
                        String.valueOf(task.getDueDate())))
                .toList();
        return result;
    }

    @Transactional(readOnly = true)
    public CrmSalesAnalyticsResponse salesAnalytics(int days) {
        if (days <= 0) {
            days = 30;
        }
        Instant cutoff = Instant.now().minusSeconds(days * 24L * 3600L);
        List<Booking> bookings = bookingRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(b -> b.getCreatedAt().isAfter(cutoff))
                .toList();
        long total = bookings.size();
        long confirmed = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
        long lost = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.REJECTED)
                .count();
        BigDecimal revenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .map(b -> b.getTour().getPrice() == null ? BigDecimal.ZERO : b.getTour().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal conversion = total == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(confirmed * 100.0d / total).setScale(2, java.math.RoundingMode.HALF_UP);
        return new CrmSalesAnalyticsResponse(total, confirmed, lost, revenue, conversion);
    }

    private CrmNotification createNotification(User recipient, String channel, String subject, String body) {
        CrmNotification n = new CrmNotification();
        n.setRecipient(recipient);
        n.setChannel(channel == null || channel.isBlank() ? "IN_APP" : channel.trim().toUpperCase());
        n.setSubject(subject);
        n.setBody(body);
        n.setCreatedAt(Instant.now());
        crmNotificationRepository.save(n);
        return n;
    }

    private Booking ensureBookingExists(UUID bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found"));
    }

    private void ensureStaffAccess(UserPrincipal actor) {
        if (actor.getRole() != Role.ADMIN && actor.getRole() != Role.MANAGER) {
            throw new AccessDeniedException("Only managers/admin can access CRM booking details");
        }
    }

    private CrmTaskResponse toTaskResponse(CrmTask task) {
        return new CrmTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                new UserResponse(
                        task.getAssignee().getId(),
                        task.getAssignee().getEmail(),
                        task.getAssignee().getFullName(),
                        task.getAssignee().getPhone(),
                        task.getAssignee().getRole()),
                task.getBooking() != null ? task.getBooking().getId() : null,
                task.getBooking() != null ? task.getBooking().getTour().getTitle() : null,
                task.getCreatedAt(),
                task.getUpdatedAt());
    }

    private CrmNotificationResponse toNotificationResponse(CrmNotification n) {
        User r = n.getRecipient();
        return new CrmNotificationResponse(
                n.getId(),
                n.getChannel(),
                n.getSubject(),
                n.getBody(),
                n.isRead(),
                new UserResponse(r.getId(), r.getEmail(), r.getFullName(), r.getPhone(), r.getRole()),
                n.getCreatedAt());
    }

    private BookingCommentResponse toCommentResponse(BookingComment comment) {
        User author = comment.getAuthor();
        return new BookingCommentResponse(
                comment.getId(),
                comment.getBody(),
                new UserResponse(author.getId(), author.getEmail(), author.getFullName(), author.getPhone(), author.getRole()),
                comment.getCreatedAt());
    }
}
