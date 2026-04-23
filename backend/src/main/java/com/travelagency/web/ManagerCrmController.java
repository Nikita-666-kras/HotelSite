package com.travelagency.web;

import com.travelagency.domain.BookingStatus;
import com.travelagency.dto.BookingResponse;
import com.travelagency.dto.ManagerUpdateBookingRequest;
import com.travelagency.dto.UserResponse;
import com.travelagency.security.UserPrincipal;
import com.travelagency.service.ManagerCrmService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public List<BookingResponse> pipeline(@RequestParam(required = false) BookingStatus status) {
        return managerCrmService.pipeline(status);
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
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ManagerUpdateBookingRequest req) {
        return managerCrmService.update(id, req, principal);
    }
}
