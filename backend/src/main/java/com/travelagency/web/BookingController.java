package com.travelagency.web;

import com.travelagency.dto.BookingResponse;
import com.travelagency.dto.CreateBookingRequest;
import com.travelagency.security.UserPrincipal;
import com.travelagency.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponse create(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody CreateBookingRequest req) {
        return bookingService.create(principal, req);
    }

    @GetMapping
    public List<BookingResponse> mine(@AuthenticationPrincipal UserPrincipal principal) {
        return bookingService.myBookings(principal);
    }

    @GetMapping("/{id}")
    public BookingResponse one(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        return bookingService.getByIdForUser(id, principal);
    }
}
