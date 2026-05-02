package com.travelagency.web;

import com.travelagency.domain.BookingStatus;
import com.travelagency.dto.BookingCommentResponse;
import com.travelagency.dto.BookingPipelinePageResponse;
import com.travelagency.dto.BookingResponse;
import com.travelagency.dto.CreateBookingCommentRequest;
import com.travelagency.dto.CreateCrmTaskRequest;
import com.travelagency.dto.CrmNotificationResponse;
import com.travelagency.dto.CrmReminderResponse;
import com.travelagency.dto.CrmTaskResponse;
import com.travelagency.dto.ManagerUpdateBookingRequest;
import com.travelagency.dto.SendCrmNotificationRequest;
import com.travelagency.dto.UpdateCrmTaskRequest;
import com.travelagency.dto.UserResponse;
import com.travelagency.security.UserPrincipal;
import com.travelagency.service.ManagerCrmService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/crm")
public class ManagerCrmController {

    private final ManagerCrmService managerCrmService;

    public ManagerCrmController(ManagerCrmService managerCrmService) {
        this.managerCrmService = managerCrmService;
    }

    @GetMapping("/bookings")
    public BookingPipelinePageResponse pipeline(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) UUID managerId,
            @RequestParam(required = false) UUID tourId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return managerCrmService.pipelinePage(status, managerId, tourId, createdFrom, createdTo, q, pageable);
    }

    @GetMapping("/email-delivery")
    public Map<String, Boolean> emailDelivery() {
        return Map.of("configured", managerCrmService.isEmailDeliveryConfigured());
    }

    @GetMapping("/bookings/my")
    public List<BookingResponse> my(@AuthenticationPrincipal UserPrincipal principal) {
        return managerCrmService.myAssignments(principal);
    }

    @GetMapping("/managers")
    public List<UserResponse> managers(@AuthenticationPrincipal UserPrincipal principal) {
        return managerCrmService.listManagers(principal);
    }

    @PatchMapping("/bookings/{id}")
    public BookingResponse update(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ManagerUpdateBookingRequest req) {
        return managerCrmService.update(id, req, principal);
    }

    @GetMapping("/bookings/{id}")
    public BookingResponse bookingDetails(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        return managerCrmService.bookingDetails(id, principal);
    }

    @GetMapping("/bookings/{id}/comments")
    public List<BookingCommentResponse> bookingComments(
            @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        return managerCrmService.bookingComments(id, principal);
    }

    @PostMapping("/bookings/{id}/comments")
    public BookingCommentResponse addBookingComment(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateBookingCommentRequest req) {
        return managerCrmService.addBookingComment(id, req, principal);
    }

    @GetMapping("/tasks")
    public List<CrmTaskResponse> tasks(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "false") boolean mineOnly) {
        return managerCrmService.listTasks(principal, mineOnly);
    }

    @PostMapping("/tasks")
    public CrmTaskResponse createTask(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody CreateCrmTaskRequest req) {
        return managerCrmService.createTask(req, principal);
    }

    @PatchMapping("/tasks/{id}")
    public CrmTaskResponse updateTask(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateCrmTaskRequest req) {
        return managerCrmService.updateTask(id, req, principal);
    }

    @GetMapping("/notifications")
    public List<CrmNotificationResponse> notifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return managerCrmService.listNotifications(principal, unreadOnly);
    }

    @PostMapping("/notifications/send")
    public CrmNotificationResponse sendNotification(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody SendCrmNotificationRequest req) {
        return managerCrmService.sendNotification(req, principal);
    }

    @PatchMapping("/notifications/{id}/read")
    public CrmNotificationResponse markRead(
            @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        return managerCrmService.markNotificationRead(id, principal);
    }

    @GetMapping("/reminders")
    public List<CrmReminderResponse> reminders(@AuthenticationPrincipal UserPrincipal principal) {
        return managerCrmService.reminders(principal);
    }

}
