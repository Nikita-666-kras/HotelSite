package com.travelagency.service;

import com.travelagency.domain.Booking;
import com.travelagency.domain.BookingParticipant;
import com.travelagency.domain.BookingStatus;
import com.travelagency.domain.Role;
import com.travelagency.domain.User;
import com.travelagency.dto.BookingResponse;
import com.travelagency.dto.CreateBookingRequest;
import com.travelagency.dto.FlightRegistrationData;
import com.travelagency.dto.HotelRegistrationData;
import com.travelagency.dto.ParticipantRequest;
import com.travelagency.dto.ParticipantResponse;
import com.travelagency.dto.RailRegistrationData;
import com.travelagency.dto.TourResponse;
import com.travelagency.dto.UserResponse;
import com.travelagency.repository.BookingRepository;
import com.travelagency.repository.TourRepository;
import com.travelagency.repository.UserRepository;
import com.travelagency.security.UserPrincipal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final TourService tourService;

    public BookingService(
            BookingRepository bookingRepository,
            TourRepository tourRepository,
            UserRepository userRepository,
            TourService tourService) {
        this.bookingRepository = bookingRepository;
        this.tourRepository = tourRepository;
        this.userRepository = userRepository;
        this.tourService = tourService;
    }

    @Transactional
    public BookingResponse create(UserPrincipal principal, CreateBookingRequest req) {
        var tour = tourRepository.findById(req.tourId()).orElseThrow(() -> new NotFoundException("Tour not found"));
        User user =
                userRepository.findById(principal.getId()).orElseThrow(() -> new NotFoundException("User not found"));
        if (req.participants().size() > tour.getMaxParticipants()) {
            throw new BadRequestException("Too many participants for this tour");
        }
        Booking b = new Booking();
        b.setUser(user);
        b.setTour(tour);
        b.setStatus(BookingStatus.NEW);
        assignManagerAutomatically(b);
        b.setContactPhone(req.contactPhone());
        if (req.flightRegistration() != null) {
            b.setFlightPassengerDocNumber(req.flightRegistration().passengerDocNumber());
            b.setFlightCitizenship(req.flightRegistration().citizenship());
            b.setFlightLoyaltyProgram(req.flightRegistration().loyaltyProgram());
            b.setFlightBaggageNotes(req.flightRegistration().baggageNotes());
        }
        if (req.hotelRegistration() != null) {
            b.setHotelGuestFullName(req.hotelRegistration().guestFullName());
            b.setHotelDocumentNumber(req.hotelRegistration().documentNumber());
            b.setHotelEstimatedArrivalTime(req.hotelRegistration().estimatedArrivalTime());
            b.setHotelSpecialRequests(req.hotelRegistration().specialRequests());
        }
        if (req.railRegistration() != null) {
            b.setRailPassengerDocNumber(req.railRegistration().passengerDocNumber());
            b.setRailPreferredSeat(req.railRegistration().preferredSeat());
            b.setRailWagonPreferences(req.railRegistration().wagonPreferences());
            b.setRailNotes(req.railRegistration().notes());
        }
        b.setCreatedAt(Instant.now());
        b.setUpdatedAt(Instant.now());
        for (ParticipantRequest p : req.participants()) {
            BookingParticipant bp = new BookingParticipant();
            bp.setBooking(b);
            bp.setFirstName(p.firstName());
            bp.setLastName(p.lastName());
            bp.setDateOfBirth(p.dateOfBirth());
            bp.setChild(p.child());
            bp.setPassportSeries(p.passportSeries());
            bp.setPassportNumber(p.passportNumber());
            bp.setPassportIssueDate(p.passportIssueDate());
            bp.setBirthCertificateNumber(p.birthCertificateNumber());
            bp.setPhone(p.phone());
            bp.setComment(p.comment());
            b.getParticipants().add(bp);
        }
        bookingRepository.save(b);
        return toResponseInternal(b);
    }

    private void assignManagerAutomatically(Booking booking) {
        List<User> managers = userRepository.findByRoleAndEnabledTrueOrderByIdAsc(Role.MANAGER);
        if (managers.isEmpty()) {
            return;
        }
        long offset = bookingRepository.count();
        User selected = managers.get((int) (offset % managers.size()));
        booking.setAssignedManager(selected);
        booking.setStatus(BookingStatus.ASSIGNED);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> myBookings(UserPrincipal principal) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(principal.getId()).stream()
                .map(this::toResponseInternal)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getByIdForUser(UUID id, UserPrincipal principal) {
        Booking b =
                bookingRepository.findById(id).orElseThrow(() -> new NotFoundException("Booking not found"));
        if (principal.getRole() == Role.USER && !b.getUser().getId().equals(principal.getId())) {
            throw new NotFoundException("Booking not found");
        }
        return toResponseInternal(b);
    }

    public BookingResponse toResponseInternal(Booking b) {
        return toResponse(b);
    }

    private BookingResponse toResponse(Booking b) {
        TourResponse tour = tourService.getById(b.getTour().getId());
        UserResponse client =
                new UserResponse(
                        b.getUser().getId(),
                        b.getUser().getEmail(),
                        b.getUser().getFullName(),
                        b.getUser().getPhone(),
                        b.getUser().getRole());
        UserResponse manager = null;
        if (b.getAssignedManager() != null) {
            var m = b.getAssignedManager();
            manager = new UserResponse(m.getId(), m.getEmail(), m.getFullName(), m.getPhone(), m.getRole());
        }
        List<ParticipantResponse> parts =
                b.getParticipants().stream()
                        .map(
                                p ->
                                        new ParticipantResponse(
                                                p.getId(),
                                                p.getFirstName(),
                                                p.getLastName(),
                                                p.getDateOfBirth(),
                                                p.isChild(),
                                                p.getPassportSeries(),
                                                p.getPassportNumber(),
                                                p.getPassportIssueDate(),
                                                p.getBirthCertificateNumber(),
                                                p.getPhone(),
                                                p.getComment()))
                        .toList();
        FlightRegistrationData flightRegistration = new FlightRegistrationData(
                b.getFlightPassengerDocNumber(),
                b.getFlightCitizenship(),
                b.getFlightLoyaltyProgram(),
                b.getFlightBaggageNotes());
        HotelRegistrationData hotelRegistration = new HotelRegistrationData(
                b.getHotelGuestFullName(),
                b.getHotelDocumentNumber(),
                b.getHotelEstimatedArrivalTime(),
                b.getHotelSpecialRequests());
        RailRegistrationData railRegistration = new RailRegistrationData(
                b.getRailPassengerDocNumber(), b.getRailPreferredSeat(), b.getRailWagonPreferences(), b.getRailNotes());

        return new BookingResponse(
                b.getId(),
                b.getStatus(),
                b.getContactPhone(),
                b.getNotes(),
                flightRegistration,
                hotelRegistration,
                railRegistration,
                tour,
                client,
                manager,
                parts,
                b.getCreatedAt(),
                b.getUpdatedAt());
    }
}
